package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.Donation;
import com.example.little_share.data.models.DonationItem;
import com.example.little_share.data.models.Organization;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.data.repositories.DonationRepository;
import com.example.little_share.data.repositories.OrganizationRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class activity_volunteer_donation_confirm extends AppCompatActivity {

    private static final String TAG = "DonationConfirm";

    private DonationRepository donationRepository;
    private CampaignRepository campaignRepository;
    private OrganizationRepository organizationRepository;
    private MaterialButton btnConfirm;
    private FirebaseFirestore db;

    // Data from previous activity
    private String donationType;
    private String category;
    private int quantity;
    private String condition;
    private int points;
    private String note;

    // Selected location data
    private String selectedOrganizationId;
    private String selectedOrganizationName;
    private String selectedLocation;

    // Available campaigns
    private List<Campaign> availableCampaigns = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_donation_confirm);

        // Initialize repositories
        donationRepository = new DonationRepository();
        campaignRepository = new CampaignRepository();
        organizationRepository = new OrganizationRepository();
        db = FirebaseFirestore.getInstance();


        // Ánh xạ views
        TextView tvCampaignName = findViewById(R.id.tvCampaignName);
        TextView tvType = findViewById(R.id.tvType);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvQuantity = findViewById(R.id.tvQuantity);
        TextView tvCondition = findViewById(R.id.tvCondition);
        TextView tvTotalPoints = findViewById(R.id.tvTotalPoints);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        donationType = intent.getStringExtra("DONATION_TYPE");
        category = intent.getStringExtra("CATEGORY");
        quantity = intent.getIntExtra("QUANTITY", 1);
        condition = intent.getStringExtra("CONDITION");
        points = intent.getIntExtra("POINTS", 0);
        note = intent.getStringExtra("NOTE");

        // Convert donation type to display text
        String typeText = convertDonationTypeToText(donationType);

        // Update UI
        tvCampaignName.setText(typeText);
        tvType.setText(typeText);
        tvCategory.setText(category);
        tvQuantity.setText(String.valueOf(quantity));
        tvCondition.setText(condition != null ? condition.replace("(100%)", "100%").replace("80%-90%", "80-90%").replace("60%-70%", "60-70%") : "Không xác định");
        tvTotalPoints.setText("+" + points);

        // Setup buttons
        findViewById(R.id.btnBack2).setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> confirmDonation());

        loadAllOrganizations();

        // Setup location selection listeners
        setupLocationListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadAllOrganizations() {
        db.collection("organization")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    availableCampaigns.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Organization org = doc.toObject(Organization.class);
                        org.setId(doc.getId());

                        // Convert Organization thành Campaign để hiển thị
                        Campaign campaign = new Campaign();
                        campaign.setOrganizationId(org.getId());
                        campaign.setOrganizationName(org.getName());
                        campaign.setLocation(extractCityFromAddress(org.getAddress()));
                        campaign.setSpecificLocation(org.getAddress());

                        availableCampaigns.add(campaign);
                    }

                    Log.d(TAG, "Loaded " + availableCampaigns.size() + " organizations");

                    // Update UI
                    updateLocationItems();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading organizations: " + e.getMessage());
                });
    }

    // Helper method để extract thành phố từ địa chỉ
    private String extractCityFromAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.isEmpty()) {
            return "Chưa xác định";
        }

        // Tách địa chỉ theo dấu phẩy và lấy phần cuối (thường là tỉnh/thành phố)
        String[] parts = fullAddress.split(",");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1].trim();

            // Loại bỏ các từ không cần thiết
            lastPart = lastPart.replace("tỉnh", "").replace("thành phố", "").replace("TP.", "").trim();

            return lastPart;
        }
        return fullAddress;
    }

    // Method để update UI cho tất cả organizations
    private void updateLocationItems() {
        int maxItems = Math.min(availableCampaigns.size(), 3); // Hiển thị tối đa 3 items

        for (int i = 0; i < maxItems; i++) {
            int includeId = getLocationIncludeId(i);
            updateLocationItem(includeId, i);
        }

        // Ẩn các item không sử dụng
        for (int i = maxItems; i < 3; i++) {
            int includeId = getLocationIncludeId(i);
            View locationView = findViewById(includeId);
            if (locationView != null) {
                locationView.setVisibility(View.GONE);
            }
        }
    }

    private int getLocationIncludeId(int index) {
        switch (index) {
            case 0: return R.id.includeLocation1;
            case 1: return R.id.includeLocation2;
            case 2: return R.id.includeLocation3;
            default: return R.id.includeLocation1;
        }
    }

    private void updateLocationItem(int includeId, int campaignIndex) {
        View locationView = findViewById(includeId);
        if (locationView == null) {
            Log.e(TAG, "Location view is NULL for includeId: " + includeId);
            return;
        }

        TextView tvCampaignTitle = locationView.findViewById(R.id.tv_campaign_title);
        TextView tvCampaignAddress = locationView.findViewById(R.id.tv_campaign_address);
        TextView tvCampaignTime = locationView.findViewById(R.id.tv_campaign_time);

        if (campaignIndex < availableCampaigns.size()) {
            Campaign campaign = availableCampaigns.get(campaignIndex);

            // Kiểm tra null và set dữ liệu
            String orgName = campaign.getOrganizationName();
            String location = campaign.getLocation();
            String specificLocation = campaign.getSpecificLocation();

            if (orgName == null) orgName = "Tổ chức từ thiện";
            if (location == null) location = "Chưa xác định";
            if (specificLocation == null) specificLocation = "Địa chỉ cụ thể chưa cập nhật";

            tvCampaignTitle.setText(orgName + " - " + location);
            tvCampaignAddress.setText(specificLocation);
            tvCampaignTime.setText("8:00 - 17:00 (T2-T7)");

            // Store campaign data in view tag
            locationView.setTag(campaign);
            locationView.setVisibility(View.VISIBLE);

            Log.d(TAG, "Updated location " + campaignIndex + ": " + orgName + " - " + location +
                    " (ID: " + campaign.getOrganizationId() + ")");
        } else {
            // Hide unused location items
            locationView.setVisibility(View.GONE);
        }
    }

    private void setupLocationListeners() {
        // Setup click listeners for location items
        setupLocationClickListener(R.id.includeLocation1, 0);
        setupLocationClickListener(R.id.includeLocation2, 1);
        setupLocationClickListener(R.id.includeLocation3, 2);
    }

    private void setupLocationClickListener(int includeId, int campaignIndex) {
        View locationView = findViewById(includeId);

        if (locationView == null) {
            Log.e(TAG, "Location view is NULL for includeId: " + includeId);
            return;
        }

        locationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "=== Location clicked ===");
                Log.d(TAG, "Include ID: " + includeId + ", Campaign Index: " + campaignIndex);

                // Clear previous selections
                clearLocationSelections();

                // Mark this as selected
                v.setSelected(true);

                // Thay đổi màu CardView
                if (v instanceof MaterialCardView) {
                    MaterialCardView cardView = (MaterialCardView) v;

                    // Đổi màu background thành xanh nhẹ
                    cardView.setCardBackgroundColor(Color.parseColor("#E8F5E8"));
                    // Đổi viền thành xanh đậm
                    cardView.setStrokeColor(Color.parseColor("#4CAF50"));
                    cardView.setStrokeWidth(6);

                    Log.d(TAG, "CardView color changed to green");
                }

                // Get campaign data - DIRECT ACCESS BY INDEX
                if (campaignIndex >= 0 && campaignIndex < availableCampaigns.size()) {
                    Campaign selectedCampaign = availableCampaigns.get(campaignIndex);

                    selectedOrganizationId = selectedCampaign.getOrganizationId();
                    selectedOrganizationName = selectedCampaign.getOrganizationName();
                    selectedLocation = selectedCampaign.getLocation();

                    if (selectedCampaign.getSpecificLocation() != null) {
                        selectedLocation += " - " + selectedCampaign.getSpecificLocation();
                    }

                    Log.d(TAG, "=== SELECTION SUCCESS ===");
                    Log.d(TAG, "ID: [" + selectedOrganizationId + "]");
                    Log.d(TAG, "Name: [" + selectedOrganizationName + "]");
                    Log.d(TAG, "Location: [" + selectedLocation + "]");

                    Toast.makeText(activity_volunteer_donation_confirm.this,
                            "Đã chọn: " + selectedOrganizationName, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "=== SELECTION FAILED ===");
                    Log.e(TAG, "Invalid campaign index: " + campaignIndex +
                            ", available campaigns: " + availableCampaigns.size());

                    Toast.makeText(activity_volunteer_donation_confirm.this,
                            "Lỗi: Không tìm thấy thông tin địa điểm", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void clearLocationSelections() {
        // Clear all location selections
        clearLocationSelection(R.id.includeLocation1);
        clearLocationSelection(R.id.includeLocation2);
        clearLocationSelection(R.id.includeLocation3);
    }

    private void clearLocationSelection(int includeId) {
        View locationView = findViewById(includeId);
        if (locationView != null) {
            locationView.setSelected(false);

            // Reset CardView về màu mặc định
            if (locationView instanceof MaterialCardView) {
                MaterialCardView cardView = (MaterialCardView) locationView;

                // Reset về màu trắng
                cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                // Reset viền về màu cam
                cardView.setStrokeColor(Color.parseColor("#FFAD00"));
                cardView.setStrokeWidth(4);
            }
        }
    }

    private String convertDonationTypeToText(String type) {
        if (type == null) return "SÁCH VỞ";

        switch (type) {
            case "BOOKS": return "SÁCH VỞ";
            case "CLOTHES": return "QUẦN ÁO";
            case "TOYS": return "ĐỒ CHƠI";
            case "ESSENTIALS": return "NHU YẾU PHẨM";
            default: return "SÁCH VỞ";
        }
    }

    private Donation.DonationType convertStringToEnum(String type) {
        if (type == null) return Donation.DonationType.BOOKS;

        switch (type) {
            case "BOOKS":
            case "BOOK":
                return Donation.DonationType.BOOKS;
            case "CLOTHES":
            case "SHIRT":
                return Donation.DonationType.CLOTHES;
            case "TOYS":
            case "TOY":
                return Donation.DonationType.TOYS;
            case "ESSENTIALS":
                return Donation.DonationType.ESSENTIALS;
            default:
                Log.w(TAG, "Unknown donation type: " + type + ", using BOOKS as default");
                return Donation.DonationType.BOOKS;
        }
    }

    private DonationItem.ItemCondition convertConditionToEnum(String conditionText) {
        if (conditionText == null || conditionText.isEmpty()) {
            Log.w(TAG, "Condition text is null or empty, using ACCEPTABLE as default");
            return DonationItem.ItemCondition.ACCEPTABLE;
        }

        Log.d(TAG, "Converting condition: " + conditionText);

        String lowerCondition = conditionText.toLowerCase().trim();

        if (lowerCondition.contains("mới") || lowerCondition.contains("100%") || lowerCondition.contains("new")) {
            return DonationItem.ItemCondition.NEW;
        } else if (lowerCondition.contains("tốt") || lowerCondition.contains("80") || lowerCondition.contains("90") || lowerCondition.contains("good")) {
            return DonationItem.ItemCondition.GOOD;
        } else if (lowerCondition.contains("khá") || lowerCondition.contains("60") || lowerCondition.contains("70") || lowerCondition.contains("fair")) {
            return DonationItem.ItemCondition.FAIR;
        } else {
            Log.w(TAG, "Unknown condition: " + conditionText + ", using ACCEPTABLE as default");
            return DonationItem.ItemCondition.ACCEPTABLE;
        }
    }

    private void confirmDonation() {
        Log.d(TAG, "========================================");
        Log.d(TAG, "CONFIRM DONATION CLICKED");
        Log.d(TAG, "========================================");
        Log.d(TAG, "selectedOrganizationId: [" + selectedOrganizationId + "]");
        Log.d(TAG, "selectedOrganizationName: [" + selectedOrganizationName + "]");
        Log.d(TAG, "selectedLocation: [" + selectedLocation + "]");
        Log.d(TAG, "========================================");

        // Kiểm tra xem đã chọn địa điểm chưa
        if (selectedOrganizationId == null || selectedOrganizationName == null) {
            Log.w(TAG, "VALIDATION FAILED: NULL values");
            Toast.makeText(this, "Vui lòng chọn địa điểm giao đồ quyên góp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedOrganizationId.trim().isEmpty() || selectedOrganizationName.trim().isEmpty()) {
            Log.w(TAG, "VALIDATION FAILED: EMPTY values");
            Toast.makeText(this, "Vui lòng chọn địa điểm giao đồ quyên góp", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "VALIDATION PASSED - Proceeding with donation");

        // Disable button to prevent double click
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang xử lý...");

        try {
            // Validate inputs
            if (donationType == null || donationType.isEmpty()) {
                throw new IllegalArgumentException("Loại quyên góp không hợp lệ");
            }

            if (category == null || category.isEmpty()) {
                throw new IllegalArgumentException("Danh mục không hợp lệ");
            }

            // Convert donation type to enum using safe method
            Donation.DonationType typeEnum = convertStringToEnum(donationType);

            // Create donation item
            DonationItem item = new DonationItem();
            item.setCategory(category);
            item.setQuantity(quantity);
            item.setCondition(convertConditionToEnum(condition));
            item.setNotes(note != null ? note : "");

            List<DonationItem> items = new ArrayList<>();
            items.add(item);

            Log.d(TAG, "Creating donation with type: " + typeEnum +
                    ", condition: " + item.getCondition() +
                    ", organization: " + selectedOrganizationName +
                    ", location: " + selectedLocation);

            // Create donation using repository with selected organization
            donationRepository.createVolunteerDonation(
                    selectedOrganizationId,
                    selectedOrganizationName,
                    typeEnum,
                    items,
                    new DonationRepository.OnDonationListener() {
                        @Override
                        public void onSuccess(String donationId) {
                            runOnUiThread(() -> {
                                Log.d(TAG, "Donation created successfully: " + donationId);
                                Toast.makeText(activity_volunteer_donation_confirm.this,
                                        "Quyên góp thành công tại " + selectedOrganizationName +
                                                "!\nBạn sẽ nhận " + points + " điểm sau khi được xác nhận",
                                        Toast.LENGTH_LONG).show();

                                // Về lại trang home của volunteer
                                Intent homeIntent = new Intent(activity_volunteer_donation_confirm.this, activity_volunteer_main.class);
                                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(homeIntent);
                                finish();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                Log.e(TAG, "Error creating donation: " + error);
                                Toast.makeText(activity_volunteer_donation_confirm.this,
                                        "Lỗi tạo quyên góp: " + error,
                                        Toast.LENGTH_LONG).show();

                                // Re-enable button
                                btnConfirm.setEnabled(true);
                                btnConfirm.setText("XÁC NHẬN");
                            });
                        }
                    }
            );

        } catch (Exception e) {
            Log.e(TAG, "Error processing donation: " + e.getMessage());
            Toast.makeText(this, "Lỗi xử lý quyên góp: " + e.getMessage(), Toast.LENGTH_LONG).show();

            // Re-enable button
            btnConfirm.setEnabled(true);
            btnConfirm.setText("XÁC NHẬN");
        }
    }
}
