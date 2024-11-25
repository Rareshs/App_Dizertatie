package com.example.app_dizertatie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class UserTasksActivity extends AppCompatActivity {

    private static final String TAG = "UserTasksActivity";
    private FirebaseFirestore db;
    private ListView listViewTasks;
    private Button buttonAddTask, buttonViewHistory;
    private ArrayList<String> taskList;
    private ArrayList<String> taskIds; // For storing task IDs
    private String userId;
    private String departmentId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tasks);

        // Initialize Firestore and views
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

        // Load tasks for the user
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
        // Clear the lists before loading new data
        if (taskList == null) {
            taskList = new ArrayList<>();
        } else {
            taskList.clear(); // Clear the existing tasks
        }

        if (taskIds == null) {
            taskIds = new ArrayList<>();
        } else {
            taskIds.clear(); // Clear the existing task IDs
        }

        Log.d(TAG, "Loading tasks for userId: " + userId);

        // Query Firestore for tasks assigned to the user and not completed
        db.collection("tasks")
                .whereEqualTo("assigned_user_id", userId)
                .whereEqualTo("isCompleted", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No tasks assigned to userId: " + userId);
                        Toast.makeText(this, "No tasks assigned to you.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String id = document.getId();
                            String title = document.getString("task_title");
                            String details = document.getString("task_details");

                            // Handle task_deadline as a Timestamp
                            Timestamp deadlineTimestamp = document.getTimestamp("task_deadline");
                            String deadline = "None";
                            if (deadlineTimestamp != null) {
                                // Format the Timestamp into a readable date
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                deadline = dateFormat.format(deadlineTimestamp.toDate());
                            }

                            // Combine task details for display
                            String taskDisplay = title + ": " + details + "\nDeadline: " + deadline;

                            // Add the task to the lists (avoid duplication)
                            if (!taskIds.contains(id)) { // Ensure task is not already added
                                taskList.add(taskDisplay);
                                taskIds.add(id);
                            }
                        }

                        // Set the adapter to display tasks in the ListView
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
                        listViewTasks.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching tasks", e);
                    Toast.makeText(this, "Error loading tasks.", Toast.LENGTH_SHORT).show();
                });
    }


}
