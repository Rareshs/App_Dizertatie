package com.example.app_dizertatie;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {

    private DataBaseHelper db;
    private ListView listViewUserTasks;
    private ArrayList<String> taskList;
    private ArrayList<Integer> taskIds;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialize database and views
        db = new DataBaseHelper(this);
        listViewUserTasks = findViewById(R.id.listViewUserTasks);

        // Get user ID from intent (passed during login)
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user information.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTasks();

        // Mark task as completed on item click
        listViewUserTasks.setOnItemClickListener((parent, view, position, id) -> {
            int taskId = taskIds.get(position);
            showCompletionConfirmationDialog(taskId, position);
        });
    }

    private void loadTasks() {
        taskList = new ArrayList<>();
        taskIds = new ArrayList<>();
        Cursor cursor = db.getPendingTasksByUser(userId); // Only fetch pending tasks

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_TASK_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_TASK_TITLE));
                String details = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_TASK_DETAILS));

                String taskDisplay = title + ": " + details;
                taskList.add(taskDisplay);
                taskIds.add(id);
            } while (cursor.moveToNext());

            cursor.close();
        }

        if (taskList.isEmpty()) {
            Toast.makeText(this, "No tasks assigned to you.", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
        listViewUserTasks.setAdapter(adapter);
    }


    private void showCompletionConfirmationDialog(int taskId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Completion")
                .setMessage("Are you sure you completed this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Mark the task as completed in the database
                    db.markTaskAsCompleted(taskId);

                    // Add the task to the history table
                    db.addHistory(userId, taskId);

                    // Notify the user and reload the tasks
                    Toast.makeText(UserActivity.this, "Task marked as completed.", Toast.LENGTH_SHORT).show();
                    loadTasks();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

}
