/*

   Copyright 2013 Albert Tedja

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */


package org.thinschema;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import org.thinschema.schemas.DBSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * DatabaseManager extends from Android's standard SQLiteOpenHelper.
 * It overrides only two methods: onCreate and onUpgrade.
 * <p/>
 * You may extend from DatabaseManager.
 *
 * @author atedja
 */
public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DT_INTEGER = "integer";
    private static final String DT_INT = "int";
    private static final String DT_REAL = "real";
    private static final String DT_FLOAT = "float";
    private static final String DT_DOUBLE = "double";
    private static final String DT_TEXT = "text";
    private static final String DT_STRING = "string";

    private DBSchema dbSchema;

    public DatabaseManager(Context context, DBSchema databaseSchema) {
        this(context, databaseSchema, false);
    }

    public DatabaseManager(Context context,
                           DBSchema databaseSchema,
                           boolean inMemory) {
        super(context, inMemory ? null : databaseSchema.getDatabaseName(),
            null, databaseSchema.getDatabaseVersion());
        dbSchema = databaseSchema;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        List<String> tables = dbSchema.getTableNames();
        for (String table : tables) {
            createTable(sqLiteDatabase, table);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,
                          int oldVersion,
                          int newVersion) {
        // get all table names from the old database
        List<String> oldTableNames = getTableNames(sqLiteDatabase);

        sqLiteDatabase.beginTransaction();

        try {
            // get all table names from the new schema
            List<String> newTableNames = dbSchema.getTableNames();

            // Iterate through tables that exists in both old and new versions, and migrate them.
            // oti = old table index, nti = new table index
            for (int oti = oldTableNames.size() - 1; oti >= 0; --oti) {
                for (int nti = newTableNames.size() - 1; nti >= 0; --nti) {
                    if (oldTableNames.get(oti).equals(newTableNames.get(nti))) {
                        // migrate table
                        migrateTable(sqLiteDatabase, newTableNames.get(nti), true);

                        // remove table from both arrays
                        oldTableNames.remove(oti);
                        newTableNames.remove(nti);

                        // exit nti loop
                        break;
                    }
                }
            }

            // oldTableNames will contain tables to delete
            for (String oldTable : oldTableNames) {
                deleteTable(sqLiteDatabase, oldTable);
            }

            // newTableNames will contain tables to create
            for (String newTable : newTableNames) {
                createTable(sqLiteDatabase, newTable);
            }

            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    /**
     * Migrates a table to the new schema. A new table is created, data from
     * old table is copied over to the new table, then the old table is deleted.
     *
     * @param sqLiteDatabase SQLiteDatabase instance.
     * @param tableName      The name of the table to be migrated.
     * @param migrateData    true if data should be migrated. false will erase
     *                       existing data.
     */
    private void migrateTable(SQLiteDatabase sqLiteDatabase, String tableName, boolean migrateData) {
        // rename old table to a new name
        String oldTableName = tableName + "_old";
        sqLiteDatabase.execSQL("ALTER TABLE " + tableName + " RENAME TO " + oldTableName + ";");

        // create the new table
        createTable(sqLiteDatabase, tableName);

        // copy existing data from old table to the new table
        if (migrateData) {
            // grab the column names of the old table
            List<String> oldColumns = getColumnNames(sqLiteDatabase, oldTableName);

            // column names of the new table
            List<String> newColumns = dbSchema.getColumnNames(tableName);

            // intersect, removes old column names that are no longer specified
            oldColumns.retainAll(newColumns);

            // construct a list of comma-delimited column names
            String columns = TextUtils.join(",", oldColumns);

            // copy data from old table to the new table
            String sql = "INSERT INTO " + tableName + " (" + columns + ") SELECT " + columns + " FROM " + oldTableName + ";";
            Log.d("", sql);
            sqLiteDatabase.execSQL(sql);
        }

        // delete old table
        deleteTable(sqLiteDatabase, oldTableName);
    }

    /**
     * Creates a table from a JSON schema.
     *
     * @param sqLiteDatabase SQLiteDatabase instance.
     * @param tableName      The name of the table to be created.
     */
    private void createTable(SQLiteDatabase sqLiteDatabase, String tableName) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");

        for (int i = 0, size = dbSchema.getColumnCount(tableName); i < size; ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(dbSchema.getColumnName(tableName, i));

            String type = dbSchema.getColumnType(tableName, i);
            if (DT_INTEGER.equalsIgnoreCase(type)
                || DT_INT.equalsIgnoreCase(type)) {
                sb.append(" INTEGER");
            } else if (DT_TEXT.equals(type)
                || DT_STRING.equalsIgnoreCase(type)) {
                sb.append(" TEXT");
            } else if (DT_REAL.equals(type)
                || DT_FLOAT.equalsIgnoreCase(type)
                || DT_DOUBLE.equalsIgnoreCase(type)) {
                sb.append(" REAL");
            }

            if (dbSchema.getColumnIsPrimary(tableName, i)) {
                sb.append(" PRIMARY KEY");
            }

            if (dbSchema.getColumnAutoIncrement(tableName, i)) {
                sb.append(" AUTOINCREMENT");
            }

            if (dbSchema.getColumnNotNull(tableName, i)) {
                sb.append(" NOT NULL");
            }

            String defaultValue = dbSchema.getColumnDefaultValue(tableName, i);
            if (defaultValue != null && defaultValue.length() > 0) {
                sb.append(" DEFAULT ").append(defaultValue);
            }
        }

        sb.append(");");
        sqLiteDatabase.execSQL(sb.toString());
    }

    /**
     * Delete an existing table.
     *
     * @param sqLiteDatabase SQLiteDatabase instance.
     * @param tableName      The name of the table to be deleted.
     */
    private void deleteTable(SQLiteDatabase sqLiteDatabase, String tableName) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableName + ";");
    }

    /**
     * Get a list of column names from a given table. Table must exist in the given
     * database.
     *
     * @param sqLiteDatabase Database name.
     * @param tableName      Name of the table.
     * @return A list of column names,
     */
    private static List<String> getColumnNames(SQLiteDatabase sqLiteDatabase,
                                               String tableName) {
        List<String> retval = null;
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query(tableName, null, null, null, null, null, null);
            if (cursor != null) {
                retval = new ArrayList<String>(Arrays.asList(cursor.getColumnNames()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return retval;
    }


    private static List<String> getTableNames(SQLiteDatabase sqLiteDatabase) {
        List<String> tableNames = null;
        Cursor cursor = null;

        try {
            cursor = sqLiteDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;", null);
            if (cursor.moveToFirst()) {
                tableNames = new ArrayList<String>();
                while (!cursor.isAfterLast()) {
                    String tableName = cursor.getString(0);
                    // ignore system tables
                    if (!"android_metadata".equals(tableName) &&
                        !"sqlite_sequence".equals(tableName)) {
                        tableNames.add(tableName);
                    }
                    cursor.moveToNext();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return tableNames;
    }
}
