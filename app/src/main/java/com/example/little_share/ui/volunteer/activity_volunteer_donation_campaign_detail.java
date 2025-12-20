package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class activity_volunteer_donation_campaign_detail extends AppCompatActivity {

    private Campaign campaign;
    private TextView tvCampaignName, tvDescription, tvLocation, tvWorkingHours;
    private TextView tvStartDate, tvEndDate, tvContact, tvPoints;
    private ImageView ivCampaignImage;
    private Button btnDonate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_donation_campaign_detail);

        // Get campaign from intent
        campaign = (Campaign) getIntent().getSerializableExtra("campaign");

        initViews();
        setupToolbar();
        displayCampaignInfo();
        setupDonateButton();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvDescription = findViewById(R.id.tvDescription);
        tvLocation = findViewById(R.id.tvLocation);
        tvWorkingHours = findViewById(R.id.tvWorkingHours);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvContact = findViewById(R.id.tvContact);
        tvPoints = findViewById(R.id.tvPoints);
        ivCampaignImage = findViewById(R.id.ivCampaignImage);
        btnDonate = findViewById(R.id.btnDonate);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayCampaignInfo() {
        if (campaign == null) return;

        tvCampaignName.setText(campaign.getName());
        tvDescription.setText(campaign.getDescription());
        tvLocation.setText(campaign.getLocation());
        tvWorkingHours.setText("Thời gian: " + campaign.getSpecificLocation()); // Working hours
        tvContact.setText(campaign.getContactPhone());
        tvPoints.setText("+" + campaign.getPointsReward() + " điểm");

        // Format dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (campaign.getStartDate() != null) {
            tvStartDate.setText(dateFormat.format(campaign.getStartDate()));
        }
        if (campaign.getEndDate() != null) {
            tvEndDate.setText(dateFormat.format(campaign.getEndDate()));
        }

        // Load image
        if (campaign.getImageUrl() != null && !campaign.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(campaign.getImageUrl())
                    .placeholder(R.drawable.ic_clothes_3d)
                    .into(ivCampaignImage);
        }
    }

    private void setupDonateButton() {
        btnDonate.setOnClickListener(v -> {
            // Xác định donation type dựa trên category của campaign
            String donationType = getDonationTypeFromCampaign(campaign);

            // Chuyển đến trang donation form với type tương ứng
            Intent intent = new Intent(this, activity_volunteer_donation_form.class);
            intent.putExtra("DONATION_TYPE", donationType);
            intent.putExtra("campaign", campaign); // Truyền thêm campaign info
            startActivity(intent);
        });
    }

    private String getDonationTypeFromCampaign(Campaign campaign) {
        if (campaign == null) return "BOOKS";

        String category = campaign.getCategory();
        if (category == null) return "BOOKS";

        // Logic để map category thành donation type
        switch (category) {
            case "EDUCATION":
                return "BOOKS"; // Giáo dục -> Sách vở
            case "HEALTH":
                return "ESSENTIALS"; // Y tế -> Nhu yếu phẩm
            case "FOOD":
                return "ESSENTIALS"; // Nấu ăn -> Nhu yếu phẩm
            case "ENVIRONMENT":
                return "ESSENTIALS"; // Môi trường -> Nhu yếu phẩm
            case "URGENT":
                // Với urgent, có thể cho user chọn hoặc mặc định
                return "ESSENTIALS"; // Hoặc mở dialog cho user chọn
            default:
                return "BOOKS";
        }
    }

}
