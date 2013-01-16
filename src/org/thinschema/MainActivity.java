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

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import org.thinschema.schemas.JSONDBSchema;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.List;
import java.util.UUID;

/**
 * Test Activity.
 */
public class MainActivity extends Activity {
    DatabaseManager dbManager;

    List<String> fillData;


    private Handler handler;

    public static final String TAG = "THINSCHEMA-TESTS";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        handler = new Handler();

    }


    private String getSchema(int id) {
        try {
            InputStream inputStream = getResources().openRawResource(id);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream), 16 * 1024);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void onRunTest(View view) {
        new Runnable() {
            public void run() {
                clearDBs();
                testEmployees();

                Thread.yield();

                // test upgrades
                testMigrationV1();
                testMigrationV2();
                testMigrationV3();
                testMigrationV4();

                Thread.yield();

                testMigrationDataFillData();
                testMigrationDataMigrate();
            }
        }.run();
    }


    public void appendLog(final String s) {
        handler.post(new Runnable() {
            public void run() {
                TextView tv = (TextView) findViewById(R.id.tv_log);

                tv.append(Html.fromHtml(s));
                tv.append("\n");
            }
        });
    }

    /**
     * Helper method to colorize a String based on the result.
     *
     * @param s      String the get colorized.
     * @param result The result. If true, it's green, otherwise, red.
     * @return
     */
    public String createHtml(String s, boolean result) {
        StringBuilder sb = new StringBuilder();
        sb.append(s);
        if (result) {
            sb.append("<font color=#00ff00>").append(result).append("</font>");
        } else {
            sb.append("<font color=#ff0000>").append(result).append("</font>");
        }
        return sb.toString();
    }

    public void clearDBs() {
        // clear database before testing.
        try {
            JSONDBSchema schema = new JSONDBSchema(new JSONObject(getSchema(R.raw.employees)));
            String dbPath = "data/data/org.thinschema/databases/" + schema.getDatabaseName();
            File file = new File(dbPath);
            file.delete();

            schema = new JSONDBSchema(new JSONObject(getSchema(R.raw.test_migration_table_v1)));
            dbPath = "data/data/org.thinschema/databases/" + schema.getDatabaseName();
            file = new File(dbPath);
            file.delete();

            schema = new JSONDBSchema(new JSONObject(getSchema(R.raw.test_migration_data_v1)));
            dbPath = "data/data/org.thinschema/databases/" + schema.getDatabaseName();
            file = new File(dbPath);
            file.delete();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testEmployees() {
        try {

            if (dbManager != null) {
                dbManager.close();
            }

            JSONObject jsonObject = new JSONObject(getSchema(R.raw.employees));
            dbManager = new DatabaseManager(this, new JSONDBSchema(jsonObject), true);
            SQLiteDatabase db = dbManager.getReadableDatabase();

            StringBuilder sb = new StringBuilder();

            appendLog("TESTING EMPLOYEES DATABASE");
            boolean test = DBValidator.tableExists(db, "Employees");
            appendLog(createHtml("Employee Table: ", test));
            test = DBValidator.tableExists(db, "Products");
            appendLog(createHtml("Products Table: ", test));

            appendLog(createHtml("Employees/_id: ", DBValidator.columnExists(db, "Employees", "_id")));
            appendLog(createHtml("Employees/first_name: ", DBValidator.columnExists(db, "Employees", "first_name")));
            appendLog(createHtml("Employees/last_name: ", DBValidator.columnExists(db, "Employees", "last_name")));

            appendLog(createHtml("Products/_id: ", DBValidator.columnExists(db, "Products", "_id")));
            appendLog(createHtml("Products/product_name: ", DBValidator.columnExists(db, "Products", "product_name")));
            appendLog(createHtml("Products/product_id: ", DBValidator.columnExists(db, "Products", "product_id")));
            appendLog(createHtml("Products/creation_date: ", DBValidator.columnExists(db, "Products", "creation_date")));
            appendLog(createHtml("Products/ship_date: ", DBValidator.columnExists(db, "Products", "ship_date")));
            appendLog(createHtml("Products/customer_name: ", DBValidator.columnExists(db, "Products", "customer_name")));

            db.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testMigrationV1() {
        try {
            JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_table_v1));
            if (dbManager != null) {
                dbManager.close();
            }

            dbManager = new DatabaseManager(this, new JSONDBSchema(jsonObject));
            SQLiteDatabase db = dbManager.getReadableDatabase();

            appendLog("TEST MIGRATION V1");
            appendLog(createHtml("TestMigrationV4/NewTable1 should not exist: ", !DBValidator.tableExists(db, "NewTable1")));
            appendLog(createHtml("TestMigrationV4/NewTable2 should not exist: ", !DBValidator.tableExists(db, "NewTable2")));
            appendLog(createHtml("TestMigrationV1/TestTable: ", DBValidator.tableExists(db, "TestTable")));
            appendLog(createHtml("TestTable/_id: ", DBValidator.columnExists(db, "TestTable", "_id")));
            appendLog(createHtml("TestTable/column1: ", DBValidator.columnExists(db, "TestTable", "column1")));
            appendLog(createHtml("TestTable/column2: ", DBValidator.columnExists(db, "TestTable", "column2")));
            appendLog(createHtml("TestTable/column3 should not exist: ", !DBValidator.columnExists(db, "TestTable", "column3")));

            db.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testMigrationV2() {
        try {
            JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_table_v2));
            if (dbManager != null) {
                dbManager.close();
            }

            dbManager = new DatabaseManager(this, new JSONDBSchema(jsonObject));
            SQLiteDatabase db = dbManager.getWritableDatabase();

            appendLog("TEST MIGRATION V2");
            appendLog(createHtml("TestMigrationV4/NewTable1 should not exist: ", !DBValidator.tableExists(db, "NewTable1")));
            appendLog(createHtml("TestMigrationV4/NewTable2 should not exist: ", !DBValidator.tableExists(db, "NewTable2")));
            appendLog(createHtml("TestMigrationV2/TestTable: ", DBValidator.tableExists(db, "TestTable")));
            appendLog(createHtml("TestTable/_id: ", DBValidator.columnExists(db, "TestTable", "_id")));
            appendLog(createHtml("TestTable/column1: ", DBValidator.columnExists(db, "TestTable", "column1")));
            appendLog(createHtml("TestTable/column2: ", DBValidator.columnExists(db, "TestTable", "column2")));
            appendLog(createHtml("TestTable/column3: ", DBValidator.columnExists(db, "TestTable", "column3")));

            db.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testMigrationV3() {
        try {
            JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_table_v3));
            if (dbManager != null) {
                dbManager.close();
            }

            dbManager = new DatabaseManager(this, new JSONDBSchema(jsonObject));
            SQLiteDatabase db = dbManager.getWritableDatabase();

            appendLog("TEST MIGRATION V3");
            appendLog(createHtml("TestMigrationV4/NewTable1 should not exist: ", !DBValidator.tableExists(db, "NewTable1")));
            appendLog(createHtml("TestMigrationV4/NewTable2 should not exist: ", !DBValidator.tableExists(db, "NewTable2")));
            appendLog(createHtml("TestMigrationV3/TestTable: ", DBValidator.tableExists(db, "TestTable")));
            appendLog(createHtml("TestTable/_id: ", DBValidator.columnExists(db, "TestTable", "_id")));
            appendLog(createHtml("TestTable/column1: ", DBValidator.columnExists(db, "TestTable", "column1")));
            appendLog(createHtml("TestTable/column2: ", DBValidator.columnExists(db, "TestTable", "column2")));
            appendLog(createHtml("TestTable/column3: ", DBValidator.columnExists(db, "TestTable", "column3")));
            appendLog(createHtml("TestTable/column4: ", DBValidator.columnExists(db, "TestTable", "column4")));
            appendLog(createHtml("TestTable/column5: ", DBValidator.columnExists(db, "TestTable", "column5")));
            appendLog(createHtml("TestTable/column6: ", DBValidator.columnExists(db, "TestTable", "column6")));
            appendLog(createHtml("TestTable/column7: ", DBValidator.columnExists(db, "TestTable", "column7")));
            appendLog(createHtml("TestTable/column8: ", DBValidator.columnExists(db, "TestTable", "column8")));
            appendLog(createHtml("TestTable/column9: ", DBValidator.columnExists(db, "TestTable", "column9")));
            appendLog(createHtml("TestTable/column10: ", DBValidator.columnExists(db, "TestTable", "column10")));

            db.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testMigrationV4() {
        try {
            JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_table_v4));
            if (dbManager != null) {
                dbManager.close();
            }

            dbManager = new DatabaseManager(this, new JSONDBSchema(jsonObject));
            SQLiteDatabase db = dbManager.getWritableDatabase();

            appendLog("TEST MIGRATION V4");
            appendLog(createHtml("TestMigrationV4/NewTable1: ", DBValidator.tableExists(db, "NewTable1")));
            appendLog(createHtml("TestMigrationV4/NewTable2: ", DBValidator.tableExists(db, "NewTable2")));
            appendLog(createHtml("TestMigrationV4/TestTable: ", DBValidator.tableExists(db, "TestTable")));
            appendLog(createHtml("TestTable/_id: ", DBValidator.columnExists(db, "TestTable", "_id")));
            appendLog(createHtml("TestTable/column1 should not exist: ", !DBValidator.columnExists(db, "TestTable", "column1")));
            appendLog(createHtml("TestTable/column2 should not exist: ", !DBValidator.columnExists(db, "TestTable", "column2")));
            appendLog(createHtml("TestTable/column3: ", DBValidator.columnExists(db, "TestTable", "column3")));
            appendLog(createHtml("TestTable/column4: ", DBValidator.columnExists(db, "TestTable", "column4")));
            appendLog(createHtml("TestTable/column5: ", DBValidator.columnExists(db, "TestTable", "column5")));

            db.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testMigrationDataFillData() {
        try {
            if (dbManager != null) {
                dbManager.close();
            }

            JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_data_v1));
            dbManager = new DatabaseManager(this, new JSONDBSchema(jsonObject));
            SQLiteDatabase db = dbManager.getWritableDatabase();

            appendLog("TEST DATA MIGRATION - FILL");
            appendLog(createHtml("TestTable/_id: ", DBValidator.columnExists(db, "TestTable", "_id")));
            appendLog(createHtml("TestTable/column1: ", DBValidator.columnExists(db, "TestTable", "column1")));
            appendLog(createHtml("TestTable/column2: ", DBValidator.columnExists(db, "TestTable", "column2")));
            appendLog(createHtml("TestTable/column3 should not exist: ", !DBValidator.columnExists(db, "TestTable", "column3")));

            // fill table with random data
            db.beginTransaction();
            try {
                for (int i = 0; i < 10; ++i) {
                    ContentValues cv = new ContentValues();
                    cv.put("column1", UUID.randomUUID().toString());
                    cv.put("column2", UUID.randomUUID().toString());
                    db.insert("TestTable", null, cv);
                }

                fillData = DBValidator.getValues(db, "TestTable", "column1");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            db.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testMigrationDataMigrate() {
        try {
            if (dbManager != null) {
                dbManager.close();
            }

            JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_data_v2));
            dbManager = new DatabaseManager(this, new JSONDBSchema(jsonObject));
            SQLiteDatabase db = dbManager.getWritableDatabase();

            appendLog("TEST DATA MIGRATION - MIGRATE");
            appendLog(createHtml("TestTable/_id: ", DBValidator.columnExists(db, "TestTable", "_id")));
            appendLog(createHtml("TestTable/column1: ", DBValidator.columnExists(db, "TestTable", "column1")));
            appendLog(createHtml("TestTable/column2 should not exist: ", !DBValidator.columnExists(db, "TestTable", "column2")));
            appendLog(createHtml("TestTable/column3: ", DBValidator.columnExists(db, "TestTable", "column3")));

            List<String> someData = DBValidator.getValues(db, "TestTable", "column1");
            appendLog(createHtml("Is data identical? ", fillData.equals(someData)));

            db.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
