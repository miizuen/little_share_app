package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class activity_voluteer_campaign_detail extends AppCompatActivity {

    private Campaign campaign;

    private ImageView imgFood;
    private TextView tvCampaignTitle, tvCategoryBadge, tvProgressNumber;
    private TextView tvOrganization, tvDescription;
    private TextView tvTime, tvLocation, tvActivity, tvRequirements;
    private ProgressBar progressBar;
    private Button btnRegister;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voluteer_campaign_detail);

        db = FirebaseFirestore.getInstance();

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
        if (getIntent() != null && getIntent().hasExtra("campaign")) {
            campaign = (Campaign) getIntent().getSerializableExtra("campaign");
        }
    }

    /* ====================== LOAD DATA ====================== */

    private void bindData() {

        // Ảnh chiến dịch
        if (campaign.getImageUrl() != null && !campaign.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(campaign.getImageUrl())
                    .placeholder(R.drawable.img_nauanchoem)
                    .error(R.drawable.img_nauanchoem)
                    .centerCrop()
                    .into(imgFood);
        } else {
            imgFood.setImageResource(R.drawable.img_nauanchoem);
        }

        tvCampaignTitle.setText(campaign.getName());

        // Set category badge với logic tương tự CampaignAdapter
        android.util.Log.d("CATEGORY_DEBUG", "Setting category badge for campaign: " + campaign.getName());
        android.util.Log.d("CATEGORY_DEBUG", "Raw category value: " + campaign.getCategory());

        try {
            String categoryStr = campaign.getCategory();
            Campaign.CampaignCategory category;

            // Nếu là string thô từ Firestore
            if (categoryStr != null) {
                category = Campaign.CampaignCategory.valueOf(categoryStr);
                android.util.Log.d("CATEGORY_DEBUG", "Converted to enum: " + category);
                tvCategoryBadge.setText(category.getDisplayName());
                android.util.Log.d("CATEGORY_DEBUG", "Display name: " + category.getDisplayName());
            } else {
                category = campaign.getCategoryEnum();
                if (category != null) {
                    tvCategoryBadge.setText(category.getDisplayName());
                    android.util.Log.d("CATEGORY_DEBUG", "Using getCategoryEnum: " + category.getDisplayName());
                } else {
                    tvCategoryBadge.setText("Chưa phân loại");
                    android.util.Log.w("CATEGORY_DEBUG", "No category found");
                }
            }
        } catch (Exception e) {
            // Fallback nếu có lỗi
            android.util.Log.e("CATEGORY_DEBUG", "Error setting category: " + e.getMessage());
            String cat = campaign.getCategory();
            tvCategoryBadge.setText(cat != null ? getCategoryDisplayName(cat) : "Chưa phân loại");
        }

        int progress = campaign.getProgressPercentage();
        tvProgressNumber.setText(progress + "%");
        progressBar.setProgress(progress);

        tvRequirements.setText(
                campaign.getRequirements() != null && !campaign.getRequirements().isEmpty()
                        ? campaign.getRequirements()
                        : "Không có yêu cầu đặc biệt"
        );

        // Hiển thị description thực tế của chiến dịch
        android.util.Log.d("CATEGORY_DEBUG", "Campaign description: " + campaign.getDescription());
        android.util.Log.d("CATEGORY_DEBUG", "Campaign category: " + campaign.getCategory());

        tvDescription.setText(
                campaign.getDescription() != null && !campaign.getDescription().isEmpty()
                        ? campaign.getDescription()
                        : "Không có mô tả"
        );


        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvTime.setText(
                sdf.format(campaign.getStartDate()) + " - " +
                        sdf.format(campaign.getEndDate())
        );

        tvLocation.setText(
                campaign.getSpecificLocation() != null
                        ? campaign.getSpecificLocation()
                        : campaign.getLocation()
        );

        tvActivity.setText(
                campaign.getActivities() != null
                        ? campaign.getActivities()
                        : "Tham gia tình nguyện đa dạng"
        );

        // ===== TỔ CHỨC =====
        if (campaign.getOrganizationName() != null && !campaign.getOrganizationName().isEmpty()) {
            tvOrganization.setText(campaign.getOrganizationName());
        } else {
            loadOrganizationName();
        }


        btnRegister.setOnClickListener(v -> checkAlreadyRegistered());
    }

    private void loadOrganizationName() {
        if (campaign.getOrganizationId() == null) {
            tvOrganization.setText("Chưa có thông tin");
            return;
        }

        db.collection("organization")
                .document(campaign.getOrganizationId())
                .get()
                .addOnSuccessListener(doc -> {
                    tvOrganization.setText(
                            doc.exists() && doc.getString("name") != null
                                    ? doc.getString("name")
                                    : "Chưa có thông tin"
                    );
                })
                .addOnFailureListener(e -> tvOrganization.setText("Chưa có thông tin"));
    }



    /* ====================== REGISTER ====================== */

    private void checkAlreadyRegistered() {
        checkCampaignHasRoles();
    }

    private void checkCampaignHasRoles() {
        db.collection("campaign_roles")
                .whereEqualTo("campaignId", campaign.getId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    Intent intent;
                    if (snapshot.isEmpty()) {
                        intent = new Intent(this, activity_volunteer_role_registration.class);
                    } else {
                        intent = new Intent(this, activity_volunteer_role_selection.class);
                        intent.putExtra("campaignId", campaign.getId());
                        intent.putExtra("campaignName", campaign.getName());
                    }
                    intent.putExtra("campaign", campaign);
                    startActivity(intent);
                });
    }

    /* ====================== INIT VIEW ====================== */

    private void initView() {
        imgFood = findViewById(R.id.imgFood);
        tvCampaignTitle = findViewById(R.id.tvCampaignTitle);
        tvCategoryBadge = findViewById(R.id.tvCategoryBadge);
        tvProgressNumber = findViewById(R.id.tvProgressNumber);
        progressBar = findViewById(R.id.progressBar);
        tvOrganization = findViewById(R.id.tvOrganization);
        tvDescription = findViewById(R.id.tvDescription);
        tvTime = findViewById(R.id.tvTime);
        tvLocation = findViewById(R.id.tvLocation);
        tvActivity = findViewById(R.id.tvActivity);
        tvRequirements = findViewById(R.id.tvRequirements);
        btnRegister = findViewById(R.id.btnRegister);

        ImageView btnBack = findViewById(R.id.btn_Back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private String getCategoryDisplayName(String category) {
        if (category == null) return "Chưa phân loại";

        android.util.Log.d("CATEGORY_DEBUG", "Converting category: " + category);

        switch (category.toUpperCase()) {
            case "EDUCATION":
                return "Giáo dục";
            case "FOOD":
                return "Nấu ăn và dinh dưỡng";
            case "ENVIRONMENT":
                return "Môi trường";
            case "HEALTH":
                return "Y tế";
            case "URGENT":
                return "Khẩn cấp";
            default:
                android.util.Log.w("CATEGORY_DEBUG", "Unknown category: " + category);
                return category;
        }
    }

}
