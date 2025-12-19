package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.example.little_share.data.models.Donation;
import com.example.little_share.data.models.DonationItem;
import com.example.little_share.data.repositories.DonationRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class activity_volunteer_donation_confirm extends AppCompatActivity {

    private static final String TAG = "DonationConfirm";

    private DonationRepository donationRepository;
    private MaterialButton btnConfirm;

    // Data from previous activity
    private String donationType;
    private String category;
    private int quantity;
    private String condition;
    private int points;
    private String note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_donation_confirm);

        // Initialize repository
        donationRepository = new DonationRepository();

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private String convertDonationTypeToText(String type) {
        if (type == null) return "SÁCH VỞ";

        switch (type) {
            case "BOOKS": return "SÁCH VỞ";
            case "CLOTHES": return "QUẦN ÁO";
            case "TOYS": return "ĐỒ CHƠI";
            case "MONEY": return "TIỀN MẶT";
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
            case "MONEY":
                return Donation.DonationType.MONEY;
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

            // For now, use a default organization (you can modify this later)
            String organizationId = "default_org_id";
            String organizationName = "Tổ chức từ thiện mặc định";

            Log.d(TAG, "Creating donation with type: " + typeEnum + ", condition: " + item.getCondition());

            // Create donation using repository
            donationRepository.createVolunteerDonation(
                    organizationId,
                    organizationName,
                    typeEnum,
                    items,
                    new DonationRepository.OnDonationListener() {
                        @Override
                        public void onSuccess(String donationId) {
                            runOnUiThread(() -> {
                                Log.d(TAG, "Donation created successfully: " + donationId);
                                Toast.makeText(activity_volunteer_donation_confirm.this,
                                        "Quyên góp thành công! Bạn sẽ nhận " + points + " điểm sau khi được xác nhận",
                                        Toast.LENGTH_LONG).show();

                                // Close this activity and return to donation list
                                setResult(RESULT_OK);
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
