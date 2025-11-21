package com.example.little_share.ui.volunteer;

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
import com.example.little_share.ui.sponsor.SponsorCampaignSharing;
import com.example.little_share.ui.sponsor.SponsorHome;
import com.example.little_share.ui.sponsor.SponsorNotification;
import com.example.little_share.ui.sponsor.SponsorProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class activity_volunteer_main extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        replaceFragment(new VolunteerHome());

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                if(item.getItemId() == R.id.nav_home){
                    selectedFragment = new VolunteerHome();
                } else if (item.getItemId() == R.id.nav_calendar) {
                    selectedFragment = new VolunteerCalendar();
                } else if (item.getItemId() == R.id.nav_volunteer) {
                    selectedFragment = new VolunteerDonation();
                }else if (item.getItemId() == R.id.nav_notification) {
                    selectedFragment = new VolunteerNotification();
                } else if (item.getItemId() == R.id.nav_profile) {
                    selectedFragment = new VolunteerProfile();
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
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
