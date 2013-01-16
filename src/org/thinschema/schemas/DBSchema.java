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

import java.util.List;

/**
 * Defines a database schema. Classes that implement this interface must sanitize the database, table, and column names.
 * 
 * @author atedja
 */
public interface DBSchema {

    /**
     * Get the name of the database.
     */
    public String getDatabaseName();

    /**
     * Get the database version.
     */
    public int getDatabaseVersion();

    /**
     * Get the number of tables in the database.
     */
    public int getTableCount();

    /**
     * Get the name of the table at the specified index.
     *
     * @param index Index of the table
     * @return Table name.
     */
    public String getTableName(int index);

    /**
     * Helper method that should return a List of all table names.
     *
     * @return
     */
    public List<String> getTableNames();

    /**
     * Get the number of columns of the given table.
     *
     * @param table Table name.
     * @return The number of columns.
     */
    public int getColumnCount(String table);

    /**
     * Get the name of a column
     *
     * @param table The table name.
     * @param index Index of the column.
     * @return Name of the column.
     */
    public String getColumnName(String table, int index);

    /**
     * Helper method to get a List of all column names.
     *
     * @param table The table name.
     * @return
     */
    public List<String> getColumnNames(String table);

    /**
     * Get the type of a column, such as "integer", "text, etc.
     *
     * @param table The name of the table.
     * @param index Index of the column.
     * @return The type of column.
     */
    public String getColumnType(String table, int index);

    /**
     * Check if colpumn has PRIMARY KEY property.
     *
     * @param table The table name.
     * @param index The index of the column.
     * @return true if it's a primary key column, false otherwise.
     */
    public boolean getColumnIsPrimary(String table, int index);

    /**
     * Check if the column has AUTO INCREMENT property.
     *
     * @param table The table name.
     * @param index The index of the column.
     * @return true if it's AUTO INCREMENT, false otherwise.
     */
    public boolean getColumnAutoIncrement(String table, int index);

    /**
     * Check if column has NOT NULL property.
     *
     * @param table The table name.
     * @param index The index of the column.
     * @return true if it's NOT NULL, false otherwise.
     */
    public boolean getColumnNotNull(String table, int index);

    /**
     * Get the default value of the column.
     *
     * @param table The table name.
     * @param index The index of the column.
     * @return The default value, if any, or null if no default value is specified.
     */
    public String getColumnDefaultValue(String table, int index);

}
