package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.adapter.volunteer.VolunteerHistoryAdapter;
import com.example.little_share.data.models.VolunteerRegistration;
import com.example.little_share.data.models.volunteer.VolunteerHistoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class frm_volunteer_calendar extends Fragment {

    private RecyclerView rvHistory;
    private VolunteerHistoryAdapter adapter;
    private ImageButton btnBack;
    private List<VolunteerHistoryModel> historyList;

    // Firebase instance
    private FirebaseFirestore db;
    private String currentUserId;

    // Danh sách VolunteerRegistration để mapping với historyList
    private List<VolunteerRegistration> registrationsList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_volunteer_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadUserRegistrations(); // Load dữ liệu thật từ Firebase
        setupClickListeners();
    }

    private void initViews(View view) {
        rvHistory = view.findViewById(R.id.rv_history);
        btnBack = view.findViewById(R.id.btnBack);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new VolunteerHistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(adapter);

        // Xử lý sự kiện click item
        adapter.setClickListener((history, position) -> {
            // Kiểm tra index hợp lệ
            if (position < registrationsList.size()) {
                VolunteerRegistration registration = registrationsList.get(position);

                Intent intent = new Intent(getContext(), activity_volunteer_detail_calendar.class);
                intent.putExtra("registration", registration);
                startActivity(intent);
            }
        });
    }

    private void loadUserRegistrations() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem lịch", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserId = user.getUid();
        android.util.Log.d("CALENDAR_DEBUG", "Loading registrations for user: " + currentUserId);

        // Query registrations (bỏ orderBy để tránh lỗi index)
        db.collection("volunteer_registrations")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("CALENDAR_DEBUG", "Found " + queryDocumentSnapshots.size() + " documents");

                    historyList = new ArrayList<>();
                    registrationsList.clear();

                    List<VolunteerRegistration> tempList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        VolunteerRegistration reg = doc.toObject(VolunteerRegistration.class);
                        reg.setId(doc.getId());

                        // Filter theo status nếu cần
                        if ("approved".equals(reg.getStatus())) {
                            // CHỈ HIỂN THỊ CÁC ĐĂNG KÝ ĐÃ DUYỆT NHƯNG CHƯA HOÀN THÀNH
                            tempList.add(reg);
                        }
                    }

                    // Load campaign points cho từng registration
                    loadCampaignPointsForRegistrations(tempList);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CALENDAR_DEBUG", "Query failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void loadCampaignPointsForRegistrations(List<VolunteerRegistration> registrations) {
        if (registrations.isEmpty()) {
            Toast.makeText(getContext(), "Chưa có lịch tình nguyện nào được duyệt", Toast.LENGTH_LONG).show();
            return;
        }

        android.util.Log.d("CALENDAR_DEBUG", "Loading points for " + registrations.size() + " registrations");

        // Counter để track số lượng đã load xong
        final int[] loadedCount = {0};
        final int totalCount = registrations.size();

        for (VolunteerRegistration reg : registrations) {
            if (reg.getCampaignId() == null || reg.getCampaignId().isEmpty()) {
                // Nếu không có campaignId, dùng điểm mặc định
                processRegistrationWithPoints(reg, 0);
                loadedCount[0]++;

                if (loadedCount[0] == totalCount) {
                    finishLoadingData();
                }
                continue;
            }

            // Load campaign để lấy pointsReward
            db.collection("campaigns")
                    .document(reg.getCampaignId())
                    .get()
                    .addOnSuccessListener(campaignDoc -> {
                        int points = 0;

                        if (campaignDoc.exists()) {
                            // Lấy pointsReward từ campaign
                            Long pointsReward = campaignDoc.getLong("pointsReward");
                            if (pointsReward != null) {
                                points = pointsReward.intValue();
                            }

                            android.util.Log.d("CALENDAR_DEBUG", "Campaign " + reg.getCampaignName() + " has " + points + " points");
                        } else {
                            android.util.Log.w("CALENDAR_DEBUG", "Campaign not found: " + reg.getCampaignId());
                        }

                        // Xử lý registration với điểm đã load
                        processRegistrationWithPoints(reg, points);

                        loadedCount[0]++;
                        if (loadedCount[0] == totalCount) {
                            finishLoadingData();
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("CALENDAR_DEBUG", "Failed to load campaign: " + e.getMessage());

                        // Dùng điểm mặc định nếu lỗi
                        processRegistrationWithPoints(reg, 0);

                        loadedCount[0]++;
                        if (loadedCount[0] == totalCount) {
                            finishLoadingData();
                        }
                    });
        }
    }
    private void processRegistrationWithPoints(VolunteerRegistration reg, int points) {
        // Set điểm vào registration
        reg.setPoints(points);

        // Thêm vào danh sách
        registrationsList.add(reg);

        // Chuyển đổi sang VolunteerHistoryModel
        VolunteerHistoryModel historyModel = convertToHistoryModel(reg);
        historyList.add(historyModel);
    }
    private void finishLoadingData() {
        // Sắp xếp theo thời gian tạo (mới nhất trước)
        registrationsList.sort((a, b) -> {
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        // Sắp xếp historyList theo cùng thứ tự
        historyList.sort((a, b) -> {
            // Tìm registration tương ứng để so sánh createdAt
            VolunteerRegistration regA = findRegistrationByCampaignName(a.getCampaignTitle());
            VolunteerRegistration regB = findRegistrationByCampaignName(b.getCampaignTitle());

            if (regA == null || regA.getCreatedAt() == null) return 1;
            if (regB == null || regB.getCreatedAt() == null) return -1;
            return regB.getCreatedAt().compareTo(regA.getCreatedAt());
        });

        // Cập nhật adapter
        adapter.setHistoryList(historyList);

        android.util.Log.d("CALENDAR_DEBUG", "Finished loading " + historyList.size() + " items with points");

        if (historyList.isEmpty()) {
            Toast.makeText(getContext(), "Chưa có lịch tình nguyện nào được duyệt", Toast.LENGTH_LONG).show();
        }
    }

    private VolunteerRegistration findRegistrationByCampaignName(String campaignName) {
        for (VolunteerRegistration reg : registrationsList) {
            if (campaignName.equals(reg.getCampaignName())) {
                return reg;
            }
        }
        return null;
    }




    private VolunteerHistoryModel convertToHistoryModel(VolunteerRegistration reg) {
        String statusText = getStatusText(reg.getStatus());
        String statusColor = getStatusColor(reg.getStatus());
        boolean isCompleted = "approved".equals(reg.getStatus()) || "completed".equals(reg.getStatus());

        // === SỬA LOGIC ĐIỂM ===
        int points = reg.getPoints(); // Điểm từ campaign (đã load)

        android.util.Log.d("POINTS_DEBUG", "=== Converting Registration ===");
        android.util.Log.d("POINTS_DEBUG", "Campaign: " + reg.getCampaignName());
        android.util.Log.d("POINTS_DEBUG", "Status: " + reg.getStatus());
        android.util.Log.d("POINTS_DEBUG", "Points from campaign: " + points);

        // QUAN TRỌNG: Luôn hiển thị điểm, không phân biệt trạng thái
        // Nếu muốn chỉ hiển thị khi completed, bỏ comment dòng dưới:
        // if (!"completed".equals(reg.getStatus())) {
        //     points = 0;
        // }

        android.util.Log.d("POINTS_DEBUG", "Final points to display: " + points);

        // Xử lý null safety
        String campaignName = reg.getCampaignName();
        if (campaignName == null || campaignName.isEmpty()) {
            campaignName = "Chiến dịch không xác định";
        }

        String roleName = reg.getRoleName();
        if (roleName == null || roleName.isEmpty()) {
            roleName = "Tình nguyện viên";
        }

        String date = reg.getDate();
        if (date == null || date.isEmpty()) {
            date = "Chưa xác định";
        }

        String shiftTime = reg.getShiftTime();
        if (shiftTime == null || shiftTime.isEmpty()) {
            shiftTime = "Chưa xác định";
        }

        return new VolunteerHistoryModel(
                statusText,
                points, // Điểm luôn được hiển thị
                campaignName,
                roleName,
                date,
                shiftTime,
                statusColor,
                isCompleted
        );
    }




    private String getStatusText(String status) {
        if (status == null) return "Không xác định";

        switch (status.toLowerCase()) {
            case "approved": return "✓ Đã duyệt";
            case "pending": return "⏳ Chờ duyệt";
            case "rejected": return "✕ Từ chối";
            case "completed": return "🎉 Hoàn thành";
            default: return "❓ " + status;
        }
    }

    private String getStatusColor(String status) {
        if (status == null) return "#999999";

        switch (status.toLowerCase()) {
            case "approved": return "#22C55E"; // Green
            case "pending": return "#F59E0B";  // Orange
            case "rejected": return "#EF4444"; // Red
            case "completed": return "#8B5CF6"; // Purple
            default: return "#999999";         // Gray
        }
    }


    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            // Quay về trang chủ
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                }
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        // Refresh dữ liệu mỗi khi fragment được hiển thị lại
        refreshData();
    }

    // THÊM METHOD REFRESH
    public void refreshData() {
        android.util.Log.d("CALENDAR_DEBUG", "Refreshing calendar data...");
        loadUserRegistrations();
    }

}
