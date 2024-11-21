package com.example.app_dizertatie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class UserTasksActivity extends AppCompatActivity {

    private static final String TAG = "UserTasksActivity";
    private FirebaseFirestore db;
    private ListView listViewTasks;
    private Button buttonAddTask, buttonViewHistory;
    private ArrayList<String> taskList;
    private String userId;
    private String departmentId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tasks);

        db = FirebaseFirestore.getInstance();
        listViewTasks = findViewById(R.id.listViewTasks);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonViewHistory = findViewById(R.id.buttonViewHistory);

        // Retrieve data from Intent
        userId = getIntent().getStringExtra("userId");
        departmentId = getIntent().getStringExtra("departmentId");
        username = getIntent().getStringExtra("username");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid user information", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Invalid userId passed to UserTasksActivity");
            finish();
            return;
        }

        // Fetch departmentId and username if missing
        if (departmentId == null || username == null) {
            Log.d(TAG, "Fetching departmentId and username for userId: " + userId);

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            departmentId = documentSnapshot.getString("department_id");
                            username = documentSnapshot.getString("username");

                            if (departmentId == null || username == null) {
                                Log.e(TAG, "Failed to retrieve departmentId or username for userId: " + userId);
                                Toast.makeText(this, "Failed to load user or department information.", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Log.d(TAG, "Fetched departmentId: " + departmentId + ", username: " + username);
                            }
                        } else {
                            Log.e(TAG, "User document not found for userId: " + userId);
                            Toast.makeText(this, "Invalid user information.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user details for userId: " + userId, e);
                        Toast.makeText(this, "Error fetching user information.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }

        loadTasks();

        // Add task button functionality
        buttonAddTask.setOnClickListener(v -> {
            if (departmentId == null || username == null) {
                Log.e(TAG, "Missing departmentId or username. Cannot navigate to AddTaskActivity.");
                Toast.makeText(this, "Cannot add task. Missing user or department information.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Navigating to AddTaskActivity with userId: " + userId + ", departmentId: " + departmentId + ", username: " + username);
            Intent intent = new Intent(UserTasksActivity.this, AddTaskActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("departmentId", departmentId);
            intent.putExtra("username", username);
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
        // Reload tasks whenever the activity is resumed
        loadTasks();
    }

    private void loadTasks() {
        Log.d(TAG, "Loading tasks for userId: " + userId);

        db.collection("tasks")
                .whereEqualTo("assigned_user_id", userId)
                .whereEqualTo("isCompleted", false) // Load only pending tasks
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String taskTitle = document.getString("task_title");
                        String taskDetails = document.getString("task_details");
                        String taskDeadline = document.getString("task_deadline");

                        if (taskDeadline == null || taskDeadline.isEmpty()) {
                            taskList.add(taskTitle + ": " + taskDetails + " (No Deadline)");
                        } else {
                            taskList.add(taskTitle + ": " + taskDetails + " (Deadline: " + taskDeadline + ")");
                        }

                        Log.d(TAG, "Loaded task: " + taskTitle + ", Details: " + taskDetails + ", Deadline: " + taskDeadline);
                    }

                    if (taskList.isEmpty()) {
                        Toast.makeText(this, "No pending tasks for this user", Toast.LENGTH_SHORT).show();
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
                    listViewTasks.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading tasks", e);
                    Toast.makeText(this, "Error loading tasks", Toast.LENGTH_SHORT).show();
                });
    }
}
