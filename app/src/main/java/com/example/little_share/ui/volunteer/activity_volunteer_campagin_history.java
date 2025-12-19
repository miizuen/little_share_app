package com.example.little_share.ui.volunteer;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.CampaignRegistration;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.ui.volunteer.adapter.HistoryAdapter;
import java.util.ArrayList;

public class activity_volunteer_campagin_history extends AppCompatActivity
        implements HistoryAdapter.OnHistoryItemClickListener {

    private ImageView btnBack;
    private TextView tvTotalEvents, tvTotalPoints;
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private CampaignRepository campaignRepository; // Sử dụng CampaignRepository hiện có

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_campagin_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initRepository();
        setupRecyclerView();
        setupClickListeners();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalEvents = findViewById(R.id.tv_total_events);
        tvTotalPoints = findViewById(R.id.tv_total_points);
        rvHistory = findViewById(R.id.rv_history);
    }

    private void initRepository() {
        campaignRepository = new CampaignRepository(); // Sử dụng repository hiện có
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter(this, new ArrayList<>());
        historyAdapter.setOnHistoryItemClickListener(this);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadData() {
        // Load statistics sử dụng phương thức mới
        campaignRepository.getRegistrationStats(new CampaignRepository.OnRegistrationStatsListener() {
            @Override
            public void onSuccess(int totalCampaigns, int totalPoints) {
                tvTotalEvents.setText(String.valueOf(totalCampaigns));
                tvTotalPoints.setText(String.valueOf(totalPoints));
            }
        });

        // Load history list sử dụng phương thức mới
        campaignRepository.getUserRegistrationHistory().observe(this, registrations -> {
            if (registrations != null) {
                historyAdapter.updateData(registrations);

                if (registrations.isEmpty()) {
                    Toast.makeText(this, "Chưa có lịch sử tham gia", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onItemClick(CampaignRegistration registration) {
        // Xử lý click vào item
        Toast.makeText(this, "Chi tiết: " + registration.getCampaignName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onQRCodeClick(CampaignRegistration registration) {
        // Hiển thị QR code
        if (registration.getQrCode() != null) {
            showQRCodeDialog(registration);
        } else {
            Toast.makeText(this, "Chưa có mã QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void showQRCodeDialog(CampaignRegistration registration) {
        // Hiển thị QR code - có thể tạo dialog tương tự GiftRedemptionSuccessDialog
        Toast.makeText(this, "Hiển thị QR: " + registration.getQrCode(), Toast.LENGTH_SHORT).show();
    }
}
