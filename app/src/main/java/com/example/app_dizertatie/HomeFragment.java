package com.example.app_dizertatie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FirebaseFirestore db;
    private ListView listViewUsers;
    private ArrayList<String> userList;
    private ArrayList<String> userIdList;
    private String adminDepartmentId;
    private String departmentName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        listViewUsers = view.findViewById(R.id.listViewUsers);
        TextView textViewAdminWelcome = view.findViewById(R.id.textViewAdminWelcome);

        // Retrieve department data passed via arguments or activity
        Bundle args = getArguments();
        if (args != null) {
            adminDepartmentId = args.getString("departmentId");
            departmentName = args.getString("departmentName");
        } else {
            // Fallback: Retrieve from Activity (if passed there)
            adminDepartmentId = getActivity().getIntent().getStringExtra("departmentId");
            departmentName = getActivity().getIntent().getStringExtra("departmentName");
        }

        // Set admin welcome message
        textViewAdminWelcome.setText("Welcome, Admin of " + (departmentName != null ? departmentName : "Unknown Department"));

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        if (adminDepartmentId == null || adminDepartmentId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid department information", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Load users in the department
        loadUsers(adminDepartmentId);

        // Handle user click
        listViewUsers.setOnItemClickListener((adapterView, view1, position, id) -> {
            String userId = userIdList.get(position);
            if (userId.equals("NO_VALID_USER")) {
                Toast.makeText(getContext(), "No valid user selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navigate to UserTasksActivity
            Intent intent = new Intent(getContext(), UserTasksActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("departmentId", adminDepartmentId);
            intent.putExtra("departmentName", departmentName);
            startActivity(intent);
        });

        return view;
    }

    private void loadUsers(String departmentId) {
        Log.d("HomeFragment", "Loading users for department: " + departmentId);
        db.collection("users")
                .whereEqualTo("department_id", departmentId)
                .whereEqualTo("role", "User")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList = new ArrayList<>();
                    userIdList = new ArrayList<>();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userId = document.getId();
                            String username = document.getString("username");

                            if (username != null && !username.isEmpty()) {
                                userList.add(username);
                                userIdList.add(userId);
                            } else {
                                userList.add("Unknown User");
                                userIdList.add("NO_VALID_USER");
                            }
                        }
                    } else {
                        userList.add("No users found in your department");
                        userIdList.add("NO_VALID_USER");
                    }

                    // Update ListView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, userList);
                    listViewUsers.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error loading users", e);
                    Toast.makeText(getContext(), "Error loading users", Toast.LENGTH_SHORT).show();
                });
    }
}
