package com.example.little_share.ui.sponsor;

import android.content.Intent;
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

import vn.zalopay.sdk.ZaloPaySDK;

public class activity_sponsor_main extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sponsor_main);
        initViews();
        setupBottomNavigation();
        handleIntent();
        setupWindowInsets();
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_home) {
                    selectedFragment = new frm_sponsor_home();
                } else if (itemId == R.id.nav_journey) {
                    selectedFragment = new frm_sponsor_campaign_sharing();
                } else if (itemId == R.id.nav_notification) {
                    selectedFragment = new frm_sponsor_notification();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new frm_sponsor_profile();
                }
                
                if (selectedFragment != null) {
                    replaceFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });
    }

    private void handleIntent() {
        // Check if we need to open donation form
        if (getIntent().getBooleanExtra("open_donation_form", false)) {
            openDonationFormFromIntent();
        } else {
            replaceFragment(new frm_sponsor_home());
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
        
        // Check if we need to open donation form from new intent
        if (intent.getBooleanExtra("open_donation_form", false)) {
            setIntent(intent);
            openDonationFormFromIntent();
        }
    }

    private void openDonationFormFromIntent() {
        Intent intent = getIntent();
        String campaignId = intent.getStringExtra("campaign_id");
        String campaignName = intent.getStringExtra("campaign_name");
        String organizationName = intent.getStringExtra("campaign_organization_name");
        double targetBudget = intent.getDoubleExtra("campaign_target_budget", 0);
        double currentBudget = intent.getDoubleExtra("campaign_current_budget", 0);

        frm_sponsor_donation_form donationFragment = frm_sponsor_donation_form.newInstance(
                campaignId, campaignName, organizationName, targetBudget, currentBudget
        );

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, donationFragment)
                .addToBackStack(null)
                .commit();
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}