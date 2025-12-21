package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class activity_voluteer_campaign_detail extends AppCompatActivity {

    private Campaign campaign;
    private ImageView imgFood;
    private TextView tvCampaignTitle, tvCategoryBadge, tvProgressNumber;
    private TextView tvOrganization, tvSponsor, tvDescription;
    private TextView tvTime, tvLocation, tvActivity;
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
        if (getIntent().hasExtra("campaign")) {
            campaign = (Campaign) getIntent().getSerializableExtra("campaign");
        }
    }

    private void checkAlreadyRegistered() {
        // Cho phép TNV đăng ký nhiều lần trong cùng chiến dịch
        // Việc kiểm tra trùng vai trò/ca/ngày sẽ được thực hiện ở màn hình đăng ký
        checkCampaignHasRoles();
    }

    private void checkCampaignHasRoles() {
        db.collection("campaign_roles")
                .whereEqualTo("campaignId", campaign.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // KHÔNG có vai trò → Chuyển đến đăng ký trực tiếp
                        Intent intent = new Intent(this, activity_volunteer_role_registration.class);
                        intent.putExtra("campaign", campaign);
                        startActivity(intent);
                    } else {
                        // CÓ vai trò → Chuyển đến chọn vai trò
                        Intent intent = new Intent(this, activity_volunteer_role_selection.class);
                        intent.putExtra("campaignId", campaign.getId());
                        intent.putExtra("campaignName", campaign.getName());
                        intent.putExtra("campaign", campaign);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(this, "Lỗi kiểm tra thông tin chiến dịch", android.widget.Toast.LENGTH_SHORT).show();
                });
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

        // Kiểm tra đã đăng ký chưa trước khi cho đăng ký
        btnRegister.setOnClickListener(v -> {
            checkAlreadyRegistered();
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

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}
