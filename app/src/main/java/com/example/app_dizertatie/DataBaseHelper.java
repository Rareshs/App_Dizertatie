package com.example.app_dizertatie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 2;

    // Shared columns
    public static final String COLUMN_DEPARTMENT_ID = "department_id";  // Shared between users and departments tables

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role";  // Admin or User

    // Departments table
    public static final String TABLE_DEPARTMENTS = "departments";
    public static final String COLUMN_DEPARTMENT_NAME = "department_name";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Departments Table
        String CREATE_DEPARTMENTS_TABLE = "CREATE TABLE " + TABLE_DEPARTMENTS + "("
                + COLUMN_DEPARTMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DEPARTMENT_NAME + " TEXT UNIQUE)";
        db.execSQL(CREATE_DEPARTMENTS_TABLE);

        // Create Users Table with department ID as foreign key
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_ROLE + " TEXT, "
                + COLUMN_DEPARTMENT_ID + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_DEPARTMENT_ID + ") REFERENCES " + TABLE_DEPARTMENTS + "(" + COLUMN_DEPARTMENT_ID + "))";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPARTMENTS);
        onCreate(db);
    }

    // Method to add a new department
    public long addDepartment(String departmentName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEPARTMENT_NAME, departmentName);

        long result = db.insert(TABLE_DEPARTMENTS, null, values);
        db.close();
        return result;
    }

    // Method to add a new user with department ID
    public void addUser(String username, String password, String role, Integer departmentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);

        // Only add department ID if the user is not an admin
        if (departmentId != null) {
            values.put(COLUMN_DEPARTMENT_ID, departmentId);
        }

        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    // Method to check if any admins exist
    public boolean hasAdmins() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID}, COLUMN_ROLE + "=?", new String[]{"Admin"}, null, null, null);
        boolean hasAdmin = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return hasAdmin;
    }

    // Method to check if any departments exist
    public boolean hasDepartments() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DEPARTMENTS, new String[]{COLUMN_DEPARTMENT_ID}, null, null, null, null, null);
        boolean hasDepartment = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return hasDepartment;
    }

    // Method to get all departments (for populating department dropdowns)
    public Cursor getAllDepartments() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_DEPARTMENTS, new String[]{COLUMN_DEPARTMENT_ID, COLUMN_DEPARTMENT_NAME},
                null, null, null, null, COLUMN_DEPARTMENT_NAME);
    }

    // Method to retrieve department ID by name (for assigning users to departments)
    public int getDepartmentId(String departmentName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DEPARTMENTS, new String[]{COLUMN_DEPARTMENT_ID},
                COLUMN_DEPARTMENT_NAME + "=?", new String[]{departmentName}, null, null, null);

        int departmentId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            departmentId = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return departmentId;
    }

    // Method to validate login and retrieve role
    public String getUserRole(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ROLE},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(0);
            cursor.close();
            return role;
        }
        return null;  // User not found
    }

    // Method to check if a user exists based on username and role
    public boolean userExists(String username, String role) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_ROLE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, role});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return exists;
    }
}
