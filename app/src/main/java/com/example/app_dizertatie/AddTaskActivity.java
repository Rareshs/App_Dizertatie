package com.example.app_dizertatie;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddTaskActivity extends AppCompatActivity {

    private DataBaseHelper db;
    private int userId;
    private String username;
    private int departmentId;

    private TextView textViewUsername;
    private EditText editTextTaskDescription, editTextDeadline;
    private Button buttonAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        db = new DataBaseHelper(this);

        // Retrieve userId, username, and departmentId from intent
        userId = getIntent().getIntExtra("userId", -1);
        username = getIntent().getStringExtra("username");
        departmentId = getIntent().getIntExtra("departmentId", -1);

        // Log values to debug
        Log.d("AddTaskActivity", "Received userId: " + userId + ", username: " + username + ", departmentId: " + departmentId);

        // Check for invalid data
        if (userId == -1 || username == null || username.isEmpty() || departmentId == -1) {
            Log.e("AddTaskActivity", "Invalid data passed to AddTaskActivity");
            Toast.makeText(this, "Invalid user or department information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        textViewUsername = findViewById(R.id.textViewUsername);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        editTextDeadline = findViewById(R.id.editTextDeadline);
        buttonAddTask = findViewById(R.id.buttonAddTask);

        // Display username
        textViewUsername.setText("Assigning task to: " + username);

        // Add task button functionality
        buttonAddTask.setOnClickListener(v -> {
            String taskDescription = editTextTaskDescription.getText().toString().trim();
            String deadline = editTextDeadline.getText().toString().trim();

            // Validate input
            if (taskDescription.isEmpty()) {
                Toast.makeText(AddTaskActivity.this, "Task description cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Insert task into database
            long result = db.addTaskWithDeadline("Task Title", taskDescription, deadline.isEmpty() ? null : deadline, userId, departmentId);
            if (result == -1) {
                Log.e("AddTaskActivity", "Failed to insert task into database");
                Toast.makeText(AddTaskActivity.this, "Failed to add task. Try again.", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("AddTaskActivity", "Task added successfully for userId: " + userId);
                Toast.makeText(AddTaskActivity.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                finish(); // Return to UserTasksActivity
            }
        });
    }
}
