package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class activity_voluteer_campaign_detail extends AppCompatActivity {

    private Campaign campaign;
    private ImageView imgFood;
    private TextView tvCampaignTitle, tvCategoryBadge, tvProgressNumber;
    private TextView tvOrganization, tvSponsor, tvDescription;
    private TextView tvTime, tvLocation, tvActivity;
    private ProgressBar progressBar;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voluteer_campaign_detail);

        initView();
        getDataFromIntent();
        if (campaign != null) {
            bindData();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void getDataFromIntent() {
        if (getIntent().hasExtra("campaign")) {
            campaign = (Campaign) getIntent().getSerializableExtra("campaign");
        }
    }

    private void bindData() {
        tvCampaignTitle.setText(campaign.getName());
        try {
            tvCategoryBadge.setText(campaign.getCategoryEnum().getDisplayName());
        } catch (Exception e) {
            tvCategoryBadge.setText(campaign.getCategory());
        }

        int progress = campaign.getProgressPercentage();
        tvProgressNumber.setText(progress + "%");
        progressBar.setProgress(progress);

        tvOrganization.setText(campaign.getOrganizationName());
        tvSponsor.setText("Quỹ từ thiện Nuôi Em");
        tvDescription.setText(campaign.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateRange = sdf.format(campaign.getStartDate()) + " - " + sdf.format(campaign.getEndDate());
        tvTime.setText(dateRange);

        tvLocation.setText(campaign.getSpecificLocation() != null ? campaign.getSpecificLocation() : campaign.getLocation());
        tvActivity.setText(campaign.getActivities() != null ? campaign.getActivities() : "Tham gia tình nguyện đa dạng");

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(activity_voluteer_campaign_detail.this, activity_volunteer_role_selection.class);
            intent.putExtra("campaignId", campaign.getId());
            intent.putExtra("campaignName", campaign.getName());

            Log.d("CampaignDetail", "Campaign ID: " + campaign.getId());
            Log.d("CampaignDetail", "Roles count: " + (campaign.getRoles() != null ? campaign.getRoles().size() : "null"));

            if (campaign.getRoles() != null && !campaign.getRoles().isEmpty()) {
                intent.putExtra("roles", (Serializable) new ArrayList<>(campaign.getRoles()));
            }
            startActivity(intent);
        });
    }

    private void initView() {
        imgFood = findViewById(R.id.imgFood);
        tvCampaignTitle = findViewById(R.id.tvCampaignTitle);
        tvCategoryBadge = findViewById(R.id.tvCategoryBadge);
        tvProgressNumber = findViewById(R.id.tvProgressNumber);
        progressBar = findViewById(R.id.progressBar);
        tvOrganization = findViewById(R.id.tvOrganization);
        tvSponsor = findViewById(R.id.tvSponsor);
        tvDescription = findViewById(R.id.tvDescription);
        tvTime = findViewById(R.id.tvTime);
        tvLocation = findViewById(R.id.tvLocation);
        tvActivity = findViewById(R.id.tvActivity);
        btnRegister = findViewById(R.id.btnRegister);
    }
}
