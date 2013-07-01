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


package org.thinschema.tests;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Quick database validator.
 * Tests only!
 *
 * @author atedja
 */
public class DBValidator {

    /**
     * Check if table exists
     *
     * @param db
     * @param tableName
     * @return
     */
    public static boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;", null);
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String table = cursor.getString(0);
                    // ignore system tables
                    if (table.equals(tableName)) {
                        return true;
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

        return false;
    }


    /**
     * Grab the values of the specified column and table.
     *
     * @param db
     * @param tableName
     * @param columnName
     * @return
     */
    public static List<String> getValues(SQLiteDatabase db, String tableName, String columnName) {
        List<String> retval = null;
        Cursor cursor = null;
        try {
            cursor = db.query(tableName, new String[]{columnName}, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                retval = new ArrayList<String>();
                while (!cursor.isAfterLast()) {
                    retval.add(cursor.getString(0));
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
        return retval;
    }


    /**
     * Check if a column exist in the table.
     *
     * @param db
     * @param tableName
     * @param columnName
     * @return
     */
    public static boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = null;
        try {
            cursor = db.query(tableName, null, null, null, null, null, null);

            if (cursor != null) {
                String[] columns = cursor.getColumnNames();
                for (String column : columns) {
                    if (columnName.equals(column)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

}
