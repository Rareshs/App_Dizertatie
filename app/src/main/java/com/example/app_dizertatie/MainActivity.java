package com.example.app_dizertatie;

import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private DataBaseHelper db;
    private EditText editTextUsername, editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Database Helper
        db = new DataBaseHelper(this);

        // Debug the users in the database
        db.debugUsers();

        // Find Views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        // Set Login Button Click Listener
        buttonLogin.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            // Debug login credentials
            Log.d("MainActivity", "Attempting login with Username: " + username + ", Password: " + password);

            // Check user role in the database
            String role = db.getUserRole(username, password);
            Log.d("MainActivity", "Role retrieved for user: " + role);

            if (role != null) {
                if (role.equals("Admin")) {
                    // Retrieve the department ID for the logged-in admin
                    int adminDepartmentId = db.getAdminDepartmentId(username);
                    String departmentName = db.getDepartmentName(adminDepartmentId);

                    // Debug the department information
                    Log.d("MainActivity", "Admin Department ID: " + adminDepartmentId + ", Department Name: " + departmentName);

                    if (adminDepartmentId != -1) {
                        // Pass department info to AdminActivity
                        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                        intent.putExtra("departmentId", adminDepartmentId);
                        intent.putExtra("departmentName", departmentName);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to retrieve department information", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("MainActivity", "Navigating to UserActivity for user: " + username);
                    startActivity(new Intent(MainActivity.this, UserActivity.class));
                    finish();
                }
            } else {
                Log.d("MainActivity", "Invalid credentials for Username: " + username);
                Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        // Set Register Button Click Listener
        buttonRegister.setOnClickListener(v -> {
            // Redirect to Register Activity
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }

}