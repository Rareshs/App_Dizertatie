package com.example.app_dizertatie;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {

    private static final String TAG = "AddTaskActivity";
    private FirebaseFirestore db; // Firestore instance
    private String userId;
    private String username;
    private String departmentId;

    private TextView textViewUsername;
    private EditText editTextTaskTitle, editTextTaskDescription, editTextDeadline;
    private Button buttonAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve userId, username, and departmentId from intent
        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");
        departmentId = getIntent().getStringExtra("departmentId");

        // Log values for debugging
        Log.d(TAG, "Received userId: " + userId + ", username: " + username + ", departmentId: " + departmentId);

        // Check for invalid data
        if (userId == null || userId.isEmpty() || username == null || username.isEmpty() || departmentId == null || departmentId.isEmpty()) {
            Log.e(TAG, "Invalid data passed to AddTaskActivity");
            Toast.makeText(this, "Invalid user or department information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        textViewUsername = findViewById(R.id.textViewUsername);
        editTextTaskTitle = findViewById(R.id.editTextTaskTitleEditText);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        editTextDeadline = findViewById(R.id.editTextDeadline);
        buttonAddTask = findViewById(R.id.buttonAddTask);

        // Display username
        textViewUsername.setText("Assigning task to: " + username);

        // Add task button functionality
        buttonAddTask.setOnClickListener(v -> {
            String taskTitle = editTextTaskTitle.getText().toString().trim();
            String taskDescription = editTextTaskDescription.getText().toString().trim();
            String deadline = editTextDeadline.getText().toString().trim();

            // Validate input
            if (taskTitle.isEmpty()) {
                Toast.makeText(AddTaskActivity.this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (taskDescription.isEmpty()) {
                Toast.makeText(AddTaskActivity.this, "Task description cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse the deadline if provided
            Timestamp deadlineTimestamp = null;
            if (!deadline.isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date parsedDate = dateFormat.parse(deadline);
                    deadlineTimestamp = new Timestamp(parsedDate);
                } catch (ParseException e) {
                    Log.e(TAG, "Invalid deadline format", e);
                    Toast.makeText(AddTaskActivity.this, "Invalid deadline format. Use yyyy-MM-dd.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Prepare task data
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("task_title", taskTitle);
            taskData.put("task_details", taskDescription);
            taskData.put("task_deadline", deadlineTimestamp); // Use null if no deadline
            taskData.put("assigned_user_id", userId);
            taskData.put("department_id", departmentId);
            taskData.put("isCompleted", false); // Default to incomplete

            // Insert task into Firestore
            db.collection("tasks")
                    .add(taskData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Task added successfully with ID: " + documentReference.getId());
                        Toast.makeText(AddTaskActivity.this, "Task added successfully", Toast.LENGTH_SHORT).show();

                        // Create a notification for the assigned user
                        createNotificationForUser(userId, "New Task Assigned", "You have been assigned the task: " + taskTitle);

                        finish(); // Return to UserTasksActivity
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to add task", e);
                        Toast.makeText(AddTaskActivity.this, "Failed to add task. Try again.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void createNotificationForUser(String userId, String title, String message) {
        // Prepare notification data
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("timestamp", Timestamp.now());
        notificationData.put("isRead", false); // Notification is unread by default

        // Add notification to Firestore
        db.collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Notification created successfully for userId: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to create notification", e));
    }
}
