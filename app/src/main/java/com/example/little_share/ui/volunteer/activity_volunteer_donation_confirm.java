package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;

public class activity_volunteer_donation_confirm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_donation_confirm);

        // Ánh xạ
        TextView tvCampaignName = findViewById(R.id.tvCampaignName);
        TextView tvType = findViewById(R.id.tvType);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvQuantity = findViewById(R.id.tvQuantity);
        TextView tvCondition = findViewById(R.id.tvCondition);
        TextView tvTotalPoints = findViewById(R.id.tvTotalPoints);

        // Nhận dữ liệu
        Intent intent = getIntent();
        String donationType = intent.getStringExtra("DONATION_TYPE");
        String category = intent.getStringExtra("CATEGORY");
        int quantity = intent.getIntExtra("QUANTITY", 1);
        String condition = intent.getStringExtra("CONDITION");
        int points = intent.getIntExtra("POINTS", 0);

        String typeText;
        if ("BOOK".equals(donationType)) {
            typeText = "SÁCH VỞ";
        } else if ("SHIRT".equals(donationType)) {
            typeText = "QUẦN ÁO";
        } else if ("TOY".equals(donationType)) {
            typeText = "ĐỒ CHƠI";
        } else if ("MONEY".equals(donationType)) {
            typeText = "TIỀN MẶT";
        } else {
            typeText = "SÁCH VỞ";
        }

        tvCampaignName.setText(typeText);
        tvType.setText(typeText);
        tvCategory.setText(category);
        tvQuantity.setText(String.valueOf(quantity));
        tvCondition.setText(condition.replace("(100%)", "100%").replace("80%-90%", "80-90%").replace("60%-70%", "60-70%"));
        tvTotalPoints.setText("+" + points);

        // Xử lý nút quay lại, xác nhận...
        findViewById(R.id.btnBack2).setOnClickListener(v -> finish());
        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            Toast.makeText(this, "Quyên góp thành công! + " + points + " điểm", Toast.LENGTH_LONG).show();
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}