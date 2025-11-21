package com.example.little_share.ui.ngo;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.little_share.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class activity_ngo_campaign_detail extends AppCompatActivity {
    MaterialButtonToggleGroup tabGroup;
    MaterialButton tabInfo, tabFinance, tabVolunteer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_campaign_detail);

        tabGroup = findViewById(R.id.tabGroup);
        tabInfo = findViewById(R.id.tabInfo);
        tabFinance = findViewById(R.id.tabFinance);
        tabVolunteer = findViewById(R.id.tabVolunteer);

        replaceFragment(new NgoCampaignDetailOverall());

        tabGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            Fragment selectedFragment = null;

            if (checkedId == R.id.tabInfo) {
                selectedFragment = new NgoCampaignDetailOverall();
            } else if (checkedId == R.id.tabFinance) {
                selectedFragment = new NgoCampaignDetailFinance();
            } else if (checkedId == R.id.tabVolunteer){
                selectedFragment = new NgoCampaignDetailVolunteer();
            }

            replaceFragment(selectedFragment);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.tabContainer, fragment)
                .commit();
    }
}
