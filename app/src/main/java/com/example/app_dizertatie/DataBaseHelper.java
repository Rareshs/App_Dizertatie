package com.example.app_dizertatie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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

    // Tasks table
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_TASK_ID = "task_id";
    public static final String COLUMN_TASK_TITLE = "task_title";
    public static final String COLUMN_TASK_DETAILS = "task_details";
    public static final String COLUMN_TASK_DEADLINE = "task_deadline"; // Optional deadline
    public static final String COLUMN_ASSIGNED_USER_ID = "assigned_user_id";
    private static final String COLUMN_TASK_COMPLETED = "isCompleted";

    //History task table
    private static final String TABLE_HISTORY = "history";
    private static final String COLUMN_HISTORY_ID = "history_id";
    private static final String COLUMN_HISTORY_USER_ID = "user_id";
    private static final String COLUMN_HISTORY_TASK_ID = "task_id";
    public static final String COLUMN_HISTORY_TIMESTAMP = "timestamp"; // Replace "timestamp" with the actual column name in your history table


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
        // Create Tasks Table
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TASK_TITLE + " TEXT, "
                + COLUMN_TASK_DETAILS + " TEXT, "
                + COLUMN_TASK_DEADLINE + " DATE DEFAULT NULL, "  // Optional deadline
                + COLUMN_ASSIGNED_USER_ID + " INTEGER, "
                + COLUMN_DEPARTMENT_ID + " INTEGER, "
                +  COLUMN_TASK_COMPLETED + " INTEGER DEFAULT 0, "
                + "FOREIGN KEY(" + COLUMN_ASSIGNED_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), "
                + "FOREIGN KEY(" + COLUMN_DEPARTMENT_ID + ") REFERENCES " + TABLE_DEPARTMENTS + "(" + COLUMN_DEPARTMENT_ID + "))";
        db.execSQL(CREATE_TASKS_TABLE);
        String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + "("
                + COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_HISTORY_USER_ID + " INTEGER, "
                + COLUMN_HISTORY_TASK_ID + " INTEGER, "
                + COLUMN_TASK_TITLE + " TEXT, " // Add this line
                + COLUMN_HISTORY_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY(" + COLUMN_HISTORY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), "
                + "FOREIGN KEY(" + COLUMN_HISTORY_TASK_ID + ") REFERENCES " + TABLE_TASKS + "(" + COLUMN_TASK_ID + "))";
        db.execSQL(CREATE_HISTORY_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPARTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS); // Drop tasks table
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

        if (departmentId != null) {
            values.put(COLUMN_DEPARTMENT_ID, departmentId);
            Log.d("Database", "Adding user with departmentId: " + departmentId);
        } else {
            Log.d("Database", "Adding user without departmentId (admin): " + username);
        }

        long result = db.insert(TABLE_USERS, null, values);
        if (result == -1) {
            Log.e("Database", "Failed to insert user: " + username);
        } else {
            Log.d("Database", "User inserted successfully: " + username);
        }

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
    public Cursor getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COLUMN_USERNAME + "=?",
                new String[]{username}, null, null, null);
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
    public int getAdminDepartmentId(String adminUsername) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_DEPARTMENT_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_ROLE + "=?",
                new String[]{adminUsername, "Admin"}, null, null, null);

        int departmentId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            departmentId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DEPARTMENT_ID));
            cursor.close();
        }
        db.close();
        return departmentId;
    }
    public String getDepartmentName(int departmentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DEPARTMENTS, new String[]{COLUMN_DEPARTMENT_NAME},
                COLUMN_DEPARTMENT_ID + "=?", new String[]{String.valueOf(departmentId)},
                null, null, null);

        String departmentName = null;
        if (cursor != null && cursor.moveToFirst()) {
            departmentName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEPARTMENT_NAME));
            cursor.close();
        }
        db.close();
        return departmentName;
    }
    public String getTaskCompletedColumn() {
        return COLUMN_TASK_COMPLETED;
    }

    public Cursor getUsersByDepartment(int departmentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null,
                COLUMN_DEPARTMENT_ID + "=? AND " + COLUMN_ROLE + "=?",
                new String[]{String.valueOf(departmentId), "User"},
                null, null, COLUMN_USERNAME);
    }
    public long addTaskWithDeadline(String title, String details, String deadline, int userId, int departmentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, title);
        values.put(COLUMN_TASK_DETAILS, details);
        if (deadline != null) {
            values.put(COLUMN_TASK_DEADLINE, deadline); // Only set deadline if provided
        }
        values.put(COLUMN_ASSIGNED_USER_ID, userId);
        values.put(COLUMN_DEPARTMENT_ID, departmentId);
        long result = db.insert(TABLE_TASKS, null, values); // Return the row ID or -1 if failed
        db.close();
        return result;
    }
    public Cursor getTasksByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TASKS, null, COLUMN_ASSIGNED_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, COLUMN_TASK_DEADLINE + " ASC");
    }
    public Cursor getTasksByDepartment(int departmentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TASKS, null, COLUMN_DEPARTMENT_ID + "=?",
                new String[]{String.valueOf(departmentId)}, null, null, COLUMN_TASK_DEADLINE + " ASC");
    }
    public Cursor getOverdueTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_TASK_DEADLINE + " < DATE('now')";
        return db.rawQuery(query, null);
    }
    public void addHistory(int userId, int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Fetch task title from the tasks table
        String taskTitle = null;
        Cursor cursor = db.query(TABLE_TASKS, new String[]{COLUMN_TASK_TITLE},
                COLUMN_TASK_ID + "=?", new String[]{String.valueOf(taskId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            taskTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE));
            cursor.close();
        }

        if (taskTitle != null) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_HISTORY_USER_ID, userId);
            values.put(COLUMN_HISTORY_TASK_ID, taskId);
            values.put(COLUMN_TASK_TITLE, taskTitle); // Insert the task title into the history table

            long result = db.insert(TABLE_HISTORY, null, values);
            if (result == -1) {
                Log.e("Database", "Failed to insert history for userId: " + userId + ", taskId: " + taskId + ", taskTitle: " + taskTitle);
            } else {
                Log.d("Database", "History inserted successfully: UserId=" + userId + ", TaskId=" + taskId + ", TaskTitle=" + taskTitle);
            }
        } else {
            Log.e("Database", "Task title is null for TaskId=" + taskId);
        }

        db.close();
    }



    public Cursor getTaskHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_HISTORY, null, null, null, null, null, COLUMN_HISTORY_TIMESTAMP + " DESC");
    }
    public Cursor getPendingTasksByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TASKS, null,
                COLUMN_ASSIGNED_USER_ID + "=? AND " + COLUMN_TASK_COMPLETED + "=0",
                new String[]{String.valueOf(userId)}, null, null, COLUMN_TASK_DEADLINE + " ASC");
    }
    public Cursor getTaskHistoryByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_TASK_TITLE + ", " + COLUMN_HISTORY_TIMESTAMP +
                " FROM " + TABLE_HISTORY +
                " WHERE " + COLUMN_HISTORY_USER_ID + "=? " +
                " ORDER BY " + COLUMN_HISTORY_TIMESTAMP + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null) {
            Log.d("Database", "Fetched history for userId=" + userId + ", Count=" + cursor.getCount());
        } else {
            Log.e("Database", "Failed to fetch history for userId=" + userId);
        }

        return cursor;
    }



    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);
    }

    public void markTaskAsCompleted(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_COMPLETED, 1); // Mark as completed
        db.update(TABLE_TASKS, values, COLUMN_TASK_ID + "=?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public void debugUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                String role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE));
                int departmentId = cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_DEPARTMENT_ID)) ? -1 : cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DEPARTMENT_ID));

                Log.d("DatabaseDebug", "User ID: " + id + ", Username: " + username + ", Role: " + role + ", Department ID: " + departmentId);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }
}
