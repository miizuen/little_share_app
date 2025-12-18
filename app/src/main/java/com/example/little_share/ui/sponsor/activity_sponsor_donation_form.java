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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

public class activity_sponsor_donation_form extends AppCompatActivity {
    private static final String TAG = "SponsorDonationForm";

    private ImageView btnBack;
    private TextView tvCampaignName, tvTargetBudget, tvCurrentBudget;
    private TextInputEditText etDonationAmount;
    private MaterialButton btnDonate, btn100k, btn500k, btn1m;

    private String campaignId;
    private String campaignName;
    private double targetBudget;
    private double currentBudget;

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

        initViews();
        getIntentData();
        setupClickListeners();
        displayData();
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
        targetBudget = intent.getDoubleExtra("campaign_target_budget", 0);
        currentBudget = intent.getDoubleExtra("campaign_current_budget", 0);

        Log.d(TAG, "Campaign ID: " + campaignId);
        Log.d(TAG, "Campaign Name: " + campaignName);
        Log.d(TAG, "Target Budget: " + targetBudget);
        Log.d(TAG, "Current Budget: " + currentBudget);
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
                .setMessage("Bạn có chắc chắn muốn tài trợ " + formattedAmount + " cho chiến dịch \"" + campaignName + "\"?")
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

        // TODO: Implement actual donation processing
        // For now, just show a message that the feature will be implemented
        new android.os.Handler().postDelayed(() -> {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedAmount = formatter.format(amount);
            
            Toast.makeText(this, "Tính năng thanh toán sẽ được tích hợp sớm.\nSố tiền: " + formattedAmount, Toast.LENGTH_LONG).show();
            
            // Re-enable button
            btnDonate.setEnabled(true);
            btnDonate.setText("TÀI TRỢ NGAY");
            
        }, 1000);
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