package com.example.little_share.ui.sponsor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.little_share.R;

public class dialog_donation_success_sponsor extends AppCompatActivity {

    private TextView tvAmount;
    private Button btnBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_donation_success_sponsor);
        initViews();
        getDataFromIntent();
        setupClickListeners();
    }

    private void initViews() {
        tvAmount = findViewById(R.id.tvAmount);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        String campaignName = intent.getStringExtra("campaign_name");
        String donationAmount = intent.getStringExtra("donation_amount");
        String message = intent.getStringExtra("message");
        String transactionId = intent.getStringExtra("transaction_id");

        // Hiển thị kết quả thanh toán
        if (result != null) {
            android.util.Log.d("DONATION_SUCCESS", "Payment result: " + result);
            android.util.Log.d("DONATION_SUCCESS", "Campaign: " + campaignName);
            android.util.Log.d("DONATION_SUCCESS", "Amount: " + donationAmount);
            android.util.Log.d("DONATION_SUCCESS", "Transaction ID: " + transactionId);

            // Cập nhật UI với thông tin chi tiết
            updateUIWithPaymentInfo(campaignName, donationAmount, transactionId);

            // Cập nhật dữ liệu vào Firebase (nếu thanh toán thành công)
            if ("Thanh toán thành công".equals(result)) {
                updateFirebaseWithDonation(campaignName, donationAmount, message, transactionId);
            }
        }
    }

    private void updateUIWithPaymentInfo(String campaignName, String donationAmount, String transactionId) {
        // Hiển thị số tiền đã tài trợ
        if (donationAmount != null && tvAmount != null) {
            try {
                double amount = Double.parseDouble(donationAmount);
                String formattedAmount = new java.text.DecimalFormat("#,###").format(amount) + " VNĐ";
                tvAmount.setText(formattedAmount);
            } catch (NumberFormatException e) {
                tvAmount.setText(donationAmount + " VNĐ");
            }
        }

        // Log thông tin để debug
        android.util.Log.d("DONATION_SUCCESS", "Updated UI with amount: " + donationAmount);
    }

    private void updateFirebaseWithDonation(String campaignName, String donationAmount, String message, String transactionId) {
        // TODO: Thêm code cập nhật Firebase ở đây
        // Ví dụ: thêm vào collection sponsoredCampaigns của user
        android.util.Log.d("FIREBASE_UPDATE", "Should update Firebase with:");
        android.util.Log.d("FIREBASE_UPDATE", "Campaign: " + campaignName);
        android.util.Log.d("FIREBASE_UPDATE", "Amount: " + donationAmount);
        android.util.Log.d("FIREBASE_UPDATE", "Transaction ID: " + transactionId);

        // Bạn có thể thêm code Firebase ở đây sau
    }

    private void setupClickListeners() {
        btnBackToHome.setOnClickListener(v -> {
            // Quay về MainActivity và chuyển đến home fragment với refresh
            Intent intent = new Intent(this, activity_sponsor_main.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("navigate_to", "home");
            intent.putExtra("refresh_sponsored", true);
            startActivity(intent);
            finish();
        });
    }
}