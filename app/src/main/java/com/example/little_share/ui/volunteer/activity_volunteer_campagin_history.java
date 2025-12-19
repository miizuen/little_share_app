package com.example.little_share.ui.volunteer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.little_share.adapter.volunteer.VolunteerHistoryAdapter;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.volunteer.VolunteerHistoryModel;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.data.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class activity_volunteer_campagin_history extends AppCompatActivity {

    private static final String TAG = "VolunteerHistory";

    private ImageView btnBack;
    private TextView tvTotalEvents;
    private TextView tvTotalPoints;
    private RecyclerView rvHistory;
    private VolunteerHistoryAdapter historyAdapter;
    private UserRepository userRepository;
    private CampaignRepository campaignRepository;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_campagin_history);

        // Initialize views
        initViews();

        // Initialize repositories
        userRepository = new UserRepository();
        campaignRepository = new CampaignRepository();
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        setupRecyclerView();

        // Load data
        loadUserStats();
        loadCampaignHistoryFromFirebase();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalEvents = findViewById(R.id.tv_total_events);
        tvTotalPoints = findViewById(R.id.tv_total_points);
        rvHistory = findViewById(R.id.rv_history);

        // Back button click listener
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        historyAdapter = new VolunteerHistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);

        // Set click listener for history items
        historyAdapter.setClickListener((history, position) -> {
            Toast.makeText(this, "Clicked: " + history.getCampaignTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to campaign detail
        });
    }

    private void loadUserStats() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(com.example.little_share.data.models.User user) {
                if (user != null) {
                    // Update total points
                    int totalPoints = user.getTotalPoints();
                    tvTotalPoints.setText(String.valueOf(totalPoints));

                    // Update total donations (as events for now)
                    int totalDonations = user.getTotalDonations();
                    tvTotalEvents.setText(String.valueOf(totalDonations));

                    Log.d(TAG, "User stats loaded - Points: " + totalPoints + ", Events: " + totalDonations);
                } else {
                    Log.e(TAG, "User data is null");
                    // Set default values
                    tvTotalEvents.setText("0");
                    tvTotalPoints.setText("0");
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading user stats: " + error);
                Toast.makeText(activity_volunteer_campagin_history.this,
                        "Lỗi tải thống kê: " + error, Toast.LENGTH_SHORT).show();

                // Set default values
                tvTotalEvents.setText("0");
                tvTotalPoints.setText("0");
            }
        });
    }

    private void loadCampaignHistoryFromFirebase() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Try to load from campaign_volunteers collection
        db.collection("campaign_volunteers")
                .whereEqualTo("volunteerId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No campaign history found in Firebase, using mock data");
                        // Use mock data if no real data
                        loadMockCampaignHistory();
                    } else {
                        Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " campaign registrations");
                        List<String> campaignIds = new ArrayList<>();

                        // Get all campaign IDs
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String campaignId = doc.getString("campaignId");
                            if (campaignId != null) {
                                campaignIds.add(campaignId);
                            }
                        }

                        // Load campaign details
                        loadCampaignDetails(campaignIds, queryDocumentSnapshots);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading campaign history: " + e.getMessage());
                    // Fallback to mock data
                    loadMockCampaignHistory();
                });
    }

    private void loadCampaignDetails(List<String> campaignIds,
                                     com.google.firebase.firestore.QuerySnapshot registrations) {
        if (campaignIds.isEmpty()) {
            loadMockCampaignHistory();
            return;
        }

        List<VolunteerHistoryModel> historyList = new ArrayList<>();

        // Load each campaign
        for (String campaignId : campaignIds) {
            db.collection("campaigns")
                    .document(campaignId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Campaign campaign = documentSnapshot.toObject(Campaign.class);
                            if (campaign != null) {
                                // Find registration info
                                String role = "Tình nguyện viên";
                                String status = "Hoàn thành";
                                String statusColor = "#22C55E";
                                boolean isCompleted = true;

                                for (QueryDocumentSnapshot regDoc : registrations) {
                                    if (campaignId.equals(regDoc.getString("campaignId"))) {
                                        role = regDoc.getString("role");
                                        if (role == null) role = "Tình nguyện viên";
                                        break;
                                    }
                                }

                                // Determine status based on campaign status
                                if ("ONGOING".equals(campaign.getStatus())) {
                                    status = "Đang diễn ra";
                                    statusColor = "#3B82F6";
                                    isCompleted = false;
                                } else if ("UPCOMING".equals(campaign.getStatus())) {
                                    status = "Sắp diễn ra";
                                    statusColor = "#F59E0B";
                                    isCompleted = false;
                                } else if ("COMPLETED".equals(campaign.getStatus())) {
                                    status = "Hoàn thành";
                                    statusColor = "#22C55E";
                                    isCompleted = true;
                                }

                                // Format date and time
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                                String date = campaign.getStartDate() != null ?
                                        dateFormat.format(campaign.getStartDate()) : "N/A";

                                String time = "8:00 - 17:00"; // Default time
                                if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
                                    time = timeFormat.format(campaign.getStartDate()) + " - " +
                                            timeFormat.format(campaign.getEndDate());
                                }

                                VolunteerHistoryModel history = new VolunteerHistoryModel(
                                        status,
                                        campaign.getPointsReward(),
                                        campaign.getName(),
                                        role,
                                        date,
                                        time,
                                        statusColor,
                                        isCompleted
                                );

                                historyList.add(history);

                                // Update adapter when all loaded
                                if (historyList.size() == campaignIds.size()) {
                                    historyAdapter.setHistoryList(historyList);
                                    Log.d(TAG, "Loaded " + historyList.size() + " campaigns from Firebase");
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading campaign details: " + e.getMessage());
                    });
        }
    }

    private void loadMockCampaignHistory() {
        List<VolunteerHistoryModel> historyList = createMockHistory();
        historyAdapter.setHistoryList(historyList);
        Log.d(TAG, "Loaded " + historyList.size() + " mock history items");
    }

    private List<VolunteerHistoryModel> createMockHistory() {
        List<VolunteerHistoryModel> list = new ArrayList<>();

        // Completed campaigns
        list.add(new VolunteerHistoryModel(
                "Hoàn thành",
                50,
                "Nấu ăn cho em",
                "Đầu bếp",
                "28/10/2024",
                "8:00 - 12:00",
                "#22C55E", // Green
                true
        ));

        list.add(new VolunteerHistoryModel(
                "Hoàn thành",
                80,
                "Mùa đông ấm áp",
                "Hỗ trợ",
                "15/11/2024",
                "9:00 - 16:00",
                "#22C55E", // Green
                true
        ));

        list.add(new VolunteerHistoryModel(
                "Hoàn thành",
                60,
                "Trồng cây xanh",
                "Tình nguyện viên",
                "05/12/2024",
                "7:00 - 11:00",
                "#22C55E", // Green
                true
        ));

        // Ongoing campaign
        list.add(new VolunteerHistoryModel(
                "Đang diễn ra",
                100,
                "Khám bệnh miễn phí",
                "Y tá",
                "20/12/2024",
                "8:00 - 17:00",
                "#3B82F6", // Blue
                false
        ));

        // Upcoming campaign
        list.add(new VolunteerHistoryModel(
                "Sắp diễn ra",
                120,
                "Cứu trợ lũ lụt",
                "Điều phối",
                "25/12/2024",
                "6:00 - 18:00",
                "#F59E0B", // Orange
                false
        ));

        list.add(new VolunteerHistoryModel(
                "Hoàn thành",
                70,
                "Dạy học miễn phí",
                "Giáo viên",
                "10/11/2024",
                "14:00 - 17:00",
                "#22C55E", // Green
                true
        ));

        list.add(new VolunteerHistoryModel(
                "Hoàn thành",
                90,
                "Tặng quà Tết",
                "Phát quà",
                "01/02/2024",
                "8:00 - 12:00",
                "#22C55E", // Green
                true
        ));

        list.add(new VolunteerHistoryModel(
                "Đã hủy",
                0,
                "Dọn dẹp bãi biển",
                "Tình nguyện viên",
                "15/10/2024",
                "6:00 - 10:00",
                "#EF4444", // Red
                false
        ));

        return list;
    }
}
