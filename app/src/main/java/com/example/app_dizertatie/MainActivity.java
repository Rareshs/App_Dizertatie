package com.example.app_dizertatie;

import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
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

        // Find Views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        // Set Login Button Click Listener
        buttonLogin.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            // Check user role in the database
            String role = db.getUserRole(username, password);
            if (role != null) {
                if (role.equals("Admin")) {
                    startActivity(new Intent(MainActivity.this, AdminActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(MainActivity.this, UserActivity.class));
                    finish();
                }
            } else {
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