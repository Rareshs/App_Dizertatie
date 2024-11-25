package com.example.app_dizertatie;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";
    private FirebaseFirestore db; // Firestore instance
    private ListView listViewUserTasks;
    private ArrayList<String> taskList;
    private ArrayList<String> taskIds; // Task IDs from Firestore
    private String userId; // Firebase user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        listViewUserTasks = findViewById(R.id.listViewUserTasks);

        // Get user ID from intent (passed during login)
        userId = getIntent().getStringExtra("userId");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid user information.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTasks();

        // Mark task as completed on item click
        listViewUserTasks.setOnItemClickListener((parent, view, position, id) -> {
            String taskId = taskIds.get(position);
            showCompletionConfirmationDialog(taskId, position);
        });
    }

    private void loadTasks() {
        taskList = new ArrayList<>();
        taskIds = new ArrayList<>();

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

                            // Retrieve task_deadline as Timestamp and convert to formatted String
                            Timestamp deadlineTimestamp = document.getTimestamp("task_deadline");
                            String deadline = "None";
                            if (deadlineTimestamp != null) {
                                // Format the Timestamp into a readable date
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                deadline = dateFormat.format(deadlineTimestamp.toDate());
                            }

                            // Combine task details for display
                            String taskDisplay = title + ": " + details + "\nDeadline: " + deadline;
                            taskList.add(taskDisplay);
                            taskIds.add(id);
                        }

                        // Set the adapter to display tasks in the ListView
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
                        listViewUserTasks.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching tasks", e);
                    Toast.makeText(this, "Error loading tasks.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showCompletionConfirmationDialog(String taskId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Completion")
                .setMessage("Are you sure you completed this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    markTaskAsCompleted(taskId, position);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void markTaskAsCompleted(String taskId, int position) {
        WriteBatch batch = db.batch();

        // Update the task in the "tasks" collection to mark it as completed
        batch.update(db.collection("tasks").document(taskId), "isCompleted", true);

        // Add an entry to the "history" collection for the completed task
        Map<String, Object> historyData = new HashMap<>();
        historyData.put("user_id", userId);
        historyData.put("task_id", taskId);
        historyData.put("timestamp", Timestamp.now());
        historyData.put("task_title", taskList.get(position));

        batch.set(db.collection("history").document(), historyData);

        // Create a notification for the admin
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", "adminId"); // Replace with dynamic admin ID or query the appropriate admin
        notificationData.put("title", "Task Completed");
        notificationData.put("message", "Task '" + taskList.get(position) + "' was completed by user.");
        notificationData.put("taskId", taskId);
        notificationData.put("timestamp", Timestamp.now());
        notificationData.put("isRead", false); // Notification is unread by default

        batch.set(db.collection("notifications").document(), notificationData);

        // Commit the batch write
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Task marked as completed and notification created for admin. Task ID: " + taskId);
                    Toast.makeText(this, "Task marked as completed.", Toast.LENGTH_SHORT).show();

                    // Remove the task from the current list and notify the adapter
                    taskList.remove(position);
                    taskIds.remove(position);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
                    listViewUserTasks.setAdapter(adapter);

                    // Notify if the list is empty after removing the task
                    if (taskList.isEmpty()) {
                        Toast.makeText(this, "No more pending tasks.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking task as completed. Task ID: " + taskId, e);
                    Toast.makeText(this, "Error completing task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
