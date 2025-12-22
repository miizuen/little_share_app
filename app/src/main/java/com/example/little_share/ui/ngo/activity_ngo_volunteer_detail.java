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
import java.util.List;

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
            currentFilter = "joined";
            updateTabUI();
            filterEvents();
        });

        tabCompleted.setOnClickListener(v -> {
            currentFilter = "completed";
            updateTabUI();
            filterEvents();
        });

        tabRegistered.setOnClickListener(v -> {
            currentFilter = "approved";
            updateTabUI();
            filterEvents();
        });
    }

    private void updateTabUI() {
        // Reset tất cả tabs
        tabAll.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabAll.setTextColor(getColor(android.R.color.darker_gray));

        tabJoined.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabJoined.setTextColor(getColor(android.R.color.darker_gray));

        tabCompleted.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabCompleted.setTextColor(getColor(android.R.color.darker_gray));

        tabRegistered.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabRegistered.setTextColor(getColor(android.R.color.darker_gray));

        // Highlight tab được chọn
        TextView selectedTab;
        switch (currentFilter) {
            case "joined": selectedTab = tabJoined; break;
            case "completed": selectedTab = tabCompleted; break;
            case "approved": selectedTab = tabRegistered; break;
            default: selectedTab = tabAll;
        }

        selectedTab.setBackgroundResource(R.drawable.bg_tab_selected);
        selectedTab.setTextColor(getColor(android.R.color.white));
    }

    private void loadVolunteerEvents() {
        db.collection("volunteer_registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("organizationId", organizationId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allEvents.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        VolunteerRegistration reg = doc.toObject(VolunteerRegistration.class);

                        VolunteerEventDetail event = new VolunteerEventDetail();
                        event.setEventId(reg.getCampaignId());
                        event.setEventName(reg.getCampaignName());
                        event.setEventDate(reg.getDate());
                        event.setStatus(reg.getStatus());
                        event.setRoleName(reg.getRoleName());
                        event.setPointsEarned(reg.getPointsEarned());

                        allEvents.add(event);
                    }

                    filterEvents();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi load dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterEvents() {
        List<VolunteerEventDetail> filtered = new ArrayList<>();

        for (VolunteerEventDetail event : allEvents) {
            if (currentFilter.equals("all") || currentFilter.equals(event.getStatus())) {
                filtered.add(event);
            }
        }

        adapter.setData(filtered);
    }
}
