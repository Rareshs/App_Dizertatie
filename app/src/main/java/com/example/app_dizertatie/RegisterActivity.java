package com.example.app_dizertatie;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    private DataBaseHelper db;
    private EditText editTextRegisterUsername, editTextRegisterPassword, editTextDepartmentName;
    private Spinner spinnerRole, spinnerDepartment;
    private Button buttonSubmitRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DataBaseHelper(this);

        // Initialize views
        editTextRegisterUsername = findViewById(R.id.editTextRegisterUsername);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        editTextDepartmentName = findViewById(R.id.editTextDepartmentName); // For new department name
        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        buttonSubmitRegister = findViewById(R.id.buttonSubmitRegister);

        // Set up the role Spinner with the roles array
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Set up role-based department visibility
        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String role = parent.getItemAtPosition(position).toString();
                if (role.equals("Admin")) {
                    editTextDepartmentName.setVisibility(View.VISIBLE);
                    spinnerDepartment.setVisibility(View.GONE);
                } else {
                    editTextDepartmentName.setVisibility(View.GONE);
                    if (db.hasDepartments()) {
                        populateDepartmentSpinner();
                        spinnerDepartment.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(RegisterActivity.this, "No departments available. Please contact an admin.", Toast.LENGTH_LONG).show();
                        spinnerDepartment.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Register button functionality
        buttonSubmitRegister.setOnClickListener(v -> {
            String username = editTextRegisterUsername.getText().toString();
            String password = editTextRegisterPassword.getText().toString();
            String role = spinnerRole.getSelectedItem().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username and Password cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (role.equals("Admin")) {
                String departmentName = editTextDepartmentName.getText().toString();
                if (!departmentName.isEmpty()) {
                    // Add the department and retrieve its ID
                    long departmentId = db.addDepartment(departmentName);
                    if (departmentId != -1) {
                        // Associate the admin with the department
                        db.addUser(username, password, role, (int) departmentId);
                        Toast.makeText(this, "Admin registered successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to add department.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Please specify a department name for the admin.", Toast.LENGTH_SHORT).show();
                }

            } else {
                if (db.hasDepartments()) {
                    String departmentName = spinnerDepartment.getSelectedItem().toString();
                    int departmentId = db.getDepartmentId(departmentName);
                    if (departmentId != -1) {
                        db.addUser(username, password, role, departmentId);
                        Toast.makeText(this, "User registered successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to retrieve selected department.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No departments available. Please contact an admin.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Populate the department spinner
    private void populateDepartmentSpinner() {
        ArrayList<String> departmentList = new ArrayList<>();
        Cursor cursor = db.getAllDepartments();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int columnIndex = cursor.getColumnIndex(DataBaseHelper.COLUMN_DEPARTMENT_NAME);
                if (columnIndex != -1) {
                    String departmentName = cursor.getString(columnIndex);
                    departmentList.add(departmentName);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (departmentList.isEmpty()) {
            Log.d("RegisterActivity", "No departments found in the database.");
        } else {
            Log.d("RegisterActivity", "Departments loaded: " + departmentList);
        }

        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentList);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(departmentAdapter);
    }
}