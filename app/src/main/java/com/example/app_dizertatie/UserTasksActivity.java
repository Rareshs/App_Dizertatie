package com.example.app_dizertatie;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class UserTasksActivity extends AppCompatActivity {

    private static final String TAG = "UserTasksActivity"; // For logging
    private DataBaseHelper db;
    private ListView listViewTasks;
    private Button buttonAddTask, buttonViewHistory; // Added buttonViewHistory
    private ArrayList<String> taskList;
    private int userId;
    private int departmentId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tasks);

        db = new DataBaseHelper(this);
        listViewTasks = findViewById(R.id.listViewTasks);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonViewHistory = findViewById(R.id.buttonViewHistory); // Initialize view history button

        // Retrieve data from Intent
        userId = getIntent().getIntExtra("userId", -1);
        departmentId = getIntent().getIntExtra("departmentId", -1);

        Log.d(TAG, "Received userId: " + userId);
        Log.d(TAG, "Received departmentId: " + departmentId);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user information", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Invalid userId passed to UserTasksActivity");
            finish();
            return;
        }

        if (departmentId == -1) {
            Log.d(TAG, "departmentId not passed via Intent, fetching from database");
            Cursor cursor = db.getUserById(userId);
            if (cursor != null && cursor.moveToFirst()) {
                departmentId = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_DEPARTMENT_ID));
                username = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_USERNAME)); // Fetch username
                Log.d(TAG, "Fetched departmentId from database: " + departmentId);
                Log.d(TAG, "Fetched username from database: " + username);
                cursor.close();
            } else {
                Log.e(TAG, "Failed to fetch departmentId and username from database");
            }
        }

        if (departmentId == -1 || username == null || username.isEmpty()) {
            Toast.makeText(this, "Failed to retrieve user or department information", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Invalid departmentId or username after retrieval");
            finish();
            return;
        }

        loadTasks();

        // Add task button functionality
        buttonAddTask.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to AddTaskActivity with userId: " + userId + ", departmentId: " + departmentId + ", username: " + username);
            Intent intent = new Intent(UserTasksActivity.this, AddTaskActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("departmentId", departmentId); // Pass departmentId
            intent.putExtra("username", username); // Pass username
            startActivity(intent);
        });

        // View history button functionality
        buttonViewHistory.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to AdminTaskHistoryActivity for userId: " + userId);
            Intent intent = new Intent(UserTasksActivity.this, AdminTaskHistoryActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload the tasks whenever the activity is resumed
        loadTasks();
    }

    private void loadTasks() {
        Log.d(TAG, "Loading tasks for userId: " + userId);
        Cursor cursor = db.getPendingTasksByUser(userId); // Use a new query method for pending tasks

        taskList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String taskTitle = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_TASK_TITLE));
                String taskDetails = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_TASK_DETAILS));
                String taskDeadline = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_TASK_DEADLINE));

                if (taskDeadline == null || taskDeadline.isEmpty()) {
                    taskList.add(taskTitle + ": " + taskDetails + " (No Deadline)");
                } else {
                    taskList.add(taskTitle + ": " + taskDetails + " (Deadline: " + taskDeadline + ")");
                }

                Log.d(TAG, "Loaded task: " + taskTitle + ", Details: " + taskDetails + ", Deadline: " + taskDeadline);
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Log.d(TAG, "No tasks found for userId: " + userId);
        }

        if (taskList.isEmpty()) {
            Toast.makeText(this, "No pending tasks for this user", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
        listViewTasks.setAdapter(adapter);
    }

}
