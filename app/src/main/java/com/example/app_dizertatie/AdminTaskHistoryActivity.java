package com.example.app_dizertatie;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AdminTaskHistoryActivity extends AppCompatActivity {

    private DataBaseHelper db;
    private ListView listViewHistory;
    private ArrayList<String> historyList;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_task_history);

        db = new DataBaseHelper(this);
        listViewHistory = findViewById(R.id.listViewHistory);

        // Get user ID from intent
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadHistory();
    }

    private void loadHistory() {
        historyList = new ArrayList<>();
        Cursor cursor = db.getTaskHistoryByUser(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String taskTitle = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_TASK_TITLE));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_HISTORY_TIMESTAMP));

                String historyEntry = "Task: " + taskTitle + "\nCompleted at: " + timestamp;
                historyList.add(historyEntry);
            } while (cursor.moveToNext());

            cursor.close();
        }

        if (historyList.isEmpty()) {
            historyList.add("No completed tasks for this user.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        listViewHistory.setAdapter(adapter);
    }
}
