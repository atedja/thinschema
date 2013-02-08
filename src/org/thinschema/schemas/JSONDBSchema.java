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


package org.thinschema.schemas;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * DBSchema implementation for JSON format.
 *
 * @author atedja
 */
public final class JSONDBSchema implements DBSchema {

    private String dbName;
    private int dbVersion;
    private Table[] dbTables;
    private HashMap<String, List<Column>> dbColumns;

    public JSONDBSchema(JSONObject jsonSchema) {
        dbName = jsonSchema.optString("name");
        dbVersion = jsonSchema.optInt("version");

        // parse the table and columns
        JSONArray tables = jsonSchema.optJSONArray("tables");
        dbTables = new Table[tables.length()];
        dbColumns = new HashMap<String, List<Column>>(tables.length());
        for (int i = 0, size = tables.length(); i < size; ++i) {
            JSONObject tableJson = tables.optJSONObject(i);
            Table table = new Table();
            table.name = tableJson.optString("name");
            table.autoPrimaryKey = tableJson.optBoolean("autoPrimaryKey");

            JSONArray columns = tableJson.optJSONArray("columns");
            ArrayList<Column> columnsList = new ArrayList<Column>(columns.length());
            for (int j = 0, columnSize = columns.length(); j < columnSize; ++j) {
                JSONObject columnJson = columns.optJSONObject(j);
                Column column = new Column();
                column.name = columnJson.optString("name");
                column.type = columnJson.optString("type");
                column.isPrimary = columnJson.optBoolean("isPrimary");
                column.autoIncrement = columnJson.optBoolean("autoIncrement");
                column.notNull = columnJson.optBoolean("notNull");
                column.defaultValue = columnJson.optString("defaultValue");
                columnsList.add(j, column);
            }

            dbTables[i] = table;
            dbColumns.put(table.name, columnsList);
        }
    }

    public String getDatabaseName() {
        return dbName;
    }

    public int getDatabaseVersion() {
        return dbVersion;
    }

    public int getTableCount() {
        return dbTables.length;
    }

    public String getTableName(int index) {
        return dbTables[index].name;
    }

    public boolean getTableAutoPrimaryKey(int index) {
        return dbTables[index].autoPrimaryKey;
    }

    public List<String> getTableNames() {
        ArrayList<String> array = new ArrayList<String>();
        for (Table table : dbTables) {
            array.add(table.name);
        }
        return array;
    }

    public int getColumnCount(String table) {
        return dbColumns.get(table).size();
    }

    public String getColumnName(String table, int index) {
        return dbColumns.get(table).get(index).name;
    }

    public List<String> getColumnNames(String table) {
        List<String> columnNames = new ArrayList<String>();
        List<Column> columns = dbColumns.get(table);
        for (Column c : columns) {
            columnNames.add(c.name);
        }
        return columnNames;
    }

    public String getColumnType(String table, int index) {
        return dbColumns.get(table).get(index).type;
    }

    public boolean getColumnIsPrimary(String table, int index) {
        return dbColumns.get(table).get(index).isPrimary;
    }

    public boolean getColumnAutoIncrement(String table, int index) {
        return dbColumns.get(table).get(index).autoIncrement;
    }

    public boolean getColumnNotNull(String table, int index) {
        return dbColumns.get(table).get(index).notNull;
    }

    public String getColumnDefaultValue(String table, int index) {
        return dbColumns.get(table).get(index).defaultValue;
    }
}
