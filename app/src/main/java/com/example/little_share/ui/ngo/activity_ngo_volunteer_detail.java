package com.example.little_share.ui.ngo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.VolunteerEventDetail;
import com.example.little_share.data.models.VolunteerRegistration;
import com.example.little_share.ui.ngo.adapter.VolunteerEventAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class activity_ngo_volunteer_detail extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvVolunteerName, tvTotalPoints, tvTotalEvents;
    private TextView tabAll, tabJoined, tabCompleted, tabRegistered;
    private RecyclerView rvEvents;

    private VolunteerEventAdapter adapter;
    private FirebaseFirestore db;

    private String userId;
    private String organizationId;
    private String currentFilter = "all";
    private List<VolunteerEventDetail> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngo_volunteer_detail);

        db = FirebaseFirestore.getInstance();

        // Lấy thông tin từ Intent
        userId = getIntent().getStringExtra("USER_ID");
        organizationId = getIntent().getStringExtra("ORGANIZATION_ID");
        String volunteerName = getIntent().getStringExtra("VOLUNTEER_NAME");
        int totalPoints = getIntent().getIntExtra("TOTAL_POINTS", 0);
        int totalEventsCount = getIntent().getIntExtra("TOTAL_EVENTS", 0);

        initViews();
        setupRecyclerView();
        setupTabFilters();

        // Set thông tin header
        tvVolunteerName.setText(volunteerName != null ? volunteerName : "N/A");
        tvTotalPoints.setText(totalPoints + " điểm");
        tvTotalEvents.setText(totalEventsCount + " sự kiện");

        loadVolunteerEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvVolunteerName = findViewById(R.id.tvVolunteerName);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        tvTotalEvents = findViewById(R.id.tvTotalEvents);

        tabAll = findViewById(R.id.tabAll);
        tabJoined = findViewById(R.id.tabJoined);
        tabCompleted = findViewById(R.id.tabCompleted);
        tabRegistered = findViewById(R.id.tabRegistered);

        rvEvents = findViewById(R.id.rvEvents);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new VolunteerEventAdapter();
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(adapter);
    }

    private void setupTabFilters() {
        tabAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateTabUI();
            filterEvents();
        });

        tabJoined.setOnClickListener(v -> {
            currentFilter = "joined";  // Đã tham gia
            updateTabUI();
            filterEvents();
        });

        tabCompleted.setOnClickListener(v -> {
            currentFilter = "completed";  // Hoàn thành
            updateTabUI();
            filterEvents();
        });

        tabRegistered.setOnClickListener(v -> {
            currentFilter = "approved";  // Đã đăng ký (pending)
            updateTabUI();
            filterEvents();
        });
    }


    private void updateTabUI() {
        // Reset tất cả tabs về trạng thái unselected
        resetTabStyle(tabAll);
        resetTabStyle(tabJoined);
        resetTabStyle(tabCompleted);
        resetTabStyle(tabRegistered);

        // Highlight tab được chọn
        TextView selectedTab = null;
        switch (currentFilter) {
            case "all":
                selectedTab = tabAll;
                break;
            case "joined":
                selectedTab = tabJoined;
                break;
            case "completed":
                selectedTab = tabCompleted;
                break;
            case "approved":
                selectedTab = tabRegistered;
                break;
        }

        if (selectedTab != null) {
            setSelectedTabStyle(selectedTab);
        }
    }

    // Helper methods để set style
    private void resetTabStyle(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_tab_unselected);
        tab.setTextColor(getColor(android.R.color.darker_gray));
    }

    private void setSelectedTabStyle(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_tab_selected);
        tab.setTextColor(getColor(android.R.color.white));
    }


    private void loadVolunteerEvents() {
        db.collection("volunteer_registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("organizationId", organizationId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    android.util.Log.d("DETAIL_DEBUG", "Total records: " + querySnapshot.size());

                    allEvents.clear();
                    Set<String> uniqueEventKeys = new HashSet<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        VolunteerRegistration reg = doc.toObject(VolunteerRegistration.class);

                        String eventName = reg.getCampaignName() != null ? reg.getCampaignName() : "";
                        String eventDate = reg.getDate() != null ? reg.getDate() : "";
                        String status = reg.getStatus() != null ? reg.getStatus() : "";

                        android.util.Log.d("DETAIL_DEBUG",
                                "Event: " + eventName + ", Status: " + status + ", Date: " + eventDate);

                        // ✅ THÊM: Loại bỏ các sự kiện đã hủy và bị từ chối
                        if ("cancelled".equals(status) || "rejected".equals(status)) {
                            android.util.Log.d("DETAIL_DEBUG", "Skipping cancelled/rejected event: " + eventName);
                            continue; // Bỏ qua sự kiện đã hủy hoặc bị từ chối
                        }

                        String uniqueKey = eventName + "|" + eventDate + "|" + status;

                        if (!uniqueEventKeys.contains(uniqueKey)) {
                            uniqueEventKeys.add(uniqueKey);

                            VolunteerEventDetail event = new VolunteerEventDetail();
                            event.setEventId(reg.getCampaignId());
                            event.setEventName(eventName);
                            event.setEventDate(eventDate);
                            event.setStatus(status);
                            event.setRoleName(reg.getRoleName());
                            event.setPointsEarned(reg.getPointsEarned());

                            allEvents.add(event);
                        }
                    }

                    calculateUniqueEventCount();
                    filterEvents();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi load dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }






    private void filterEvents() {
        List<VolunteerEventDetail> filtered = new ArrayList<>();

        for (VolunteerEventDetail event : allEvents) {
            boolean shouldInclude = false;
            String status = event.getStatus();

            switch (currentFilter) {
                case "all":
                    // Hiển thị tất cả
                    shouldInclude = true;
                    break;

                case "approved":
                    // Tab "Đã đăng ký" - chỉ hiển thị status pending
                    shouldInclude = "pending".equals(status);
                    break;

                case "joined":
                    // Tab "Đã tham gia" - hiển thị approved, joined, attended
                    shouldInclude = "approved".equals(status) ||
                            "joined".equals(status) ||
                            "attended".equals(status);
                    break;

                case "completed":
                    // Tab "Hoàn thành" - chỉ hiển thị completed
                    shouldInclude = "completed".equals(status);
                    break;

                default:
                    shouldInclude = false;
            }

            if (shouldInclude) {
                filtered.add(event);
            }
        }

        // Log để debug
        android.util.Log.d("FILTER_DEBUG",
                "Filter: " + currentFilter + ", Total: " + allEvents.size() + ", Filtered: " + filtered.size());

        adapter.setData(filtered);
    }

    private void calculateUniqueEventCount() {
        Set<String> uniqueEventNames = new HashSet<>();

        for (VolunteerEventDetail event : allEvents) {
            String eventName = event.getEventName();
            if (eventName != null && !eventName.trim().isEmpty()) {
                uniqueEventNames.add(eventName.trim());
            }
        }

        int uniqueCount = uniqueEventNames.size();

        // Cập nhật hiển thị
        tvTotalEvents.setText(uniqueCount + " sự kiện");

        // Log để debug
        android.util.Log.d("UNIQUE_EVENTS_DEBUG",
                "Unique event names: " + uniqueEventNames.toString() +
                        " - Count: " + uniqueCount);
    }


}
