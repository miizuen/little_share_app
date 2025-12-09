package com.example.little_share.ui.ngo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.little_share.R;
import com.example.little_share.adapter.CampaignAdapter;
import com.example.little_share.data.models.CampaignModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class frm_ngo_campaign_list extends Fragment {

    private RecyclerView rvCampaigns;
    private CampaignAdapter adapter;
    private List<CampaignModel> campaignList;
    private List<CampaignModel> filteredList;

    private EditText etSearch;
    private TextView chipAll, chipOngoing, chipCompleted, chipCancelled;
    private String selectedFilter = "Tất cả";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_ngo_campaign_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        createSampleData();
        setupRecyclerView();
        setupFilterChips();
        setupSearch();
    }

    private void initViews(View view) {
        rvCampaigns = view.findViewById(R.id.rvCampaigns);
        etSearch = view.findViewById(R.id.etSearch);
        chipAll = view.findViewById(R.id.chipAll);
        chipOngoing = view.findViewById(R.id.chipOngoing);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        chipCancelled = view.findViewById(R.id.chipCancelled);
    }

    private void createSampleData() {
        campaignList = new ArrayList<>();

        // Campaign 1
        CampaignModel campaign1 = new CampaignModel(
                "1",
                "Nấu ăn cho em",
                "Nấu ăn và dinh dưỡng",
                "Đang diễn ra",
                "Sơn La",
                "01/11/2025",
                "29/12/2025",
                45, 62,
                50,
                120000000L,
                150000000L,
                R.drawable.img_nauanchoem
        );
        campaign1.setSponsors(Arrays.asList(
                new CampaignModel.Sponsor("Công ty TNHH Alex", 50000000L),
                new CampaignModel.Sponsor("Trung Tâm ngoại ngữ Dazapease", 70000000L)
        ));
        campaignList.add(campaign1);

        // Campaign 2
        CampaignModel campaign2 = new CampaignModel(
                "2",
                "Tặng sách cho trẻ em vùng cao",
                "Giáo dục",
                "Đang diễn ra",
                "Lai Châu",
                "15/11/2025",
                "30/12/2025",
                30, 50,
                40,
                80000000L,
                100000000L,
                R.drawable.img_nauanchoem
        );
        campaignList.add(campaign2);

        // Campaign 3
        CampaignModel campaign3 = new CampaignModel(
                "3",
                "Xây dựng nhà tình nghĩa",
                "Nhà ở",
                "Đã hoàn thành",
                "Hà Giang",
                "01/09/2025",
                "31/10/2025",
                50, 50,
                100,
                200000000L,
                200000000L,
                R.drawable.img_nauanchoem
        );
        campaignList.add(campaign3);

        // Campaign 4
        CampaignModel campaign4 = new CampaignModel(
                "4",
                "Khám bệnh miễn phí",
                "Y tế",
                "Đang diễn ra",
                "Điện Biên",
                "20/11/2025",
                "20/12/2025",
                20, 40,
                60,
                50000000L,
                80000000L,
                R.drawable.img_nauanchoem
        );
        campaignList.add(campaign4);

        // Campaign 5
        CampaignModel campaign5 = new CampaignModel(
                "5",
                "Trồng cây xanh",
                "Môi trường",
                "Đã hủy",
                "Hòa Bình",
                "01/10/2025",
                "15/10/2025",
                10, 30,
                30,
                0L,
                50000000L,
                R.drawable.img_nauanchoem
        );
        campaignList.add(campaign5);

        filteredList = new ArrayList<>(campaignList);
    }

    private void setupRecyclerView() {
        adapter = new CampaignAdapter(filteredList);
        rvCampaigns.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCampaigns.setAdapter(adapter);

        adapter.setOnCampaignClickListener(new CampaignAdapter.OnCampaignClickListener() {
            @Override
            public void onEditClick(CampaignModel campaign, int position) {
                Toast.makeText(getContext(),
                        "Chỉnh sửa: " + campaign.getName(),
                        Toast.LENGTH_SHORT).show();
                // TODO: Navigate to edit screen
            }

            @Override
            public void onViewDetailsClick(CampaignModel campaign, int position) {
                Toast.makeText(getContext(),
                        "Chi tiết: " + campaign.getName(),
                        Toast.LENGTH_SHORT).show();
                // TODO: Navigate to detail screen
            }
        });
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFilter(chipAll, "Tất cả");
            }
        });

        chipOngoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFilter(chipOngoing, "Đang diễn ra");
            }
        });

        chipCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFilter(chipCompleted, "Đã hoàn thành");
            }
        });

        chipCancelled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFilter(chipCancelled, "Đã hủy");
            }
        });
    }

    private void selectFilter(TextView selectedChip, String filter) {
        // Reset all chips
        resetChipStyle(chipAll);
        resetChipStyle(chipOngoing);
        resetChipStyle(chipCompleted);
        resetChipStyle(chipCancelled);

        // Set selected chip style
        selectedChip.setBackgroundResource(R.drawable.bg_chip_selected);
        selectedChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        // Apply filter
        selectedFilter = filter;
        applyFilters();
    }

    private void resetChipStyle(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_rounded_corners_white);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilters() {
        String searchText = etSearch.getText().toString().toLowerCase().trim();

        filteredList = new ArrayList<>();

        for (CampaignModel campaign : campaignList) {
            // Filter by status
            boolean matchesStatus = selectedFilter.equals("Tất cả") ||
                    campaign.getStatus().equals(selectedFilter);

            // Filter by search text
            boolean matchesSearch = searchText.isEmpty() ||
                    campaign.getName().toLowerCase().contains(searchText) ||
                    campaign.getCategory().toLowerCase().contains(searchText) ||
                    campaign.getLocation().toLowerCase().contains(searchText);

            if (matchesStatus && matchesSearch) {
                filteredList.add(campaign);
            }
        }

        adapter.updateList(filteredList);
    }
}