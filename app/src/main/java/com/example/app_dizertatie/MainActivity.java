package com.example.app_dizertatie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth; // Firebase Authentication instance
    private FirebaseFirestore db; // Firestore instance
    private EditText editTextEmail, editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find Views
        editTextEmail = findViewById(R.id.editTextUsername); // Ensure this matches XML
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        // Login Button Click Listener
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Validate input fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(MainActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Log in user using FirebaseAuth
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();

                        // Fetch additional user details (role, department, etc.) from Firestore
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String role = documentSnapshot.getString("role");
                                        Log.d("MainActivity", "User role: " + role);

                                        if (role != null) {
                                            if (role.equals("Admin")) {
                                                String departmentId = documentSnapshot.getString("department_id");

                                                if (departmentId != null) {
                                                    // Fetch department name
                                                    db.collection("departments").document(departmentId).get()
                                                            .addOnSuccessListener(departmentDoc -> {
                                                                if (departmentDoc.exists()) {
                                                                    String departmentName = departmentDoc.getString("department_name");
                                                                    Log.d("MainActivity", "Admin logged in with Department ID: " + departmentId + " and Name: " + departmentName);

                                                                    // Navigate to AdminActivity with department details
                                                                    Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                                                                    intent.putExtra("departmentId", departmentId);
                                                                    intent.putExtra("departmentName", departmentName);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    Log.e("MainActivity", "Department not found for departmentId: " + departmentId);
                                                                    Toast.makeText(MainActivity.this, "Failed to fetch department details", Toast.LENGTH_SHORT).show();
                                                                }
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("MainActivity", "Error fetching department details", e);
                                                                Toast.makeText(MainActivity.this, "Error fetching department details", Toast.LENGTH_SHORT).show();
                                                            });
                                                } else {
                                                    Log.e("MainActivity", "Admin missing department ID");
                                                    Toast.makeText(MainActivity.this, "Admin department ID not found.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Log.d("MainActivity", "User logged in with UID: " + uid);

                                                // Navigate to UserActivity with user ID
                                                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                                                intent.putExtra("userId", uid);
                                                startActivity(intent);
                                                finish();
                                            }
                                        } else {
                                            Log.e("MainActivity", "Role not found for user: " + uid);
                                            Toast.makeText(MainActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.e("MainActivity", "User document not found for UID: " + uid);
                                        Toast.makeText(MainActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MainActivity", "Error fetching user details", e);
                                    Toast.makeText(MainActivity.this, "Error logging in", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Login failed", e);
                        Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    });
        });

        // Register Button Click Listener
        buttonRegister.setOnClickListener(v -> {
            // Redirect to RegisterActivity
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }
}
