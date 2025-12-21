package com.example.little_share.ui.sponsor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.example.little_share.data.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class activity_sponsor_donation_form extends AppCompatActivity {
    private static final String TAG = "SponsorDonationForm";

    private ImageView btnBack;
    private TextView tvCampaignName, tvTargetBudget, tvCurrentBudget;
    private TextInputEditText etDonationAmount;
    private MaterialButton btnDonate, btn100k, btn500k, btn1m;

    private String campaignId;
    private String campaignName;
    private String organizationId;
    private String organizationName;
    private double targetBudget;
    private double currentBudget;

    private UserRepository userRepository;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sponsor_donation_form);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initRepositories();
        initViews();
        getIntentData();
        setupClickListeners();
        displayData();
    }

    private void initRepositories() {
        userRepository = new UserRepository();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvTargetBudget = findViewById(R.id.tvTargetBudget);
        tvCurrentBudget = findViewById(R.id.tvCurrentBudget);
        etDonationAmount = findViewById(R.id.etDonationAmount);
        btnDonate = findViewById(R.id.btnDonate);
        btn100k = findViewById(R.id.btn100k);
        btn500k = findViewById(R.id.btn500k);
        btn1m = findViewById(R.id.btn1m);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        campaignId = intent.getStringExtra("campaign_id");
        campaignName = intent.getStringExtra("campaign_name");
        organizationId = intent.getStringExtra("organization_id");
        organizationName = intent.getStringExtra("organization_name");
        targetBudget = intent.getDoubleExtra("campaign_target_budget", 0);
        currentBudget = intent.getDoubleExtra("campaign_current_budget", 0);

        Log.d(TAG, "Campaign ID: " + campaignId);
        Log.d(TAG, "Campaign Name: " + campaignName);
        Log.d(TAG, "Organization ID: " + organizationId);
        Log.d(TAG, "Organization Name: " + organizationName);
        Log.d(TAG, "Target Budget: " + targetBudget);
        Log.d(TAG, "Current Budget: " + currentBudget);

        // If organizationId is missing, try to get it from campaign
        if (organizationId == null || organizationId.isEmpty()) {
            getOrganizationIdFromCampaign();
        }
    }

    private void getOrganizationIdFromCampaign() {
        if (campaignId != null && !campaignId.isEmpty()) {
            db.collection("campaigns")
                    .document(campaignId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            organizationId = documentSnapshot.getString("organizationId");
                            organizationName = documentSnapshot.getString("organizationName");
                            Log.d(TAG, "Retrieved organizationId from campaign: " + organizationId);
                            Log.d(TAG, "Retrieved organizationName from campaign: " + organizationName);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get organization info from campaign: " + e.getMessage());
                    });
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        // Quick amount buttons
        btn100k.setOnClickListener(v -> {
            etDonationAmount.setText("100000");
            updateButtonSelection(btn100k);
        });

        btn500k.setOnClickListener(v -> {
            etDonationAmount.setText("500000");
            updateButtonSelection(btn500k);
        });

        btn1m.setOnClickListener(v -> {
            etDonationAmount.setText("1000000");
            updateButtonSelection(btn1m);
        });

        btnDonate.setOnClickListener(v -> processDonation());
    }

    private void updateButtonSelection(MaterialButton selectedButton) {
        // Reset all buttons
        resetButtonStyles();

        // Highlight selected button
        selectedButton.setBackgroundTintList(getColorStateList(R.color.primary_green));
        selectedButton.setTextColor(getColor(R.color.white));
    }

    private void resetButtonStyles() {
        int lightBlue = getColor(R.color.gray_lighter);
        int darkBlue = getColor(R.color.primary_green);

        btn100k.setBackgroundTintList(getColorStateList(R.color.gray_lighter));
        btn100k.setTextColor(darkBlue);

        btn500k.setBackgroundTintList(getColorStateList(R.color.gray_lighter));
        btn500k.setTextColor(darkBlue);

        btn1m.setBackgroundTintList(getColorStateList(R.color.gray_lighter));
        btn1m.setTextColor(darkBlue);
    }

    private void processDonation() {
        String amountStr = etDonationAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            etDonationAmount.setError("Vui lòng nhập số tiền tài trợ");
            etDonationAmount.requestFocus();
            return;
        }

        try {
            double donationAmount = Double.parseDouble(amountStr);

            if (donationAmount <= 0) {
                etDonationAmount.setError("Số tiền phải lớn hơn 0");
                etDonationAmount.requestFocus();
                return;
            }

            if (donationAmount < 10000) {
                etDonationAmount.setError("Số tiền tối thiểu là 10,000 VND");
                etDonationAmount.requestFocus();
                return;
            }

            // Show confirmation dialog
            showDonationConfirmation(donationAmount);

        } catch (NumberFormatException e) {
            etDonationAmount.setError("Số tiền không hợp lệ");
            etDonationAmount.requestFocus();
        }
    }

    private void showDonationConfirmation(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = formatter.format(amount);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận tài trợ")
                .setMessage("Bạn có chắc chắn muốn tài trợ " + formattedAmount + " cho chiến dịch \"" + campaignName + "\"?\n\nSau khi xác nhận, bạn sẽ được chuyển đến trang thanh toán.")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    executeDonation(amount);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void executeDonation(double amount) {
        // Disable button to prevent double submission
        btnDonate.setEnabled(false);
        btnDonate.setText("Đang xử lý...");

        // Get current user info first
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // ✅ FIX: Sử dụng getCurrentUserData thay vì getUserById
        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(com.example.little_share.data.models.User user) {
                if (user == null) {
                    Toast.makeText(activity_sponsor_donation_form.this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    resetDonateButton();
                    return;
                }

                // Create sponsor donation data
                String transactionId = "TXN_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

                Map<String, Object> donationData = new HashMap<>();
                donationData.put("sponsorId", currentUserId);
                donationData.put("sponsorName", user.getFullName());
                donationData.put("campaignId", campaignId);
                donationData.put("campaignName", campaignName);
                donationData.put("organizationName", organizationName != null ? organizationName : "Light of Heart");
                donationData.put("organizationId", organizationId != null ? organizationId : ""); // ✅ Add organizationId
                donationData.put("amount", amount);
                donationData.put("message", "Tài trợ từ ứng dụng Little Share");
                donationData.put("paymentMethod", "ZaloPay");
                donationData.put("transactionId", transactionId);
                donationData.put("status", "COMPLETED"); // ✅ Set as COMPLETED for immediate counting
                donationData.put("donationDate", FieldValue.serverTimestamp());

                // Save sponsor donation to Firebase
                db.collection("sponsorDonations")
                        .add(donationData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "Sponsor donation saved successfully with ID: " + documentReference.getId());

                            // Update campaign budget
                            updateCampaignBudget(campaignId, amount, transactionId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to save sponsor donation: " + e.getMessage());

                            runOnUiThread(() -> {
                                Toast.makeText(activity_sponsor_donation_form.this,
                                        "Lỗi khi lưu thông tin tài trợ: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                resetDonateButton();
                            });
                        });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to get user info: " + error);
                Toast.makeText(activity_sponsor_donation_form.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                resetDonateButton();
            }
        });
    }

    private void updateCampaignBudget(String campaignId, double amount, String transactionId) {
        db.collection("campaigns")
                .document(campaignId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double currentBudgetObj = documentSnapshot.getDouble("currentBudget");
                        double currentBudget = currentBudgetObj != null ? currentBudgetObj : 0.0;
                        double newBudget = currentBudget + amount;

                        db.collection("campaigns")
                                .document(campaignId)
                                .update("currentBudget", newBudget)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Campaign budget updated successfully");
                                    showSuccessMessage(amount, transactionId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update campaign budget: " + e.getMessage());
                                    // Still show success message even if budget update fails
                                    showSuccessMessage(amount, transactionId);
                                });
                    } else {
                        Log.w(TAG, "Campaign document not found, but donation was saved");
                        showSuccessMessage(amount, transactionId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get campaign document: " + e.getMessage());
                    // Still show success message since donation was saved
                    showSuccessMessage(amount, transactionId);
                });
    }

    private void showSuccessMessage(double amount, String transactionId) {
        runOnUiThread(() -> {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedAmount = formatter.format(amount);

            Toast.makeText(activity_sponsor_donation_form.this,
                    "Tài trợ thành công!\nSố tiền: " + formattedAmount +
                            "\nMã giao dịch: " + transactionId,
                    Toast.LENGTH_LONG).show();

            // Navigate back to sponsor home or previous screen
            finish();
        });
    }

    private void resetDonateButton() {
        btnDonate.setEnabled(true);
        btnDonate.setText("TÀI TRỢ NGAY");
    }

    private void displayData() {
        tvCampaignName.setText(campaignName != null ? campaignName : "Chiến dịch");

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTargetBudget.setText(formatter.format(targetBudget));
        tvCurrentBudget.setText(formatter.format(currentBudget));

        // Calculate remaining amount
        double remaining = targetBudget - currentBudget;
        if (remaining > 0) {
            TextView tvRemaining = findViewById(R.id.tvRemaining);
            if (tvRemaining != null) {
                tvRemaining.setText("Còn thiếu: " + formatter.format(remaining));
            }
        }
    }
}
