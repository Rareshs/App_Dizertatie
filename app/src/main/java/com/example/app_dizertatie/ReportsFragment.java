package com.example.app_dizertatie;

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

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private static final String TAG = "ReportsFragment";

    private FirebaseFirestore db;
    private TextView textViewSummary;
    private ListView listViewUserReports;

    private String departmentId; // The admin's department ID
    private int totalTasks = 0;
    private int completedTasks = 0;
    private int pendingTasks = 0;
    private int overdueTasks = 0;

    private ArrayList<String> userReports;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        textViewSummary = view.findViewById(R.id.textViewSummary);
        listViewUserReports = view.findViewById(R.id.listViewUserReports);

        // Get the department ID from arguments
        if (getArguments() != null && getArguments().containsKey("departmentId")) {
            departmentId = getArguments().getString("departmentId");
        } else {
            Log.e(TAG, "Missing departmentId argument");
            Toast.makeText(getContext(), "Invalid department information.", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Load report data
        loadReports();

        return view;
    }

    private void loadReports() {
        userReports = new ArrayList<>();

        // Fetch all tasks in the department
        db.collection("tasks")
                .whereEqualTo("department_id", departmentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalTasks = queryDocumentSnapshots.size();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        boolean isCompleted = document.getBoolean("isCompleted") != null && document.getBoolean("isCompleted");

                        if (isCompleted) {
                            completedTasks++;
                        } else {
                            pendingTasks++;
                            // Check for overdue tasks
                            Timestamp deadlineTimestamp = document.getTimestamp("task_deadline");
                            if (deadlineTimestamp != null && deadlineTimestamp.toDate().before(new Date())) {
                                overdueTasks++;
                            }
                        }
                    }

                    // Display summary
                    displaySummary();

                    // Load user-specific reports
                    loadUserReports();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading tasks", e);
                    Toast.makeText(getContext(), "Error loading reports.", Toast.LENGTH_SHORT).show();
                });
    }

    private void displaySummary() {
        String summary = String.format(Locale.getDefault(),
                "Total Tasks: %d\nCompleted: %d\nPending: %d\nOverdue: %d",
                totalTasks, completedTasks, pendingTasks, overdueTasks);
        textViewSummary.setText(summary);
    }

    private void loadUserReports() {
        // Fetch all users in the department
        db.collection("users")
                .whereEqualTo("department_id", departmentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        userReports.add("No users found in this department.");
                        displayUserReports();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String username = document.getString("username");
                        String userId = document.getId();

                        // Fetch tasks for each user
                        fetchUserTaskReport(userId, username);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading users", e);
                    Toast.makeText(getContext(), "Error loading user reports.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUserTaskReport(String userId, String username) {
        db.collection("tasks")
                .whereEqualTo("assigned_user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int userTotalTasks = queryDocumentSnapshots.size();
                    int userCompletedTasks = 0;
                    int userPendingTasks = 0;
                    int userOverdueTasks = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        boolean isCompleted = document.getBoolean("isCompleted") != null && document.getBoolean("isCompleted");

                        if (isCompleted) {
                            userCompletedTasks++;
                        } else {
                            userPendingTasks++;
                            // Check for overdue tasks
                            Timestamp deadlineTimestamp = document.getTimestamp("task_deadline");
                            if (deadlineTimestamp != null && deadlineTimestamp.toDate().before(new Date())) {
                                userOverdueTasks++;
                            }
                        }
                    }

                    // Add user report to the list
                    String userReport = String.format(Locale.getDefault(),
                            "%s\nTotal Tasks: %d | Completed: %d | Pending: %d | Overdue: %d",
                            username, userTotalTasks, userCompletedTasks, userPendingTasks, userOverdueTasks);
                    userReports.add(userReport);

                    // Display user reports
                    displayUserReports();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading tasks for user: " + username, e);
                    Toast.makeText(getContext(), "Error loading user tasks.", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayUserReports() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, userReports);
        listViewUserReports.setAdapter(adapter);
    }
}
