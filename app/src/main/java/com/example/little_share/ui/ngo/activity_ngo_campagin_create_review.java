package com.example.little_share.ui.ngo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.Campain.CampaignRole;
import com.example.little_share.data.models.Shift;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.data.repositories.NotificationRepository;
import com.example.little_share.helper.ImgBBUploader;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class activity_ngo_campagin_create_review extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextView tvCampaignName, tvCampaignDescription, tvCategory;
    private ImageView ivCampaignImage;
    private TextView tvStartDate, tvEndDate, tvLocation, tvSpecificLocation, tvYeuCau;
    private CardView layoutSponsorSection, layoutRolesSection, layoutShiftsSection, layoutYeuCauSection;
    private LinearLayout layoutRolesContainer, layoutShiftsContainer;
    private SwitchMaterial switchSponsorDisplay;
    private TextView tvBudget;
    private MaterialButton btnCreateCampaign;

    // Data
    private String campaignName, campaignDescription, category, imageUrl;
    private String location, specificLocation, yeuCau, startDate, endDate;
    private boolean needsSponsor;
    private String targetBudget, budgetPurpose;
    private List<CampaignRole> roleList;
    private List<Shift> shiftList;
    private Map<String, Map<String, Integer>> shiftRoleAssignments;
    private CampaignRepository repository;
    private FirebaseFirestore db;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_campagin_create_review);

        repository = new CampaignRepository();
        db = FirebaseFirestore.getInstance();

        receiveData();
        initViews();
        setupToolbar();
        displayData();
        setupButton();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void receiveData() {
        Intent intent = getIntent();
        campaignName = intent.getStringExtra("campaignName");
        campaignDescription = intent.getStringExtra("campaignDescription");
        category = intent.getStringExtra("category");
        imageUrl = intent.getStringExtra("imageUrl");
        location = intent.getStringExtra("location");
        specificLocation = intent.getStringExtra("specificLocation");
        yeuCau = intent.getStringExtra("yeuCau");
        startDate = intent.getStringExtra("startDate");
        endDate = intent.getStringExtra("endDate");
        needsSponsor = intent.getBooleanExtra("needsSponsor", false);
        targetBudget = intent.getStringExtra("targetBudget");
        budgetPurpose = intent.getStringExtra("budgetPurpose");
        roleList = (List<CampaignRole>) intent.getSerializableExtra("roles");
        shiftList = (List<Shift>) intent.getSerializableExtra("shifts");
        shiftRoleAssignments = (Map<String, Map<String, Integer>>)
                intent.getSerializableExtra("shiftRoleAssignments");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvCampaignDescription = findViewById(R.id.tvCampaignDescription);
        tvCategory = findViewById(R.id.tvCategory);
        ivCampaignImage = findViewById(R.id.ivCampaignImage);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvLocation = findViewById(R.id.tvLocation);
        tvSpecificLocation = findViewById(R.id.tvSpecificLocation);
        tvYeuCau = findViewById(R.id.tvYeuCau);

        // CardView sections
        layoutSponsorSection = findViewById(R.id.layoutSponsorSection);
        layoutRolesSection = findViewById(R.id.layoutRolesSection);
        layoutShiftsSection = findViewById(R.id.layoutShiftsSection);
        layoutYeuCauSection = findViewById(R.id.layoutYeuCauSection);

        switchSponsorDisplay = findViewById(R.id.switchSponsorDisplay);
        tvBudget = findViewById(R.id.tvBudget);

        // LinearLayout containers inside CardViews
        layoutRolesContainer = findViewById(R.id.layoutRolesContainer);
        layoutShiftsContainer = findViewById(R.id.layoutShiftsContainer);

        btnCreateCampaign = findViewById(R.id.btnCreateCampaign);

        // ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void displayData() {
        // Basic info
        tvCampaignName.setText(campaignName);
        tvCampaignDescription.setText(campaignDescription);
        tvCategory.setText(category);
        tvStartDate.setText(startDate);
        tvEndDate.setText(endDate);
        tvLocation.setText(location);
        tvSpecificLocation.setText(specificLocation != null && !specificLocation.isEmpty()
                ? specificLocation : "Không có");

        // Yeu Cau section
        if (yeuCau != null && !yeuCau.trim().isEmpty()) {
            layoutYeuCauSection.setVisibility(View.VISIBLE);
            tvYeuCau.setText(yeuCau);
        } else {
            layoutYeuCauSection.setVisibility(View.GONE);
        }

        // Image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(imageUrl))
                    .placeholder(R.drawable.img_nauanchoem)
                    .error(R.drawable.img_nauanchoem)
                    .into(ivCampaignImage);
        }

        // Sponsor section
        if (needsSponsor) {
            layoutSponsorSection.setVisibility(View.VISIBLE);
            switchSponsorDisplay.setChecked(true);
            tvBudget.setText(formatMoney(targetBudget) + " VNĐ");
        } else {
            layoutSponsorSection.setVisibility(View.GONE);
        }

        // Display Roles
        if (roleList != null && !roleList.isEmpty()) {
            layoutRolesSection.setVisibility(View.VISIBLE);
            displayRoles();
        } else {
            layoutRolesSection.setVisibility(View.GONE);
        }

        // Display Shifts
        if (shiftList != null && !shiftList.isEmpty()) {
            layoutShiftsSection.setVisibility(View.VISIBLE);
            displayShifts();
        } else {
            layoutShiftsSection.setVisibility(View.GONE);
        }
    }

    private void displayRoles() {
        layoutRolesContainer.removeAllViews();

        for (CampaignRole role : roleList) {
            View roleView = LayoutInflater.from(this).inflate(R.layout.item_review_role, layoutRolesContainer, false);

            TextView tvRoleName = roleView.findViewById(R.id.tvRoleName);
            TextView tvRoleDescription = roleView.findViewById(R.id.tvRoleDescription);
            TextView tvRolePoints = roleView.findViewById(R.id.tvRolePoints);

            tvRoleName.setText(role.getRoleName());
            tvRoleDescription.setText(role.getDescription());
            tvRolePoints.setText(String.valueOf(role.getPointsReward()));

            layoutRolesContainer.addView(roleView);
        }
    }

    private void displayShifts() {
        layoutShiftsContainer.removeAllViews();

        for (Shift shift : shiftList) {
            View shiftView = LayoutInflater.from(this).inflate(R.layout.item_review_shift, layoutShiftsContainer, false);

            TextView tvShiftName = shiftView.findViewById(R.id.tvShiftName);
            TextView tvShiftTime = shiftView.findViewById(R.id.tvShiftTime);
            LinearLayout layoutRoleAssignments = shiftView.findViewById(R.id.layoutRoleAssignments);

            tvShiftName.setText(shift.getShiftName());
            tvShiftTime.setText(shift.getTimeRange());

            // Hiển thị phân bổ vai trò cho ca này
            Map<String, Integer> assignments = shiftRoleAssignments.get(shift.getId());
            if (assignments != null && !assignments.isEmpty()) {
                for (Map.Entry<String, Integer> entry : assignments.entrySet()) {
                    if (entry.getValue() > 0) {
                        View assignmentView = LayoutInflater.from(this).inflate(
                                R.layout.item_review_role_assignment, layoutRoleAssignments, false);

                        TextView tvRoleName = assignmentView.findViewById(R.id.tvRoleName);
                        TextView tvVolunteerCount = assignmentView.findViewById(R.id.tvVolunteerCount);

                        tvRoleName.setText(entry.getKey());
                        tvVolunteerCount.setText(entry.getValue() + " TNV");

                        layoutRoleAssignments.addView(assignmentView);
                    }
                }
            }

            layoutShiftsContainer.addView(shiftView);
        }
    }

    private String formatMoney(String amount) {
        try {
            double value = Double.parseDouble(amount);
            return String.format(Locale.getDefault(), "%,.0f", value).replace(",", ".");
        } catch (Exception e) {
            return amount;
        }
    }

    private void setupButton() {
        btnCreateCampaign.setOnClickListener(v -> createCampaign());
    }



    private void createCampaign() {
        btnCreateCampaign.setEnabled(false);
        btnCreateCampaign.setText("Đang tạo....");

        Campaign campaign = new Campaign();
        campaign.setName(campaignName);
        campaign.setDescription(campaignDescription);
        campaign.setCategory(category);
        campaign.setLocation(location);
        campaign.setSpecificLocation(specificLocation);
        campaign.setRequirements(yeuCau);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            campaign.setStartDate(sdf.parse(startDate));
            campaign.setEndDate(sdf.parse(endDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        campaign.setNeedsSponsor(needsSponsor);
        if (needsSponsor) {
            try {
                campaign.setTargetBudget(Double.parseDouble(targetBudget));
            } catch (Exception e) {
                campaign.setTargetBudget(0);
            }
            campaign.setBudgetPurpose(budgetPurpose);
        }

        int totalVolunteers = 0;
        int totalPoints = 0;

        if (shiftList != null && !shiftList.isEmpty()) {
            for (Shift shift : shiftList) {
                totalVolunteers += shift.getMaxVolunteers();
            }

            if (roleList != null && !roleList.isEmpty()) {
                for (CampaignRole role : roleList) {
                    totalPoints += role.getPointsReward();
                }
                totalPoints = totalPoints / roleList.size();
            }
        }

        campaign.setMaxVolunteers(totalVolunteers);
        campaign.setPointsReward(totalPoints);
        campaign.setStatus(Campaign.CampaignStatus.UPCOMING.name());

        // ✅ Upload image với ImgBB
        if (imageUrl != null && imageUrl.startsWith("content://")) {
            uploadImageAndCreateCampaign(campaign);
        } else {
            campaign.setImageUrl(imageUrl);
            saveCampaignToFirestore(campaign);
        }
    }

    // ✅ Upload ảnh lên ImgBB (FREE, không cần Firebase Storage)
    private void uploadImageAndCreateCampaign(Campaign campaign) {
        progressDialog.setMessage("Đang tải ảnh lên...");
        progressDialog.show();

        ImgBBUploader.uploadImage(this, Uri.parse(imageUrl), new ImgBBUploader.UploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                android.util.Log.d("ImageUpload", "✅ Upload thành công: " + imageUrl);
                progressDialog.dismiss();

                campaign.setImageUrl(imageUrl);
                saveCampaignToFirestore(campaign);
            }

            @Override
            public void onFailure(String error) {
                android.util.Log.e("ImageUpload", "❌ Upload thất bại: " + error);
                progressDialog.dismiss();

                // Hiển thị dialog hỏi user
                new AlertDialog.Builder(activity_ngo_campagin_create_review.this)
                        .setTitle("Upload ảnh thất bại")
                        .setMessage("Không thể tải ảnh lên.\n\n" + error + "\n\nBạn có muốn tạo chiến dịch không có ảnh?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            campaign.setImageUrl(null);
                            saveCampaignToFirestore(campaign);
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> {
                            btnCreateCampaign.setEnabled(true);
                            btnCreateCampaign.setText("Tạo chiến dịch");
                        })
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onProgress(int progress) {
                progressDialog.setMessage("Đang tải ảnh lên... " + progress + "%");
            }
        });
    }

    private void saveCampaignToFirestore(Campaign campaign) {
    progressDialog.setMessage("Đang tạo chiến dịch...");
    progressDialog.show();

    // Tính maxVolunteers cho mỗi role TRƯỚC khi lưu
    if (roleList != null && !roleList.isEmpty()) {
        Map<String, Integer> totalVolunteersPerRole = new HashMap<>();
        
        if (shiftRoleAssignments != null) {
            for (Map<String, Integer> shiftAssignment : shiftRoleAssignments.values()) {
                for (Map.Entry<String, Integer> entry : shiftAssignment.entrySet()) {
                    String roleName = entry.getKey();
                    int count = entry.getValue();
                    totalVolunteersPerRole.put(roleName, 
                        totalVolunteersPerRole.getOrDefault(roleName, 0) + count);
                }
            }
        }
        
        // Set maxVolunteers cho từng role
        for (CampaignRole role : roleList) {
            int maxVol = totalVolunteersPerRole.getOrDefault(role.getRoleName(), 0);
            role.setMaxVolunteers(maxVol);
        }
        
        campaign.setRoles(roleList);
    }

        repository.createCampaign(campaign, new CampaignRepository.OnCampaignListener() {
            @Override
            public void onSuccess(String campaignId) {
                // Lưu roles và shifts nếu có
                if (roleList != null && !roleList.isEmpty()) {
                    saveRolesAndShifts(campaignId);
                } else {
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    db.collection("organization").document(currentUserId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                String orgName = doc.exists() && doc.getString("name") != null ?
                                        doc.getString("name") : "Tổ chức từ thiện";
                                onCampaignCreatedSuccess(campaignId, campaignName, orgName);
                            })
                            .addOnFailureListener(e -> {
                                onCampaignCreatedSuccess(campaignId, campaignName, "Tổ chức từ thiện");
                            });
                }
            }

            @Override
            public void onFailure(String error) {
                progressDialog.dismiss();
                btnCreateCampaign.setEnabled(true);
                btnCreateCampaign.setText("Tạo chiến dịch");
                Toast.makeText(activity_ngo_campagin_create_review.this,
                        "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveRolesAndShifts(String campaignId) {
           Map<String, Integer> totalVolunteersPerRole = new HashMap<>();
    
    if (shiftRoleAssignments != null) {
        for (Map<String, Integer> shiftAssignment : shiftRoleAssignments.values()) {
            for (Map.Entry<String, Integer> entry : shiftAssignment.entrySet()) {
                String roleName = entry.getKey();
                int count = entry.getValue();
                totalVolunteersPerRole.put(roleName, 
                    totalVolunteersPerRole.getOrDefault(roleName, 0) + count);
            }
        }
    }
        // Lưu roles
        for (CampaignRole role : roleList) {
        role.setCampaignId(campaignId);
        // Set maxVolunteers từ tổng đã tính
        int maxVol = totalVolunteersPerRole.getOrDefault(role.getRoleName(), 0);
        role.setMaxVolunteers(maxVol);
        db.collection("campaign_roles").add(role);
    }

        // Lưu shifts
        if (shiftList != null) {
            for (Shift shift : shiftList) {
                shift.setCampaignId(campaignId);
                db.collection("shifts").add(shift)
                        .addOnSuccessListener(docRef -> {
                            // Lưu role assignments
                            String shiftId = docRef.getId();
                            Map<String, Integer> assignments = shiftRoleAssignments.get(shift.getId());
                            if (assignments != null) {
                                saveShiftRoleAssignments(shiftId, assignments);
                            }
                        });
            }
        }
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("organization").document(currentUserId)
                        .get()
                                .addOnSuccessListener(doc -> {
                                    String orgName = doc.exists() && doc.getString("name") != null ?
                                            doc.getString("name") : "Tổ chức từ thiện";
                                    onCampaignCreatedSuccess(campaignId, campaignName, orgName);
                                })
                                .addOnFailureListener(e -> {
                                    onCampaignCreatedSuccess(campaignId, campaignName, "Tổ chức từ thiện");
                                });
    }

    private void saveShiftRoleAssignments(String shiftId, Map<String, Integer> assignments) {
        for (Map.Entry<String, Integer> entry : assignments.entrySet()) {
            Map<String, Object> assignment = new HashMap<>();
            assignment.put("shiftId", shiftId);
            assignment.put("roleName", entry.getKey());
            assignment.put("maxVolunteers", entry.getValue());

            db.collection("shift_role_assignments").add(assignment);
        }
    }

    private void onCampaignCreatedSuccess(String campaignId, String campaginName, String orgName) {

        new NotificationRepository().notifyVolunteersAboutNewCampaign
                (
                campaignId,
                campaginName,
                orgName,
                new NotificationRepository.OnNotificationListener() {
                    @Override
                    public void onSuccess(String result) {
                        android.util.Log.d("Notification", "Gửi thành công: " + result);
                    }

                    @Override
                    public void onFailure(String error) {
                        android.util.Log.e("Notification", "Lỗi gửi thông báo: " + error);
                    }
                }
        );
        progressDialog.dismiss();
        Toast.makeText(this, "Tạo chiến dịch thành công!", Toast.LENGTH_SHORT).show();

        // Chuyển đến trang chính NGO
        Intent intent = new Intent(this, activity_ngo_main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}