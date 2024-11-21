package com.example.app_dizertatie;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private FirebaseFirestore db; // Firestore instance
    private ListView listViewUsers;
    private ArrayList<String> userList;
    private ArrayList<String> userIdList; // To hold Firestore document IDs
    private String departmentName; // Admin's department name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        listViewUsers = findViewById(R.id.listViewUsers);

        // Get data from intent
        String adminDepartmentId = getIntent().getStringExtra("departmentId");
        departmentName = getIntent().getStringExtra("departmentName");

        // Set admin welcome message
        TextView textViewAdminWelcome = findViewById(R.id.textViewAdminWelcome);
        if (departmentName != null) {
            textViewAdminWelcome.setText("Welcome, Admin of " + departmentName);
        } else {
            textViewAdminWelcome.setText("Welcome, Admin of Unknown Department");
        }

        if (adminDepartmentId == null || adminDepartmentId.isEmpty()) {
            Toast.makeText(this, "Invalid department information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load users in the admin's department
        loadUsers(adminDepartmentId);

        // Handle user click in ListView
        listViewUsers.setOnItemClickListener((adapterView, view, position, id) -> {
            String userId = userIdList.get(position);
            if (userId.equals("NO_VALID_USER")) {
                Toast.makeText(AdminActivity.this, "No valid user selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navigate to UserTasksActivity
            Intent intent = new Intent(AdminActivity.this, UserTasksActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("departmentId", adminDepartmentId); // Pass department ID
            intent.putExtra("departmentName", departmentName); // Pass department name
            startActivity(intent);
        });
    }

    private void loadUsers(String departmentId) {
        // Query Firestore for users in the specified department
        db.collection("users")
                .whereEqualTo("department_id", departmentId)
                .whereEqualTo("role", "User") // Only retrieve users with the "User" role
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList = new ArrayList<>();
                    userIdList = new ArrayList<>();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userId = document.getId(); // Firestore document ID
                            String username = document.getString("username");

                            if (username != null && !username.isEmpty()) {
                                userIdList.add(userId);
                                userList.add(username);
                            } else {
                                userList.add("Unknown User");
                                userIdList.add("NO_VALID_USER");
                            }
                        }
                    } else {
                        userList.add("No users found in your department");
                        userIdList.add("NO_VALID_USER"); // Placeholder for no valid user
                    }

                    // Populate ListView with user data
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
                    listViewUsers.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Error loading users", Toast.LENGTH_SHORT).show();
                });
    }
}

