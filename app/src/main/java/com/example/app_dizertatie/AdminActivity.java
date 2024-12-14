package com.example.app_dizertatie;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AdminActivity extends AppCompatActivity {

    private static final int HOME = 1;
    private static final int DEPARTMENT = 2;
    private static final int REPORTS = 3;
    private static final int SETTINGS = 4;
    private static final int LOGOUT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve departmentId and departmentName from intent or saved state
        String departmentId = getIntent().getStringExtra("departmentId");
        String departmentName = getIntent().getStringExtra("departmentName");

        // Pass the departmentId and departmentName to the default fragment
        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putString("departmentId", departmentId);
            args.putString("departmentName", departmentName);
            loadFragment(new HomeFragment(), "Home", args);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Manually add menu items with integer IDs
        menu.add(Menu.NONE, HOME, Menu.NONE, "Home").setIcon(R.drawable.ic_home);
        menu.add(Menu.NONE, DEPARTMENT, Menu.NONE, "Department Tasks").setIcon(R.drawable.ic_department);
        menu.add(Menu.NONE, REPORTS, Menu.NONE, "Reports").setIcon(R.drawable.ic_reports);
        menu.add(Menu.NONE, SETTINGS, Menu.NONE, "Settings").setIcon(R.drawable.ic_settings);
        menu.add(Menu.NONE, LOGOUT, Menu.NONE, "Log Out").setIcon(R.drawable.ic_logout);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String title = null;
        Bundle args = new Bundle();

        // Retrieve departmentId and departmentName
        String departmentId = getIntent().getStringExtra("departmentId");
        String departmentName = getIntent().getStringExtra("departmentName");
        args.putString("departmentId", departmentId);
        args.putString("departmentName", departmentName);

        switch (item.getItemId()) {
            case HOME:
                fragment = new HomeFragment();
                title = "Home";
                break;
            case DEPARTMENT:
                fragment = new DepartmentFragment();
                title = "Department Tasks";
                break;
            case REPORTS:
                fragment = new ReportsFragment();
                title = "Reports";
                break;
            case SETTINGS:
                fragment = new SettingsFragment();
                title = "Settings";
                break;
            case LOGOUT:
                finish(); // Exit the app or log out
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        if (fragment != null) {
            loadFragment(fragment, title, args);
        }
        return true;
    }

    private void loadFragment(Fragment fragment, String title, Bundle args) {
        if (args != null) {
            fragment.setArguments(args);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_layout, fragment);
        transaction.commit();
        setTitle(title); // Set the title for the current screen
    }
}
