package com.example.little_share.ui.volunteer;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.CampaignRegistration;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.ui.volunteer.adapter.HistoryAdapter;
import java.util.ArrayList;
import java.util.List;

public class activity_volunteer_campagin_history extends AppCompatActivity
        implements HistoryAdapter.OnHistoryItemClickListener {

    private static final String TAG = "CampaignHistory";
    private ImageView btnBack;
    private TextView tvTotalEvents, tvTotalPoints;
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private CampaignRepository campaignRepository;
    private Observer<List<CampaignRegistration>> historyObserver;
    private boolean isDataLoaded = false;

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

        Log.d(TAG, "Views initialized");
    }

    private void initRepository() {
        campaignRepository = new CampaignRepository();
        Log.d(TAG, "Repository initialized");
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter(this, new ArrayList<>());
        historyAdapter.setOnHistoryItemClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvHistory.setLayoutManager(layoutManager);
        rvHistory.setAdapter(historyAdapter);

        // Tắt nested scrolling để tránh conflict
        rvHistory.setNestedScrollingEnabled(false);

        Log.d(TAG, "RecyclerView setup completed");
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadData() {
        Log.d(TAG, "Loading data...");

        // Load statistics một lần
        loadStats();

        // Setup observer cho history list
        setupHistoryObserver();
    }

    private void loadStats() {
        campaignRepository.getRegistrationStats(new CampaignRepository.OnRegistrationStatsListener() {
            @Override
            public void onSuccess(int totalCampaigns, int totalPoints) {
                Log.d(TAG, "Stats loaded: " + totalCampaigns + " campaigns, " + totalPoints + " points");
                tvTotalEvents.setText(String.valueOf(totalCampaigns));
                tvTotalPoints.setText(String.valueOf(totalPoints));
            }
        });
    }

    private void setupHistoryObserver() {
        // Tạo observer chỉ một lần
        historyObserver = registrations -> {
            Log.d(TAG, "Observer triggered - Registration history received: " +
                    (registrations != null ? registrations.size() : 0) + " items");

            if (registrations != null) {
                // Chỉ update nếu có dữ liệu thực sự
                if (!registrations.isEmpty()) {
                    historyAdapter.updateData(registrations);
                    isDataLoaded = true;

                    Log.d(TAG, "Updated adapter with " + registrations.size() + " registrations");

                    // Log first item for debugging
                    CampaignRegistration first = registrations.get(0);
                    Log.d(TAG, "First item: " + first.getCampaignName() + " - " + first.getRoleName());

                } else if (!isDataLoaded) {
                    // Chỉ hiển thị message nếu chưa load data lần nào
                    Toast.makeText(this, "Chưa có lịch sử tham gia", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "Registrations list is null");
            }
        };

        // Observe data
        campaignRepository.getUserRegistrationHistory().observe(this, historyObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity resumed - Data loaded: " + isDataLoaded);

        // Không reload data khi resume để tránh clear list
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove observer để tránh memory leak
        if (historyObserver != null) {
            campaignRepository.getUserRegistrationHistory().removeObserver(historyObserver);
        }
    }

    @Override
    public void onItemClick(CampaignRegistration registration) {
        Log.d(TAG, "Item clicked: " + registration.getCampaignName());
        Toast.makeText(this, "Chi tiết: " + registration.getCampaignName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onQRCodeClick(CampaignRegistration registration) {
        Log.d(TAG, "QR clicked: " + registration.getCampaignName());
        if (registration.getQrCode() != null) {
            Toast.makeText(this, "QR: " + registration.getQrCode(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Chưa có mã QR", Toast.LENGTH_SHORT).show();
        }
    }
}
