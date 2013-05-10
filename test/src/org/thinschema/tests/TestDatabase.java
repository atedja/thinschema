package org.thinschema.tests;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.InstrumentationTestCase;
import org.json.JSONException;
import org.json.JSONObject;
import org.thinschema.DatabaseManager;
import org.thinschema.schemas.JSONDBSchema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author atedja
 */
public class TestDatabase extends InstrumentationTestCase {

    Context context;

    @Override
    protected void setUp() throws Exception {
        context = getInstrumentation().getContext();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private String getSchema(int id) {
        try {
            InputStream inputStream = context.getResources().openRawResource(id);
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

    public void testAutoPrimaryKey() throws JSONException {
        JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_auto_primary_key));
        DatabaseManager dbManager = new DatabaseManager(context, new JSONDBSchema(jsonObject), true);
        SQLiteDatabase db = dbManager.getWritableDatabase();

        assertTrue(DBValidator.columnExists(db, "TestTable", "_id"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column1"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column2"));

        db.close();
    }

    public void testEmployees() throws JSONException {
        JSONObject jsonObject = new JSONObject(getSchema(R.raw.employees));
        DatabaseManager dbManager = new DatabaseManager(context, new JSONDBSchema(jsonObject), true);
        SQLiteDatabase db = dbManager.getReadableDatabase();

        StringBuilder sb = new StringBuilder();

        assertTrue(DBValidator.tableExists(db, "Employees"));
        assertTrue(DBValidator.tableExists(db, "Products"));

        assertTrue(DBValidator.columnExists(db, "Employees", "_id"));
        assertTrue(DBValidator.columnExists(db, "Employees", "first_name"));
        assertTrue(DBValidator.columnExists(db, "Employees", "last_name"));

        assertTrue(DBValidator.columnExists(db, "Products", "_id"));
        assertTrue(DBValidator.columnExists(db, "Products", "product_name"));
        assertTrue(DBValidator.columnExists(db, "Products", "product_id"));
        assertTrue(DBValidator.columnExists(db, "Products", "creation_date"));
        assertTrue(DBValidator.columnExists(db, "Products", "ship_date"));
        assertTrue(DBValidator.columnExists(db, "Products", "customer_name"));

        db.close();
    }

    public void testMigrationV1() throws JSONException {
        JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_table_v1));
        DatabaseManager dbManager = new DatabaseManager(context, new JSONDBSchema(jsonObject), true);
        SQLiteDatabase db = dbManager.getReadableDatabase();

        assertFalse(DBValidator.tableExists(db, "NewTable1"));
        assertFalse(DBValidator.tableExists(db, "NewTable2"));
        assertTrue(DBValidator.tableExists(db, "TestTable"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "_id"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column1"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column2"));
        assertFalse(DBValidator.columnExists(db, "TestTable", "column3"));

        db.close();
    }

    public void testMigrationV2() throws JSONException {
        JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_table_v2));
        DatabaseManager dbManager = new DatabaseManager(context, new JSONDBSchema(jsonObject), true);
        SQLiteDatabase db = dbManager.getWritableDatabase();

        assertFalse(DBValidator.tableExists(db, "NewTable1"));
        assertFalse(DBValidator.tableExists(db, "NewTable2"));
        assertTrue(DBValidator.tableExists(db, "TestTable"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "_id"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column1"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column2"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column3"));

        db.close();
    }

    public void testMigrationV3() throws JSONException {
        JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_table_v3));
        DatabaseManager dbManager = new DatabaseManager(context, new JSONDBSchema(jsonObject), true);
        SQLiteDatabase db = dbManager.getWritableDatabase();

        assertFalse(DBValidator.tableExists(db, "NewTable1"));
        assertFalse(DBValidator.tableExists(db, "NewTable2"));
        assertTrue(DBValidator.tableExists(db, "TestTable"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "_id"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column1"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column2"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column3"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column4"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column5"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column6"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column7"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column8"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column9"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column10"));

        db.close();
    }

    public void testMigrationV4() throws JSONException {
        JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_table_v4));
        DatabaseManager dbManager = new DatabaseManager(context, new JSONDBSchema(jsonObject), true);
        SQLiteDatabase db = dbManager.getWritableDatabase();

        assertTrue(DBValidator.tableExists(db, "NewTable1"));
        assertTrue(DBValidator.tableExists(db, "NewTable2"));
        assertTrue(DBValidator.tableExists(db, "TestTable"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "_id"));
        assertFalse(DBValidator.columnExists(db, "TestTable", "column1"));
        assertFalse(DBValidator.columnExists(db, "TestTable", "column2"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column3"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column4"));
        assertTrue(DBValidator.columnExists(db, "TestTable", "column5"));

        db.close();
    }

//    public void testMigrationDataFillData() {
//        JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_data_v1));
//        DatabaseManager dbManager = new DatabaseManager(context, new JSONDBSchema(jsonObject), true);
//        SQLiteDatabase db = dbManager.getWritableDatabase();
//
//        assertTrue(DBValidator.columnExists(db, "TestTable", "_id"));
//        assertTrue(DBValidator.columnExists(db, "TestTable", "column1"));
//        assertTrue(DBValidator.columnExists(db, "TestTable", "column2"));
//        assertFalse(DBValidator.columnExists(db, "TestTable", "column3"));
//
//        // fill table with random data
//        db.beginTransaction();
//        try {
//            for (int i = 0; i < 10; ++i) {
//                ContentValues cv = new ContentValues();
//                cv.put("column1", UUID.randomUUID().toString());
//                cv.put("column2", UUID.randomUUID().toString());
//                db.insert("TestTable", null, cv);
//            }
//
//            List<String> fillData = DBValidator.getValues(db, "TestTable", "column1");
//            db.setTransactionSuccessful();
//        } finally {
//            db.endTransaction();
//        }
//
//        db.close();
//    }
//
//    public void testMigrationDataMigrate() throws JSONException {
//        JSONObject jsonObject = new JSONObject(getSchema(R.raw.test_migration_data_v2));
//        DatabaseManager dbManager = new DatabaseManager(context, new JSONDBSchema(jsonObject), true);
//        SQLiteDatabase db = dbManager.getWritableDatabase();
//
//        assertTrue(DBValidator.columnExists(db, "TestTable", "_id"));
//        assertTrue(DBValidator.columnExists(db, "TestTable", "column1"));
//        assertFalse(DBValidator.columnExists(db, "TestTable", "column2"));
//        assertTrue(DBValidator.columnExists(db, "TestTable", "column3"));
//
//        List<String> someData = DBValidator.getValues(db, "TestTable", "column1");
//        appendLog(createHtml("Is data identical? ", fillData.equals(someData)));
//
//        db.close();
//    }
}
