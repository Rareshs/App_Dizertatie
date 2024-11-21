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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity"; // For logging
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

                            String taskDisplay = title + ": " + details;
                            taskList.add(taskDisplay);
                            taskIds.add(id);
                        }

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
                    markTaskAsCompleted(taskId);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void markTaskAsCompleted(String taskId) {
        WriteBatch batch = db.batch();

        // Get the index of the task in the taskList
        int taskIndex = taskIds.indexOf(taskId);
        if (taskIndex == -1) {
            Log.e(TAG, "Task ID not found in task list");
            return;
        }

        // Update the task in the "tasks" collection to mark it as completed
        batch.update(db.collection("tasks").document(taskId), "isCompleted", true);

        // Add an entry to the "history" collection for the completed task
        Map<String, Object> historyData = new HashMap<>();
        historyData.put("user_id", userId);
        historyData.put("task_id", taskId);
        historyData.put("timestamp", Timestamp.now()); // Use Firestore Timestamp
        historyData.put("task_title", taskList.get(taskIndex)); // Use the task title from the list

        batch.set(db.collection("history").document(), historyData);

        // Commit the batch write
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Task marked as completed and history updated for taskId: " + taskId);
                    Toast.makeText(this, "Task marked as completed.", Toast.LENGTH_SHORT).show();

                    // Remove the task from the current list and notify the adapter
                    taskList.remove(taskIndex);
                    taskIds.remove(taskIndex);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
                    listViewUserTasks.setAdapter(adapter);

                    // Optionally notify if the list is empty after removing the task
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
