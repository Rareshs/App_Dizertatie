package com.example.app_dizertatie;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private FirebaseAuth auth; // Firebase Authentication instance
    private FirebaseFirestore db; // Firestore instance

    private EditText editTextRegisterUsername, editTextRegisterPassword, editTextRegisterEmail, editTextDepartmentName;
    private Spinner spinnerRole, spinnerDepartment;
    private Button buttonSubmitRegister;

    private boolean adminExists = false; // Track if an admin exists
    private boolean isAdminCheckComplete = false; // Track if the admin check is complete

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        editTextRegisterUsername = findViewById(R.id.editTextRegisterUsername);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail);
        editTextDepartmentName = findViewById(R.id.editTextDepartmentName);
        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        buttonSubmitRegister = findViewById(R.id.buttonSubmitRegister);

        // Disable spinner until admin check is complete
        spinnerRole.setEnabled(false);
        checkIfAdminExists();

        // Set up the role Spinner with the roles array
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Role-based department visibility
        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String role = parent.getItemAtPosition(position).toString();

                if (!isAdminCheckComplete) {
                    Toast.makeText(RegisterActivity.this, "Checking for admin... Please wait.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (role.equals("Admin")) {
                    editTextDepartmentName.setVisibility(View.VISIBLE);
                    spinnerDepartment.setVisibility(View.GONE);
                } else {
                    if (!adminExists) {
                        // Warn and reset the spinner if no admin exists
                        Toast.makeText(RegisterActivity.this, "An admin must be created before adding users.", Toast.LENGTH_LONG).show();
                        spinnerRole.setSelection(0); // Reset to "Admin"
                        return;
                    }

                    editTextDepartmentName.setVisibility(View.GONE);
                    populateDepartmentSpinner();
                    spinnerDepartment.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Register button functionality
        buttonSubmitRegister.setOnClickListener(v -> {
            String username = editTextRegisterUsername.getText().toString().trim();
            String password = editTextRegisterPassword.getText().toString().trim();
            String email = editTextRegisterEmail.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (role.equals("Admin")) {
                String departmentName = editTextDepartmentName.getText().toString().trim();
                if (!departmentName.isEmpty()) {
                    addAdminWithDepartment(username, email, password, departmentName);
                } else {
                    Toast.makeText(this, "Please specify a department name for the admin.", Toast.LENGTH_SHORT).show();
                }
            } else {
                String departmentName = spinnerDepartment.getSelectedItem().toString();
                if (!departmentName.isEmpty()) {
                    addUser(username, email, password, role, departmentName);
                } else {
                    Toast.makeText(this, "Please select a department.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Check if at least one admin exists
    private void checkIfAdminExists() {
        db.collection("users")
                .whereEqualTo("role", "Admin")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    adminExists = !queryDocumentSnapshots.isEmpty();
                    isAdminCheckComplete = true;
                    spinnerRole.setEnabled(true); // Enable spinner after check is complete
                    Log.d(TAG, "Admin exists: " + adminExists);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for admin existence", e);
                    adminExists = false; // Default to false on error
                    isAdminCheckComplete = true;
                    spinnerRole.setEnabled(true); // Enable spinner after check is complete
                });
    }

    private void addAdminWithDepartment(String username, String email, String password, String departmentName) {
        db.collection("departments").add(Map.of("department_name", departmentName))
                .addOnSuccessListener(departmentRef -> {
                    String departmentId = departmentRef.getId();

                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                FirebaseUser firebaseUser = authResult.getUser();
                                if (firebaseUser != null) {
                                    String uid = firebaseUser.getUid();
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("username", username);
                                    userData.put("email", email);
                                    userData.put("role", "Admin");
                                    userData.put("department_id", departmentId);

                                    db.collection("users").document(uid)
                                            .set(userData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Admin registered successfully.", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error saving admin data", e);
                                                Toast.makeText(this, "Failed to save admin data.", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating admin user", e);
                                Toast.makeText(this, "Failed to register admin. Try again.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding department", e);
                    Toast.makeText(this, "Failed to add department.", Toast.LENGTH_SHORT).show();
                });
    }

    private void addUser(String username, String email, String password, String role, String departmentName) {
        db.collection("departments")
                .whereEqualTo("department_name", departmentName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String departmentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener(authResult -> {
                                    FirebaseUser firebaseUser = authResult.getUser();
                                    if (firebaseUser != null) {
                                        String uid = firebaseUser.getUid();
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("username", username);
                                        userData.put("email", email);
                                        userData.put("role", role);
                                        userData.put("department_id", departmentId);

                                        db.collection("users").document(uid)
                                                .set(userData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(this, "User registered successfully.", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error saving user data", e);
                                                    Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating user", e);
                                    Toast.makeText(this, "Failed to register user. Try again.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Department not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding department", e);
                    Toast.makeText(this, "Error finding department.", Toast.LENGTH_SHORT).show();
                });
    }

    private void populateDepartmentSpinner() {
        db.collection("departments").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> departmentList = new ArrayList<>();
                    queryDocumentSnapshots.forEach(document -> {
                        String departmentName = document.getString("department_name");
                        departmentList.add(departmentName);
                    });

                    ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentList);
                    departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDepartment.setAdapter(departmentAdapter);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading departments", e));
    }
}
