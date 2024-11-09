package com.example.app_dizertatie;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private DataBaseHelper db;
    private EditText editTextRegisterUsername, editTextRegisterPassword;
    private Spinner spinnerRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DataBaseHelper(this);

        // Initialize views
        editTextRegisterUsername = findViewById(R.id.editTextRegisterUsername);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        Button buttonSubmitRegister = findViewById(R.id.buttonSubmitRegister);

        // Set up the role Spinner with the roles array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        // Register button functionality
        buttonSubmitRegister.setOnClickListener(v -> {
            String username = editTextRegisterUsername.getText().toString();
            String password = editTextRegisterPassword.getText().toString();
            String role = spinnerRole.getSelectedItem().toString();

            // Add user to the database with the selected role
            if (!username.isEmpty() && !password.isEmpty()) {
                if (db.userExists(username, role)) {
                    Toast.makeText(RegisterActivity.this, "User with this username and role already exists", Toast.LENGTH_SHORT).show();
                } else {
                    db.addUser(username, password, role);
                    Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    finish();  // Return to Login
                }
            }
        });
    }
}
