
package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "EmployeeDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE employees (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "department TEXT NOT NULL, " +
                "phone TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS employees");
        onCreate(db);
    }

    public boolean insertEmployee(String name, String department, String phone) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("department", department);
        values.put("phone", phone);

        return getWritableDatabase().insert("employees", null, values) != -1;
    }

    public Cursor getEmployees(String keyword) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM employees WHERE name LIKE ? ORDER BY id DESC",
                new String[]{"%" + keyword + "%"}
        );
    }

    public int getEmployeeCount() {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM employees", null);

        int count = 0;

        try {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }

        return count;
    }

    public boolean updateEmployee(int id, String name, String department, String phone) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("department", department);
        values.put("phone", phone);

        return getWritableDatabase().update(
                "employees", values, "id = ?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteEmployee(int id) {
        return getWritableDatabase().delete(
                "employees", "id = ?",
                new String[]{String.valueOf(id)}) > 0;
    }
}
