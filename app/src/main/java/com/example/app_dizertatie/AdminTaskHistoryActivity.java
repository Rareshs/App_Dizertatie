package com.example.app_dizertatie;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdminTaskHistoryActivity extends AppCompatActivity {

    private static final String TAG = "AdminTaskHistoryActivity"; // For logging
    private FirebaseFirestore db; // Firestore instance
    private ListView listViewHistory;
    private ArrayList<String> historyList;
    private String userId; // Changed to String for Firebase compatibility

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_task_history);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        listViewHistory = findViewById(R.id.listViewHistory);

        // Get user ID from intent
        userId = getIntent().getStringExtra("userId");

        if (userId == null) {
            Toast.makeText(this, "Invalid user information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadHistory();
    }

    private void loadHistory() {
        historyList = new ArrayList<>();
        Log.d(TAG, "Loading task history for userId: " + userId);

        db.collection("history")
                .whereEqualTo("user_id", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No completed tasks found for userId: " + userId);
                        historyList.add("No completed tasks for this user.");
                    } else {
                        queryDocumentSnapshots.forEach(document -> {
                            String taskTitle = document.getString("task_title");

                            // Safely handle null timestamps
                            com.google.firebase.Timestamp timestamp = document.getTimestamp("timestamp");
                            String formattedTimestamp = (timestamp != null)
                                    ? timestamp.toDate().toString()
                                    : "No timestamp available";

                            String historyEntry = "Task: " + taskTitle + "\nCompleted at: " + formattedTimestamp;
                            historyList.add(historyEntry);
                        });
                    }

                    // Populate the ListView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
                    listViewHistory.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching task history", e);
                    Toast.makeText(this, "Error loading task history", Toast.LENGTH_SHORT).show();
                });
    }

}
