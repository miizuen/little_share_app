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

        // Hiển thị kết quả thanh toán (có thể log hoặc toast)
        if (result != null) {
            android.util.Log.d("DONATION_SUCCESS", "Payment result: " + result);

            // Có thể cập nhật UI dựa vào result
            updateUIBasedOnResult(result);
        }
    }

    private void updateUIBasedOnResult(String result) {
        TextView tvTitle = findViewById(R.id.tvAmount).getRootView()
                .findViewById(R.id.tvAmount); // Tìm TextView tiêu đề nếu cần

        // Tùy chỉnh hiển thị dựa vào kết quả
        switch (result) {
            case "Thanh toán thành công":
                // UI đã đúng với success
                break;
            case "Hủy thanh toán":
                // Có thể thay đổi icon, text nếu cần
                break;
            case "Lỗi thanh toán":
                // Có thể thay đổi màu sắc, thông báo
                break;
        }
    }

    private void setupClickListeners() {
        btnBackToHome.setOnClickListener(v -> {
            // Quay về trang chủ và xóa toàn bộ stack
            Intent intent = new Intent(this, activity_sponsor_main.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}