package com.example.little_share.ui.sponsor;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class activity_sponsor_main extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sponsor_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        replaceFragment(new frm_sponsor_home());

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                if(item.getItemId() == R.id.nav_home){
                    selectedFragment = new frm_sponsor_home();
                } else if (item.getItemId() == R.id.nav_journey) {
<<<<<<< HEAD
                    selectedFragment = new frm_sponsor_campaign_sharing();
                } else if (item.getItemId() == R.id.nav_notification) {
                    selectedFragment = new frm_sponsor_notification();
                } else if (item.getItemId() == R.id.nav_profile) {
                    selectedFragment = new frm_sponsor_profile();
=======
                    selectedFragment = new frm_campaign_sharing_sponsor();
                } else if (item.getItemId() == R.id.nav_notification) {
                    selectedFragment = new frm_sponsor_notification();
                } else if (item.getItemId() == R.id.nav_profile) {
                    selectedFragment = new frm_profile_sponsor();
>>>>>>> 1021f6666a9fdef6213bea93a837382b23605876
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