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

package org.thinschema.dataaccess;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.thinschema.schemas.DBSchema;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Helper methods to insert/update/delete records.
 *
 * @author atedja
 */
public class JSONAdapter {


    /**
     * Get all data from table and convert them to JSON. Each row is converted
     * to its own JSONObject containing the names of the columns as keys, and
     * the contents as values. For example, if a table whose name is "People"
     * consists of two columns "FirstName" and "LastName", the generated JSON
     * would be:
     * </p>
     * <code>
     * { "name": "People", "rows": [ { "FirstName": "John", "LastName": "Doe" },
     * { "FirstName": "Susan", "LastName": "Appleseed" } ] }
     * </code>
     *
     * @param database  SQLiteDatabase instance.
     * @param tableName The name of the table.
     * @return JSONObject instance containing all records.
     */
    public static JSONObject get(SQLiteDatabase database, String tableName) {

        JSONObject retval = new JSONObject();
        Cursor cursor = null;
        try {
            retval.put("name", tableName);
            cursor = database.query(tableName, null, null, null, null, null, null);

            // we get the list of all column names to make it easier when inserting key-value pairs
            String[] columnNames = cursor.getColumnNames();

            // iterate through each row
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                JSONArray data = new JSONArray();
                while (!cursor.isAfterLast()) {
                    JSONObject row = new JSONObject();

                    // for each column, store the key-value pair, using column name as key
                    for (int i = 0, size = columnNames.length; i < size; ++i) {
                        // convert everything to a string
                        row.put(columnNames[i], cursor.getString(i));
                    }

                    data.put(row);
                }

                // add all of that to the result
                retval.put("rows", data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return retval;
    }

    /**
     * Fill a table with data from a JSON. The JSON must contain an array of
     * JSON named 'rows', where each JSON represents one row (or record)
     * in the database.
     *
     * @param sqLiteDatabase SQLiteDatabase instance.
     * @param dbSchema       Database schema.
     * @param tableName      Table name.
     * @param jsonData       JSONObject
     * @return true if insertion is successful.
     */
    public static boolean fill(SQLiteDatabase sqLiteDatabase, DBSchema dbSchema, String tableName, JSONObject jsonData) {
        boolean success = true;
        JSONArray data = jsonData.optJSONArray("rows");

        sqLiteDatabase.beginTransaction();
        try {
            if (data != null && data.length() > 0) {
                for (int i = 0, size = data.length(); i < size; ++i) {
                    JSONObject row = data.optJSONObject(i);
                    ContentValues cv = toContentValues(row, dbSchema, tableName);
                    if (sqLiteDatabase.insert(tableName, null, cv) == -1) {
                        success = false;
                        break;
                    }
                }
            }

            if (success) {
                sqLiteDatabase.setTransactionSuccessful();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
        return success;
    }

    /**
     * Convert a JSONObject to ConventValues, based on the provided schema.
     * Keys that do not exist in the schema as columns will be ignored.
     * Likewise, columns that exist in schema but does not exist in the JSON
     * will use the default value.
     *
     * @param jsonObject JSONObject instance.
     * @param dbSchema   Database schema.
     * @param tableName  Name of table.
     * @return An instance of ContentValues.
     */
    private static ContentValues toContentValues(JSONObject jsonObject, DBSchema dbSchema, String tableName) {
        ContentValues cv = new ContentValues();

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();

            // check if this column exist in the schema
            boolean add = false;
            for (int i = 0, size = dbSchema.getColumnCount(tableName); i < size; ++i) {
                if (dbSchema.getColumnName(tableName, i).equals(key)) {
                    add = true;
                    break;
                }
            }

            if (add) {
                cv.put(key, jsonObject.optString(key));
            }
        }
        return cv;
    }
}
