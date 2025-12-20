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
import com.example.little_share.data.models.VolunteerRegistration;
import com.example.little_share.ui.ngo.adapter.VolunteerListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class frm_ngo_volunteer_list extends Fragment {

    private RecyclerView rvPendingList;
    private EditText edtSearch;
    private TextView tabAll, tabJoining, tabCompleted, tabStopped;
    private ImageView btnBack;

    private VolunteerListAdapter adapter;
    private FirebaseFirestore db;
    private String organizationId;

    private List<VolunteerRegistration> allRegistrations = new ArrayList<>();
    private String currentFilter = "all";

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
        setupTabFilters();
        setupSearch();
        loadVolunteers();
    }

    private void initViews(View view) {
        rvPendingList = view.findViewById(R.id.rvPendingList);
        edtSearch = view.findViewById(R.id.edtSearch);
        tabAll = view.findViewById(R.id.tabAll);
        tabJoining = view.findViewById(R.id.tabJoining);
        tabCompleted = view.findViewById(R.id.tabCompleted);
        tabStopped = view.findViewById(R.id.tabStopped);
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

    private void setupTabFilters() {
        tabAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateTabUI();
            filterData();
        });

        tabJoining.setOnClickListener(v -> {
            currentFilter = "approved";
            updateTabUI();
            filterData();
        });

        tabCompleted.setOnClickListener(v -> {
            currentFilter = "completed";
            updateTabUI();
            filterData();
        });

        // Ẩn tab "Đã dừng" nếu không cần
        tabStopped.setVisibility(View.GONE);
    }

    private void updateTabUI() {
        // Reset tất cả tabs
        tabAll.setBackgroundResource(R.drawable.bg_default);
        tabAll.setTextColor(0xFF666666);
        tabJoining.setBackgroundResource(R.drawable.bg_default);
        tabJoining.setTextColor(0xFF666666);
        tabCompleted.setBackgroundResource(R.drawable.bg_default);
        tabCompleted.setTextColor(0xFF666666);

        // Highlight tab được chọn
        TextView selectedTab;
        switch (currentFilter) {
            case "approved": selectedTab = tabJoining; break;
            case "completed": selectedTab = tabCompleted; break;
            default: selectedTab = tabAll;
        }
        selectedTab.setBackgroundResource(R.drawable.bg_chip_selected);
        selectedTab.setTextColor(0xFFFFFFFF);
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
        db.collection("volunteer_registrations")
                .whereEqualTo("organizationId", organizationId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allRegistrations.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        VolunteerRegistration reg = doc.toObject(VolunteerRegistration.class);
                        reg.setId(doc.getId());

                        // Chỉ lấy approved và completed
                        String status = reg.getStatus();
                        if ("approved".equals(status) || "completed".equals(status)) {
                            allRegistrations.add(reg);
                        }
                    }
                    filterData();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterData() {
        String searchText = edtSearch.getText().toString().toLowerCase().trim();
        List<VolunteerRegistration> filtered = new ArrayList<>();

        for (VolunteerRegistration reg : allRegistrations) {
            // Filter theo status
            boolean matchStatus = currentFilter.equals("all") ||
                    currentFilter.equals(reg.getStatus());

            // Filter theo search
            boolean matchSearch = searchText.isEmpty() ||
                    (reg.getUserName() != null && reg.getUserName().toLowerCase().contains(searchText)) ||
                    (reg.getUserEmail() != null && reg.getUserEmail().toLowerCase().contains(searchText)) ||
                    (reg.getCampaignName() != null && reg.getCampaignName().toLowerCase().contains(searchText));

            if (matchStatus && matchSearch) {
                filtered.add(reg);
            }
        }

        adapter.setData(filtered);
    }
}
