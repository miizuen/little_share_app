package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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
    private LinearLayout locationContainer;

    // Data from previous activity
    private String donationType;
    private String category;
    private int quantity;
    private String condition;
    private int points;
    private String note;
    private String campaignId;

    // Selected location data
    private String selectedOrganizationId;
    private String selectedOrganizationName;
    private String selectedLocation;

    // Campaign data
    private Campaign selectedCampaign;

    // Organization list
    private List<Organization> organizationList = new ArrayList<>();
    private MaterialCardView selectedCardView = null;

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

        // Initialize views
        locationContainer = findViewById(R.id.locationContainer);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Get data from intent
        Intent intent = getIntent();
        donationType = intent.getStringExtra("DONATION_TYPE");
        category = intent.getStringExtra("CATEGORY");
        quantity = intent.getIntExtra("QUANTITY", 1);
        condition = intent.getStringExtra("CONDITION");
        points = intent.getIntExtra("POINTS", 0);
        note = intent.getStringExtra("NOTE");
        campaignId = intent.getStringExtra("CAMPAIGN_ID");

        // Setup UI with donation info
        setupDonationInfo();

        // Setup buttons
        findViewById(R.id.btnBack2).setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> confirmDonation());

        // Load appropriate data based on campaign ID
        if (campaignId != null && !campaignId.isEmpty()) {
            Log.d(TAG, "Campaign ID found: " + campaignId + " - Loading campaign data");
            loadCampaignAndOrganization();
        } else {
            Log.d(TAG, "No campaign ID - Loading all organizations");
            loadAllOrganizations();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupDonationInfo() {
        TextView tvCampaignName = findViewById(R.id.tvCampaignName);
        TextView tvType = findViewById(R.id.tvType);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvQuantity = findViewById(R.id.tvQuantity);
        TextView tvCondition = findViewById(R.id.tvCondition);
        TextView tvTotalPoints = findViewById(R.id.tvTotalPoints);

        String typeText = convertDonationTypeToText(donationType);

        tvCampaignName.setText(typeText);
        tvType.setText(typeText);
        tvCategory.setText(category);
        tvQuantity.setText(String.valueOf(quantity));
        tvCondition.setText(condition != null ?
                condition.replace("(100%)", "100%")
                        .replace("80%-90%", "80-90%")
                        .replace("60%-70%", "60-70%")
                : "Không xác định");
        tvTotalPoints.setText("+" + points);
    }

    // ========== LOAD CAMPAIGN & ORGANIZATION ==========
    private void loadCampaignAndOrganization() {
        Log.d(TAG, "Loading campaign: " + campaignId);

        db.collection("campaigns").document(campaignId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        selectedCampaign = documentSnapshot.toObject(Campaign.class);
                        if (selectedCampaign != null) {
                            selectedCampaign.setId(documentSnapshot.getId());
                            loadOrganizationDetails(selectedCampaign.getOrganizationId());
                        } else {
                            Log.e(TAG, "Failed to parse campaign data");
                            showError("Lỗi đọc dữ liệu chiến dịch");
                        }
                    } else {
                        Log.e(TAG, "Campaign not found: " + campaignId);
                        showError("Không tìm thấy chiến dịch");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading campaign: " + e.getMessage());
                    showError("Lỗi tải thông tin chiến dịch: " + e.getMessage());
                });
    }

    private void loadOrganizationDetails(String organizationId) {
        Log.d(TAG, "Loading organization: " + organizationId);

        db.collection("organization").document(organizationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Organization organization = documentSnapshot.toObject(Organization.class);
                        if (organization != null) {
                            organization.setId(documentSnapshot.getId());
                            displaySingleOrganization(organization);
                        } else {
                            Log.e(TAG, "Failed to parse organization data");
                            showError("Lỗi đọc dữ liệu tổ chức");
                        }
                    } else {
                        Log.e(TAG, "Organization not found: " + organizationId);
                        showError("Không tìm thấy tổ chức");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading organization: " + e.getMessage());
                    showError("Lỗi tải thông tin tổ chức: " + e.getMessage());
                });
    }

    private void displaySingleOrganization(Organization organization) {
        Log.d(TAG, "Displaying single organization: " + organization.getName());

        // Clear container
        locationContainer.removeAllViews();

        // Inflate location card
        View locationCard = LayoutInflater.from(this)
                .inflate(R.layout.item_donation_location, locationContainer, false);

        TextView tvCampaignTitle = locationCard.findViewById(R.id.tv_campaign_title);
        TextView tvCampaignAddress = locationCard.findViewById(R.id.tv_campaign_address);
        TextView tvCampaignTime = locationCard.findViewById(R.id.tv_campaign_time);

        // Set data
        String title = selectedCampaign != null ?
                selectedCampaign.getName() + " - " + organization.getName() :
                organization.getName();
        String address = organization.getAddress() != null ?
                organization.getAddress() : "Địa chỉ chưa cập nhật";
        String workingHours = selectedCampaign != null && selectedCampaign.getSpecificLocation() != null ?
                selectedCampaign.getSpecificLocation() : "8:00 - 17:00 (T2-T7)";

        tvCampaignTitle.setText(title);
        tvCampaignAddress.setText(address);
        tvCampaignTime.setText(workingHours);

        // Auto-select this organization
        if (locationCard instanceof MaterialCardView) {
            MaterialCardView cardView = (MaterialCardView) locationCard;
            cardView.setCardBackgroundColor(Color.parseColor("#E8F5E8"));
            cardView.setStrokeColor(Color.parseColor("#4CAF50"));
            cardView.setStrokeWidth(6);
            selectedCardView = cardView;
        }

        // Set selected data
        selectedOrganizationId = organization.getId();
        selectedOrganizationName = organization.getName();
        selectedLocation = address;

        locationContainer.addView(locationCard);

        Log.d(TAG, "Organization displayed successfully");
        Log.d(TAG, "ID: " + selectedOrganizationId);
        Log.d(TAG, "Name: " + selectedOrganizationName);
    }

    // ========== LOAD ALL ORGANIZATIONS ==========
    private void loadAllOrganizations() {
        Log.d(TAG, "Loading all organizations from Firestore");

        db.collection("organization")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    organizationList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Organization org = doc.toObject(Organization.class);
                        org.setId(doc.getId());
                        organizationList.add(org);

                        Log.d(TAG, "Organization loaded: " + org.getName() + " | " + org.getAddress());
                    }

                    Log.d(TAG, "Total organizations loaded: " + organizationList.size());

                    if (organizationList.isEmpty()) {
                        showError("Không tìm thấy tổ chức nào");
                    } else {
                        displayOrganizationList();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading organizations: " + e.getMessage());
                    showError("Lỗi tải danh sách tổ chức: " + e.getMessage());
                });
    }

    private void displayOrganizationList() {
        Log.d(TAG, "Displaying " + organizationList.size() + " organizations");

        // Clear container
        locationContainer.removeAllViews();

        for (Organization org : organizationList) {
            View locationCard = LayoutInflater.from(this)
                    .inflate(R.layout.item_donation_location, locationContainer, false);

            TextView tvCampaignTitle = locationCard.findViewById(R.id.tv_campaign_title);
            TextView tvCampaignAddress = locationCard.findViewById(R.id.tv_campaign_address);
            TextView tvCampaignTime = locationCard.findViewById(R.id.tv_campaign_time);

            // Set organization data
            tvCampaignTitle.setText(org.getName());
            tvCampaignAddress.setText(org.getAddress() != null ? org.getAddress() : "Địa chỉ chưa cập nhật");
            tvCampaignTime.setText("8:00 - 17:00 (T2-T7)");

            // Setup click listener for selection
            MaterialCardView cardView = (MaterialCardView) locationCard;
            cardView.setOnClickListener(v -> selectOrganization(cardView, org));

            locationContainer.addView(locationCard);
        }

        Log.d(TAG, "Organization list displayed successfully");
    }

    private void selectOrganization(MaterialCardView clickedCard, Organization organization) {
        // Deselect previous card
        if (selectedCardView != null) {
            selectedCardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            selectedCardView.setStrokeColor(Color.parseColor("#E0E0E0"));
            selectedCardView.setStrokeWidth(2);
        }

        // Select new card
        clickedCard.setCardBackgroundColor(Color.parseColor("#E8F5E8"));
        clickedCard.setStrokeColor(Color.parseColor("#4CAF50"));
        clickedCard.setStrokeWidth(6);
        selectedCardView = clickedCard;

        // Update selected data
        selectedOrganizationId = organization.getId();
        selectedOrganizationName = organization.getName();
        selectedLocation = organization.getAddress();

        Log.d(TAG, "Organization selected: " + selectedOrganizationName);
        Toast.makeText(this, "Đã chọn: " + selectedOrganizationName, Toast.LENGTH_SHORT).show();
    }

    // ========== CONFIRM DONATION ==========
    private void confirmDonation() {
        Log.d(TAG, "========================================");
        Log.d(TAG, "CONFIRM DONATION CLICKED");
        Log.d(TAG, "========================================");
        Log.d(TAG, "selectedOrganizationId: [" + selectedOrganizationId + "]");
        Log.d(TAG, "selectedOrganizationName: [" + selectedOrganizationName + "]");
        Log.d(TAG, "campaignId: [" + campaignId + "]");
        Log.d(TAG, "========================================");

        // Validate organization selection
        if (selectedOrganizationId == null || selectedOrganizationName == null) {
            Toast.makeText(this, "Vui lòng chọn tổ chức để quyên góp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedOrganizationId.trim().isEmpty() || selectedOrganizationName.trim().isEmpty()) {
            Toast.makeText(this, "Thông tin tổ chức không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "VALIDATION PASSED - Proceeding with donation");

        // Disable button
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

            // Convert donation type
            Donation.DonationType typeEnum = convertStringToEnum(donationType);

            // Create donation item
            DonationItem item = new DonationItem();
            item.setCategory(category);
            item.setQuantity(quantity);
            item.setCondition(convertConditionToEnum(condition));
            item.setNotes(note != null ? note : "");

            List<DonationItem> items = new ArrayList<>();
            items.add(item);

            Log.d(TAG, "Creating donation:");
            Log.d(TAG, "  Type: " + typeEnum);
            Log.d(TAG, "  Organization: " + selectedOrganizationName);
            Log.d(TAG, "  Category: " + category);
            Log.d(TAG, "  Quantity: " + quantity);
            Log.d(TAG, "  Condition: " + item.getCondition());

            // Create donation
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

                                String successMessage = campaignId != null && !campaignId.isEmpty() ?
                                        "Quyên góp thành công cho chiến dịch: " +
                                                (selectedCampaign != null ? selectedCampaign.getName() : selectedOrganizationName) +
                                                "!\nBạn sẽ nhận " + points + " điểm sau khi được xác nhận" :
                                        "Quyên góp thành công cho " + selectedOrganizationName +
                                                "!\nBạn sẽ nhận " + points + " điểm sau khi được xác nhận";

                                Toast.makeText(activity_volunteer_donation_confirm.this,
                                        successMessage,
                                        Toast.LENGTH_LONG).show();

                                // Return to home
                                Intent homeIntent = new Intent(activity_volunteer_donation_confirm.this,
                                        activity_volunteer_main.class);
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

    // ========== HELPER METHODS ==========
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
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
            case "BOOKS": return Donation.DonationType.BOOKS;
            case "CLOTHES": return Donation.DonationType.CLOTHES;
            case "TOYS": return Donation.DonationType.TOYS;
            case "ESSENTIALS": return Donation.DonationType.ESSENTIALS;
            default:
                Log.w(TAG, "Unknown donation type: " + type + ", using BOOKS as default");
                return Donation.DonationType.BOOKS;
        }
    }

    private DonationItem.ItemCondition convertConditionToEnum(String conditionText) {
        if (conditionText == null || conditionText.isEmpty()) {
            return DonationItem.ItemCondition.ACCEPTABLE;
        }

        String lowerCondition = conditionText.toLowerCase().trim();

        if (lowerCondition.contains("mới") || lowerCondition.contains("100%")) {
            return DonationItem.ItemCondition.NEW;
        } else if (lowerCondition.contains("tốt") || lowerCondition.contains("80") || lowerCondition.contains("90")) {
            return DonationItem.ItemCondition.GOOD;
        } else if (lowerCondition.contains("khá") || lowerCondition.contains("60") || lowerCondition.contains("70")) {
            return DonationItem.ItemCondition.FAIR;
        } else {
            return DonationItem.ItemCondition.ACCEPTABLE;
        }
    }
}