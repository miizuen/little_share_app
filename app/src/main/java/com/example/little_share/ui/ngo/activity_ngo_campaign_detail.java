package com.example.little_share.ui.ngo;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

public class activity_ngo_campaign_detail extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCampaignTitle, tvCampaignStatus, tvCampaignId;
    private TextView tvCurrentVolunteer, tvTargetVolunteer, tvPointsReward, tvCampaignCategory;
    private TextView tvDuration, tvProgress;
    private MaterialButtonToggleGroup tabGroup;
    private MaterialButton tabInfo, tabFinance, tabVolunteer;
    private Campaign campaign;
    private String campaignId;
    private CampaignRepository repository;
    private FirebaseFirestore db;
    private static final String TAG = "NGOCampaignDetail";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_campaign_detail);

        repository = new CampaignRepository();
        db = FirebaseFirestore.getInstance();
        campaignId = getIntent().getStringExtra("campaignId");

        initViews();
        setupListeners();
        loadCampaignData();
        
        // Xóa dữ liệu test nếu có
        cleanupTestDataForCampaign();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadCampaignData() {
        if (campaignId == null) return;

        repository.getCampaignById(campaignId).observe(this, campaign -> {
            if (campaign != null) {
                this.campaign = campaign;
                displayCampaignData();

                // CHỈ HIỂN THỊ TAB ĐẦU TIÊN SAU KHI CÓ DATA
                if (getSupportFragmentManager().findFragmentById(R.id.tabContainer) == null) {
                    showFragment(new frm_ngo_campaign_overall());
                    tabInfo.setChecked(true);
                }
            }
        });
    }

    private void displayCampaignData() {
        tvCampaignTitle.setText(campaign.getName());
        tvCampaignStatus.setText(campaign.getStatusEnum().getDisplayName());
        tvCampaignId.setText(campaign.getId());

        tvCurrentVolunteer.setText(String.valueOf(campaign.getCurrentVolunteers()));
        tvTargetVolunteer.setText(String.valueOf(campaign.getMaxVolunteers()));
        tvPointsReward.setText(String.valueOf(campaign.getPointsReward()));
        tvCampaignCategory.setText(campaign.getCategory() != null ? campaign.getCategory() : "");

        //Load số tình nguyện viên chờ duyệt thật từ Firebase
        loadPendingVolunteersCount(campaignId);

        int progress = campaign.getProgressPercentage();
        tvProgress.setText(progress + "%");

        Bundle bundle = new Bundle();
        bundle.putSerializable("campaign", campaign);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.tabContainer);
        if (currentFragment != null) {
            currentFragment.setArguments(bundle);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        tabGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if(isChecked){
                if(checkedId == R.id.tabInfo){
                    showFragment(new frm_ngo_campaign_overall());
                } else if (checkedId == R.id.tabFinance) {
                    showFragment(new frm_ngo_finance_campagin_detail());
                } else if (checkedId == R.id.tabVolunteer) {
                    showFragment(new frm_ngo_volunteer_campaign_detail());
                }
            }
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCampaignTitle = findViewById(R.id.tvCampaignTitle);
        tvCampaignStatus = findViewById(R.id.tvCampaignStatus);
        tvCampaignId = findViewById(R.id.tvCampaignId);
        tvCurrentVolunteer = findViewById(R.id.tvCurrentVolunteer);
        tvTargetVolunteer = findViewById(R.id.tvTargetVolunteer);
        tvPointsReward = findViewById(R.id.tvPointsReward);
        tvDuration = findViewById(R.id.tvDuration);
        tvProgress = findViewById(R.id.tvProgress);
        tvCampaignCategory = findViewById(R.id.tvCampaignCategory);

        tabGroup = findViewById(R.id.tabGroup);
        tabInfo = findViewById(R.id.tabInfo);
        tabFinance = findViewById(R.id.tabFinance);
        tabVolunteer = findViewById(R.id.tabVolunteer);
    }

    private void showFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("campaign", campaign);
        bundle.putString("campaignId", campaignId);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.tabContainer, fragment)
                .commit();
    }

    // Load số tình nguyện viên chờ duyệt cho chiến dịch này - CHỈ DỮ LIỆU THẬT
    private void loadPendingVolunteersCount(String campaignId) {
        if (campaignId == null) {
            Log.e(TAG, "Campaign ID is null");
            tvDuration.setText("0");
            return;
        }

        Log.d(TAG, "Loading pending volunteers for campaign: " + campaignId);

        db.collection("volunteer_registrations")
                .whereEqualTo("campaignId", campaignId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading pending volunteers: " + error.getMessage());
                        tvDuration.setText("0");
                        return;
                    }

                    java.util.Set<String> uniquePendingVolunteers = new java.util.HashSet<>();
                    if (snapshots != null) {
                        Log.d(TAG, "Found " + snapshots.size() + " pending registrations for campaign");
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String userId = doc.getString("userId");
                            String status = doc.getString("status");
                            String docCampaignId = doc.getString("campaignId");
                            Log.d(TAG, "Pending registration - userId: " + userId + ", status: " + status + ", campaignId: " + docCampaignId);
                            if (userId != null) {
                                uniquePendingVolunteers.add(userId);
                            }
                        }
                    } else {
                        Log.d(TAG, "No pending registrations found for campaign: " + campaignId);
                    }

                    int totalPendingVolunteers = uniquePendingVolunteers.size();
                    Log.d(TAG, "Real pending volunteers count for campaign: " + totalPendingVolunteers);

                    // Cập nhật UI với dữ liệu thật
                    tvDuration.setText(String.valueOf(totalPendingVolunteers));
                    Log.d(TAG, "✅ Updated pending volunteers count: " + totalPendingVolunteers);
                });
    }
    
    // Xóa tất cả dữ liệu test/ảo cho chiến dịch
    private void cleanupTestDataForCampaign() {
        Log.d(TAG, "Cleaning up test data for campaigns...");
        
        // Xóa tất cả volunteer registrations có userId bắt đầu bằng "test"
        db.collection("volunteer_registrations")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null) {
                        int deletedCount = 0;
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String userId = doc.getString("userId");
                            if (userId != null && (userId.startsWith("test") || userId.contains("test_"))) {
                                doc.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "✅ Deleted test registration: " + doc.getId());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "❌ Failed to delete test registration: " + e.getMessage());
                                        });
                                deletedCount++;
                            }
                        }
                        Log.d(TAG, "Cleaned up " + deletedCount + " test registrations");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading registrations for cleanup: " + e.getMessage());
                });
    }

    
}