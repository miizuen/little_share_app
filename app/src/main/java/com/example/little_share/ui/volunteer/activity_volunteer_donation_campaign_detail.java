package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;

public class activity_volunteer_donation_campaign_detail extends AppCompatActivity {

    private Campaign campaign;
    private TextView tvCampaignName, tvOrganizationName, tvLocation, tvDescription, tvCategory;
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
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvOrganizationName = findViewById(R.id.tvOrganizationName);
        tvLocation = findViewById(R.id.tvLocation);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        btnDonateNow = findViewById(R.id.btnDonateNow);
    }

    private void setupData() {
        tvCampaignName.setText(campaign.getName());
        tvOrganizationName.setText(campaign.getOrganizationName());
        tvDescription.setText(campaign.getDescription());

        // Format location with working hours
        String locationText = campaign.getLocation();
        if (campaign.getSpecificLocation() != null && !campaign.getSpecificLocation().isEmpty()) {
            locationText += " • " + campaign.getSpecificLocation();
        }
        tvLocation.setText(locationText);

        // Set category display
        String categoryDisplay = getCategoryDisplayName(campaign.getCategory());
        tvCategory.setText(categoryDisplay);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDonateNow.setOnClickListener(v -> {
            // Navigate to appropriate donation form based on campaign category
            String donationType = getDonationTypeFromCategory(campaign.getCategory());
            
            Intent intent = new Intent(this, activity_volunteer_donation_form.class);
            intent.putExtra("DONATION_TYPE", donationType);
            intent.putExtra("CAMPAIGN_ID", campaign.getId());
            intent.putExtra("ORGANIZATION_ID", campaign.getOrganizationId());
            intent.putExtra("ORGANIZATION_NAME", campaign.getOrganizationName());
            startActivity(intent);
        });
    }

    private String getCategoryDisplayName(String category) {
        if (category == null) return "Khác";
        
        switch (category.toUpperCase()) {
            case "BOOKS":
            case "EDUCATION":
                return "Sách vở";
            case "CLOTHES":
                return "Quần áo";
            case "TOYS":
                return "Đồ chơi";
            case "ESSENTIALS":
            case "FOOD":
                return "Nhu yếu phẩm";
            default:
                return "Khác";
        }
    }

    private String getDonationTypeFromCategory(String category) {
        if (category == null) return "ESSENTIALS";
        
        switch (category.toUpperCase()) {
            case "BOOKS":
            case "EDUCATION":
                return "BOOKS";
            case "CLOTHES":
                return "CLOTHES";
            case "TOYS":
                return "TOYS";
            case "ESSENTIALS":
            case "FOOD":
            default:
                return "ESSENTIALS";
        }
    }
}