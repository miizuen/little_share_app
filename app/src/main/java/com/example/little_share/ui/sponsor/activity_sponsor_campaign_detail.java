package com.example.little_share.ui.sponsor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class activity_sponsor_campaign_detail extends AppCompatActivity {
    private static final String TAG = "SponsorCampaignDetail";

    // Views
    private ImageView btnBack, imgCampaign;
    private TextView tvCampaignName, tvOrganizationName, tvCategory, tvLocation;
    private TextView tvStartDate, tvEndDate, tvDescription;
    private TextView tvCurrentBudget, tvTargetBudget, tvProgressPercent;
    private ProgressBar progressBudget;
    private MaterialButton btnDonate, btnViewReport;

    // Data
    private String campaignId;
    private String campaignName;
    private String campaignDescription;
    private String campaignLocation;
    private Date campaignStartDate;
    private Date campaignEndDate;
    private double campaignTargetBudget;
    private double campaignCurrentBudget;
    private String campaignImageUrl;
    private String campaignOrganizationName;
    private String campaignCategory;
    private String campaignStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sponsor_campaign_detail);

        setupWindowInsets();
        initViews();
        getIntentData();
        setupClickListeners();
        displayCampaignData();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgCampaign = findViewById(R.id.imgCampaign);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvOrganizationName = findViewById(R.id.tvOrganizationName);
        tvCategory = findViewById(R.id.tvCategory);
        tvLocation = findViewById(R.id.tvLocation);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvDescription = findViewById(R.id.tvDescription);
        tvCurrentBudget = findViewById(R.id.tvCurrentBudget);
        tvTargetBudget = findViewById(R.id.tvTargetBudget);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        progressBudget = findViewById(R.id.progressBudget);
        btnDonate = findViewById(R.id.btnDonate);
        btnViewReport = findViewById(R.id.btnViewReport);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            campaignId = intent.getStringExtra("campaign_id");
            campaignName = intent.getStringExtra("campaign_name");
            campaignDescription = intent.getStringExtra("campaign_description");
            campaignLocation = intent.getStringExtra("campaign_location");
            campaignStartDate = (Date) intent.getSerializableExtra("campaign_start_date");
            campaignEndDate = (Date) intent.getSerializableExtra("campaign_end_date");
            campaignTargetBudget = intent.getDoubleExtra("campaign_target_budget", 0);
            campaignCurrentBudget = intent.getDoubleExtra("campaign_current_budget", 0);
            campaignImageUrl = intent.getStringExtra("campaign_image_url");
            campaignOrganizationName = intent.getStringExtra("campaign_organization_name");
            campaignCategory = intent.getStringExtra("campaign_category");
            campaignStatus = intent.getStringExtra("campaign_status");

            Log.d(TAG, "Campaign ID: " + campaignId);
            Log.d(TAG, "Campaign Name: " + campaignName);
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnDonate != null) {
            btnDonate.setOnClickListener(v -> {
                Intent intent = new Intent(this, activity_sponsor_main.class);
                intent.putExtra("open_donation_form", true);
                intent.putExtra("campaign_id", campaignId);
                intent.putExtra("campaign_name", campaignName);
                intent.putExtra("campaign_organization_name", campaignOrganizationName);
                intent.putExtra("campaign_target_budget", campaignTargetBudget);
                intent.putExtra("campaign_current_budget", campaignCurrentBudget);
                startActivity(intent);
                finish();
            });
        }

        if (btnViewReport != null) {
            btnViewReport.setOnClickListener(v -> {
                Intent intent = new Intent(this, activity_sponsor_report_view.class);
                intent.putExtra("campaign_id", campaignId);
                intent.putExtra("campaign_name", campaignName);
                startActivity(intent);
            });
        }
    }

    private void displayCampaignData() {
        // Tên chiến dịch
        if (tvCampaignName != null) {
            tvCampaignName.setText(campaignName != null ? campaignName : "Chưa có tên");
        }

        // Tổ chức
        if (tvOrganizationName != null) {
            tvOrganizationName.setText(campaignOrganizationName != null ? campaignOrganizationName : "Chưa có thông tin");
        }

        // Category
        if (tvCategory != null) {
            tvCategory.setText(getCategoryDisplayName(campaignCategory));
        }

        // Địa điểm
        if (tvLocation != null) {
            tvLocation.setText(campaignLocation != null ? campaignLocation : "Chưa xác định");
        }

        // Mô tả
        if (tvDescription != null) {
            tvDescription.setText(campaignDescription != null ? campaignDescription : "Chưa có mô tả");
        }

        // Ngày tháng
        displayDates();

        // Ngân sách
        displayBudget();

        // Load image
        loadCampaignImage();

        // Hiển thị nút phù hợp theo trạng thái
        updateButtonsVisibility();
    }

    private void displayDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            String startStr = campaignStartDate != null ? sdf.format(campaignStartDate) : "N/A";
            String endStr = campaignEndDate != null ? sdf.format(campaignEndDate) : "N/A";
            
            if (tvStartDate != null) tvStartDate.setText(startStr);
            if (tvEndDate != null) tvEndDate.setText(endStr);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage());
            if (tvStartDate != null) tvStartDate.setText("Chưa xác định");
            if (tvEndDate != null) tvEndDate.setText("Chưa xác định");
        }
    }

    private void displayBudget() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        if (tvCurrentBudget != null) {
            tvCurrentBudget.setText(formatter.format(campaignCurrentBudget));
        }
        
        if (tvTargetBudget != null) {
            tvTargetBudget.setText(formatter.format(campaignTargetBudget));
        }

        // Progress
        int progress = 0;
        if (campaignTargetBudget > 0) {
            progress = (int) ((campaignCurrentBudget / campaignTargetBudget) * 100);
        }
        
        if (progressBudget != null) {
            progressBudget.setProgress(Math.min(progress, 100));
        }
        
        if (tvProgressPercent != null) {
            tvProgressPercent.setText(progress + "%");
        }
    }

    private void loadCampaignImage() {
        if (imgCampaign != null) {
            if (campaignImageUrl != null && !campaignImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(campaignImageUrl)
                        .placeholder(R.drawable.img_quyengop_dochoi)
                        .error(R.drawable.img_quyengop_dochoi)
                        .into(imgCampaign);
            } else {
                imgCampaign.setImageResource(R.drawable.img_quyengop_dochoi);
            }
        }
    }

    private void updateButtonsVisibility() {
        if (campaignStatus != null) {
            switch (campaignStatus) {
                case "ACTIVE":
                case "ONGOING":
                    if (btnDonate != null) btnDonate.setVisibility(View.VISIBLE);
                    if (btnViewReport != null) btnViewReport.setVisibility(View.GONE);
                    break;
                case "COMPLETED":
                case "FINISHED":
                    if (btnDonate != null) btnDonate.setVisibility(View.GONE);
                    if (btnViewReport != null) btnViewReport.setVisibility(View.VISIBLE);
                    break;
                default:
                    if (btnDonate != null) btnDonate.setVisibility(View.VISIBLE);
                    if (btnViewReport != null) btnViewReport.setVisibility(View.GONE);
                    break;
            }
        } else {
            if (btnDonate != null) btnDonate.setVisibility(View.VISIBLE);
            if (btnViewReport != null) btnViewReport.setVisibility(View.GONE);
        }
    }

    private String getCategoryDisplayName(String category) {
        if (category == null) return "Khác";

        try {
            Campaign.CampaignCategory categoryEnum = Campaign.CampaignCategory.valueOf(category);
            return categoryEnum.getDisplayName();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing category: " + category);
            return "Khác";
        }
    }
}