package com.example.app_dizertatie;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private DataBaseHelper db;
    private ListView listViewUsers;
    private ArrayList<String> userList;
    private ArrayList<Integer> userIdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = new DataBaseHelper(this);
        listViewUsers = findViewById(R.id.listViewUsers);

        // Get data from intent
        int adminDepartmentId = getIntent().getIntExtra("departmentId", -1);
        String departmentName = getIntent().getStringExtra("departmentName");

        // Set admin welcome message
        TextView textViewAdminWelcome = findViewById(R.id.textViewAdminWelcome);
        textViewAdminWelcome.setText("Welcome, Admin of " + departmentName);

        if (adminDepartmentId == -1) {
            Toast.makeText(this, "Invalid department information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load users in the admin's department
        loadUsers(adminDepartmentId);

        // Handle user click in ListView
        listViewUsers.setOnItemClickListener((adapterView, view, position, id) -> {
            int userId = userIdList.get(position);
            if (userId == -1) {
                Toast.makeText(AdminActivity.this, "No valid user selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navigate to UserTasksActivity
            Intent intent = new Intent(AdminActivity.this, UserTasksActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

    }

    private void loadUsers(int departmentId) {
        Cursor cursor = db.getUsersByDepartment(departmentId);

        userList = new ArrayList<>();
        userIdList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_USER_ID));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_USERNAME));

                userIdList.add(userId);
                userList.add(username);

            } while (cursor.moveToNext());
            cursor.close();
        }

        if (userList.isEmpty()) {
            userList.add("No users found in your department");
            userIdList.add(-1); // Placeholder for no valid user
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        listViewUsers.setAdapter(adapter);
    }
}
