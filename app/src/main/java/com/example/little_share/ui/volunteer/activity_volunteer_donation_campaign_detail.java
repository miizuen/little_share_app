package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;

public class activity_volunteer_donation_campaign_detail extends AppCompatActivity {

    private static final String TAG = "DonationCampaignDetail";

    private Campaign campaign;
    private TextView tvCampaignName, tvOrganizationName, tvLocation, tvDescription, tvCategory;
    private TextView tvUrgentBadge;
    private CardView btnDonateNow;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_donation_campaign_detail);

        // Get campaign from intent
        campaign = (Campaign) getIntent().getSerializableExtra("campaign");
        if (campaign == null) {
            Toast.makeText(this, "Lỗi tải thông tin chiến dịch", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupData();
        setupClickListeners();

        Log.d(TAG, "Campaign loaded: " + campaign.getName());
        Log.d(TAG, "Donation Type: " + campaign.getDonationType());
        Log.d(TAG, "Campaign ID: " + campaign.getId());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvOrganizationName = findViewById(R.id.tvOrganizationName);
        tvLocation = findViewById(R.id.tvLocation);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvUrgentBadge = findViewById(R.id.tvUrgentBadge);
        btnDonateNow = findViewById(R.id.btnDonateNow);
    }

    private void setupData() {
        // Campaign name
        tvCampaignName.setText(campaign.getName());

        // Organization name
        String orgName = campaign.getOrganizationName();
        tvOrganizationName.setText(orgName != null && !orgName.isEmpty()
                ? orgName : "Tổ chức từ thiện");

        // Description
        String description = campaign.getDescription();
        tvDescription.setText(description != null && !description.isEmpty()
                ? description : "Chiến dịch quyên góp vật phẩm");

        // Format location with working hours
        String locationText = campaign.getLocation();
        if (campaign.getSpecificLocation() != null && !campaign.getSpecificLocation().isEmpty()) {
            locationText += " • " + campaign.getSpecificLocation();
        }
        tvLocation.setText(locationText);

        // Set donation type display - THAY ĐỔI QUAN TRỌNG
        String donationTypeDisplay = getDonationTypeDisplayName(campaign.getDonationType());
        tvCategory.setText(donationTypeDisplay);

        Log.d(TAG, "Displaying donation type: " + donationTypeDisplay);

        // Show/hide urgent badge
        if ("URGENT".equals(campaign.getCategory())) {
            tvUrgentBadge.setVisibility(View.VISIBLE);
        } else {
            tvUrgentBadge.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDonateNow.setOnClickListener(v -> {
            String donationType = campaign.getDonationType();

            if (donationType == null || donationType.isEmpty()) {
                Toast.makeText(this, "Lỗi: Loại quyên góp không hợp lệ", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Donation type is null or empty");
                return;
            }

            Log.d(TAG, "Opening donation form with type: " + donationType);
            Log.d(TAG, "Campaign ID: " + campaign.getId());
            Log.d(TAG, "Organization ID: " + campaign.getOrganizationId());

            Intent intent = new Intent(this, activity_volunteer_donation_form.class);
            intent.putExtra("DONATION_TYPE", donationType);
            intent.putExtra("CAMPAIGN_ID", campaign.getId());
            intent.putExtra("ORGANIZATION_ID", campaign.getOrganizationId());
            intent.putExtra("ORGANIZATION_NAME", campaign.getOrganizationName());
            startActivity(intent);
        });
    }

    private String getDonationTypeDisplayName(String donationType) {
        if (donationType == null || donationType.isEmpty()) {
            Log.w(TAG, "Donation type is null or empty");
            return "Quyên góp";
        }

        try {
            Campaign.DonationType type = Campaign.DonationType.valueOf(donationType);
            String displayName = type.getDisplayName();
            Log.d(TAG, "Donation type: " + donationType + " -> " + displayName);
            return displayName;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid donation type: " + donationType);
            return "Quyên góp";
        }
    }
}