package com.example.little_share.ui.volunteer;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.little_share.R;
import com.example.little_share.utils.LittleShareNotification;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class activity_volunteer_main extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_main);

        // Request notification permission trước
        requestNotificationPermission();

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        String navigateTo = getIntent().getStringExtra("navigateTo");
        if ("calendar".equals(navigateTo)) {
            replaceFragment(new frm_volunteer_calendar());
            bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
        } else {
            replaceFragment(new frm_volunteer_home());
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                if(item.getItemId() == R.id.nav_home){
                    selectedFragment = new frm_volunteer_home();
                } else if (item.getItemId() == R.id.nav_calendar) {
                    selectedFragment = new frm_volunteer_calendar();
                } else if (item.getItemId() == R.id.nav_volunteer) {
                    selectedFragment = new frm_volunteer_donation();
                }else if (item.getItemId() == R.id.nav_notification) {
                    selectedFragment = new frm_volunteer_notification();
                } else if (item.getItemId() == R.id.nav_profile) {
                    selectedFragment = new frm_profile_volunteer();
                }

                if (selectedFragment != null) {
                    replaceFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Test notification sau 3 giây
        testNotification();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                android.util.Log.d("VolunteerMain", "Notification permission granted");
            } else {
                android.util.Log.d("VolunteerMain", "Notification permission denied");
                // Có thể hiển thị dialog giải thích tại sao cần permission
            }
        }
    }

    private void testNotification() {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            android.util.Log.d("VolunteerMain", "Testing notification...");
            LittleShareNotification.testNotification(this);
        }, 3000);
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources nếu cần
    }
}