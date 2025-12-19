package com.example.little_share.ui.volunteer;

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
import com.example.little_share.data.models.volunteer.VolunteerHistoryModel;

import java.util.ArrayList;
import java.util.List;

public class frm_volunteer_calendar extends Fragment {

    private RecyclerView rvHistory;
    private VolunteerHistoryAdapter adapter;
    private ImageButton btnBack;
    private List<VolunteerHistoryModel> historyList;

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
        loadHistoryData();
        setupClickListeners();
    }

    private void initViews(View view) {
        rvHistory = view.findViewById(R.id.rv_history);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        adapter = new VolunteerHistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(adapter);

        // Xử lý sự kiện click item
        adapter.setClickListener((history, position) -> {
            Toast.makeText(getContext(),
                    "Chi tiết: " + history.getCampaignTitle(),
                    Toast.LENGTH_SHORT).show();

            // Có thể navigate đến màn hình chi tiết
            // NavController navController = Navigation.findNavController(requireView());
            // Bundle bundle = new Bundle();
            // bundle.putString("campaignTitle", history.getCampaignTitle());
            // navController.navigate(R.id.action_to_detail, bundle);
        });
    }

    private void loadHistoryData() {
        historyList = new ArrayList<>();

        // Thêm dữ liệu mẫu - Hoạt động đã hoàn thành
        historyList.add(new VolunteerHistoryModel(
                "✓ Hoàn thành",
                70,
                "Nấu ăn cho em",
                "Đầu bếp",
                "28/10/2024",
                "8:00 - 12:00",
                "#22C55E",  // Green
                true
        ));

        historyList.add(new VolunteerHistoryModel(
                "✓ Hoàn thành",
                85,
                "Dạy học cho trẻ vùng cao",
                "Giáo viên",
                "25/10/2024",
                "13:00 - 17:00",
                "#22C55E",
                true
        ));

        historyList.add(new VolunteerHistoryModel(
                "✓ Hoàn thành",
                60,
                "Phát quà Trung thu",
                "Phụ trách",
                "20/10/2024",
                "9:00 - 15:00",
                "#22C55E",
                true
        ));

        // Hoạt động sắp tới
        historyList.add(new VolunteerHistoryModel(
                "⏰ Sắp tới",
                0,
                "Mùa đông ấm áp",
                "Hỗ trợ",
                "05/11/2024",
                "7:00 - 11:00",
                "#F59E0B",  // Amber/Orange
                false
        ));

        historyList.add(new VolunteerHistoryModel(
                "⏰ Sắp tới",
                0,
                "Xây dựng trường học",
                "Công nhân",
                "12/11/2024",
                "6:00 - 16:00",
                "#F59E0B",
                false
        ));

        // Hoạt động đang diễn ra
        historyList.add(new VolunteerHistoryModel(
                "▶ Đang diễn ra",
                0,
                "Bữa cơm Nghĩa Tình",
                "Đầu bếp",
                "28/11/2024",
                "8:00 - 12:00",
                "#3B82F6",  // Blue
                false
        ));

        // Hoạt động đã hủy
        historyList.add(new VolunteerHistoryModel(
                "✕ Đã hủy",
                0,
                "Trao học bổng",
                "MC",
                "15/10/2024",
                "14:00 - 18:00",
                "#EF4444",  // Red
                false
        ));

        adapter.setHistoryList(historyList);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            // Xử lý nút back
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }
}