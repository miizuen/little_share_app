package com.example.little_share.ui.volunteer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.CampaignRegistration;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.ui.volunteer.adapter.HistoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class activity_volunteer_campagin_history extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TextView tvTotalEvents, tvTotalPoints;
    private ImageView btnBack;
    private HistoryAdapter adapter;
    private CampaignRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_campagin_history);

        initViews();
        setupRecyclerView();
        loadData();
        setupClickListeners();
    }

    private void initViews() {
        rvHistory = findViewById(R.id.rv_history);
        tvTotalEvents = findViewById(R.id.tv_total_events);
        tvTotalPoints = findViewById(R.id.tv_total_points);
        btnBack = findViewById(R.id.btnBack);

        repository = new CampaignRepository();
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(this, new ArrayList<>());
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        // Setup click listeners
        adapter.setOnHistoryItemClickListener(new HistoryAdapter.OnHistoryItemClickListener() {
            @Override
            public void onItemClick(CampaignRegistration registration) {
                android.util.Log.d("HISTORY", "Item clicked: " + registration.getCampaignName());
                // TODO: Mở detail nếu cần
            }

            @Override
            public void onQRCodeClick(CampaignRegistration registration) {
                android.util.Log.d("HISTORY", "QR clicked: " + registration.getQrCode());
                // TODO: Hiển thị QR code
            }
        });
    }

    private void loadData() {
        android.util.Log.d("HISTORY", "=== LOADING HISTORY DATA ===");

        // Load history list
        repository.getUserRegistrationHistory().observe(this, registrations -> {
            android.util.Log.d("HISTORY", "Received " +
                    (registrations != null ? registrations.size() : 0) + " registrations");

            if (registrations != null && !registrations.isEmpty()) {
                adapter.updateData(registrations);

                // Debug log
                for (CampaignRegistration reg : registrations) {
                    android.util.Log.d("HISTORY", "Campaign: " + reg.getCampaignName() +
                            " | Role: " + reg.getRoleName() +
                            " | Status: " + reg.getStatus() +
                            " | Points: " + reg.getPointsEarned());
                }
            } else {
                android.util.Log.w("HISTORY", "No completed registrations found");
                Toast.makeText(this, "Chưa có lịch sử tham gia", Toast.LENGTH_SHORT).show();
            }
        });

        // Load stats
        repository.getRegistrationStats(new CampaignRepository.OnRegistrationStatsListener() {
            @Override
            public void onSuccess(int totalCampaigns, int totalPoints) {
                android.util.Log.d("HISTORY", "Stats - Campaigns: " + totalCampaigns + ", Points: " + totalPoints);

                tvTotalEvents.setText(String.valueOf(totalCampaigns));
                tvTotalPoints.setText(String.valueOf(totalPoints));
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}
