package com.example.little_share.ui.ngo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.VolunteerInfo;
import com.example.little_share.data.models.VolunteerRegistration;
import com.example.little_share.ui.ngo.adapter.VolunteerListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class frm_ngo_volunteer_list extends Fragment {

    private RecyclerView rvPendingList;
    private EditText edtSearch;
    private ImageView btnBack;

    private VolunteerListAdapter adapter;
    private FirebaseFirestore db;
    private String organizationId;

    private List<VolunteerRegistration> allRegistrations = new ArrayList<>();
    private List<VolunteerInfo> allVolunteerInfos = new ArrayList<>();
    private String currentFilter = "all";

    // Thêm loading state
    private boolean isLoading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_ngo_volunteer_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        organizationId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initViews(view);
        setupRecyclerView();
        setupSearch();
        loadVolunteers();
    }

    private void initViews(View view) {
        rvPendingList = view.findViewById(R.id.rvPendingList);
        edtSearch = view.findViewById(R.id.edtSearch);
        btnBack = view.findViewById(R.id.btnBack);

        // Ẩn nút back vì đây là fragment trong bottom nav
        btnBack.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new VolunteerListAdapter();
        rvPendingList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPendingList.setAdapter(adapter);

        adapter.setOnItemClickListener(registration -> {
            // Xử lý khi click xem chi tiết
            Toast.makeText(getContext(),
                    "TNV: " + registration.getUserName() + "\nChiến dịch: " + registration.getCampaignName(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadVolunteers() {
        if (isLoading) return;
        isLoading = true;

        db.collection("volunteer_registrations")
                .whereEqualTo("organizationId", organizationId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allRegistrations.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        VolunteerRegistration reg = doc.toObject(VolunteerRegistration.class);
                        reg.setId(doc.getId());

                        String status = reg.getStatus();
                        if ("approved".equals(status) || "joined".equals(status) || "completed".equals(status)) {
                            allRegistrations.add(reg);
                        }
                    }

                    // Load thông tin user đầy đủ
                    loadFullUserInfo();
                })
                .addOnFailureListener(e -> {
                    isLoading = false;
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFullUserInfo() {
        if (allRegistrations.isEmpty()) {
            filterData();
            return;
        }

        allVolunteerInfos.clear();

        // Group registrations by userId để tránh trùng lặp
        Map<String, List<VolunteerRegistration>> groupedByUser = new HashMap<>();

        for (VolunteerRegistration reg : allRegistrations) {
            String userId = reg.getUserId();
            if (userId != null) {
                if (!groupedByUser.containsKey(userId)) {
                    groupedByUser.put(userId, new ArrayList<>());
                }
                groupedByUser.get(userId).add(reg);
            }
        }

        final int[] loadedCount = {0};
        int totalUsers = groupedByUser.size();

        if (totalUsers == 0) {
            filterData();
            return;
        }

        for (Map.Entry<String, List<VolunteerRegistration>> entry : groupedByUser.entrySet()) {
            String userId = entry.getKey();
            List<VolunteerRegistration> userRegistrations = entry.getValue();

            // Lấy registration đầu tiên để hiển thị thông tin user
            VolunteerRegistration firstReg = userRegistrations.get(0);

            // Load thông tin user từ collection users
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        VolunteerInfo volunteerInfo = new VolunteerInfo(firstReg);

                        if (userDoc.exists()) {
                            String fullName = userDoc.getString("fullName");
                            String avatar = userDoc.getString("avatar");
                            Long totalPoints = userDoc.getLong("totalPoints");

                            if (fullName != null && !fullName.isEmpty()) {
                                firstReg.setUserName(fullName);
                            }

                            volunteerInfo.setAvatar(avatar);
                            volunteerInfo.setTotalPoints(totalPoints != null ? totalPoints.intValue() : 0);
                        } else {
                            volunteerInfo.setTotalPoints(0);
                        }

                        // Sử dụng Cách 2: Tính số sự kiện chính xác cho tổ chức này
                        calculateEventsForOrganization(userId, volunteerInfo, () -> {
                            allVolunteerInfos.add(volunteerInfo);
                            loadedCount[0]++;

                            if (loadedCount[0] >= totalUsers) {
                                filterData();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        VolunteerInfo volunteerInfo = new VolunteerInfo(firstReg);
                        volunteerInfo.setTotalPoints(0);

                        // Tính số sự kiện ngay cả khi load user thất bại
                        calculateEventsForOrganization(userId, volunteerInfo, () -> {
                            allVolunteerInfos.add(volunteerInfo);
                            loadedCount[0]++;

                            if (loadedCount[0] >= totalUsers) {
                                filterData();
                            }
                        });
                    });
        }
    }

    private void calculateEventsForOrganization(String userId, VolunteerInfo volunteerInfo, Runnable onComplete) {
        db.collection("volunteer_registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("organizationId", organizationId) // Chỉ tính sự kiện của tổ chức này
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Đếm số sự kiện duy nhất (theo campaignId)
                    Set<String> uniqueCampaigns = new HashSet<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        VolunteerRegistration reg = doc.toObject(VolunteerRegistration.class);
                        String status = reg.getStatus();
                        String campaignId = reg.getCampaignId();

                        // Chỉ tính các sự kiện đã tham gia hoặc hoàn thành
                        if (("approved".equals(status) || "joined".equals(status) || "completed".equals(status))
                                && campaignId != null && !campaignId.isEmpty()) {
                            uniqueCampaigns.add(campaignId);
                        }
                    }

                    volunteerInfo.setTotalCampaigns(uniqueCampaigns.size());

                    // Log để debug
                    android.util.Log.d("EVENTS_DEBUG",
                            "User: " + volunteerInfo.getRegistration().getUserName() +
                                    " - Events: " + uniqueCampaigns.size());

                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("EVENTS_ERROR", "Error calculating events for user: " + userId, e);
                    volunteerInfo.setTotalCampaigns(0);
                    onComplete.run();
                });
    }

    private void filterData() {
        isLoading = false; // Reset loading state

        String searchText = edtSearch.getText().toString().toLowerCase().trim();
        List<VolunteerInfo> filtered = new ArrayList<>();

        for (VolunteerInfo volunteerInfo : allVolunteerInfos) {
            VolunteerRegistration reg = volunteerInfo.getRegistration();

            // Filter theo search
            boolean matchSearch = searchText.isEmpty() ||
                    (reg.getUserName() != null && reg.getUserName().toLowerCase().contains(searchText)) ||
                    (reg.getUserEmail() != null && reg.getUserEmail().toLowerCase().contains(searchText));

            if (matchSearch) {
                filtered.add(volunteerInfo);
            }
        }

        adapter.setData(filtered);
    }
}
