package com.example.little_share.ui.ngo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.ui.ngo.adapter.NGOCampaignAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class frm_ngo_campaign_list extends Fragment {

    private RecyclerView rvCampaigns;
    private EditText etSearch;
    private TextView chipAll, chipOngoing, chipCompleted, chipCancelled;
    private ProgressBar progressBar;
    private View layoutEmpty;

    private CampaignRepository repo;
    private NGOCampaignAdapter adapter;
    private List<Campaign> campaignList = new ArrayList<>();
    private List<Campaign> filteredList = new ArrayList<>();
    private String currentFilter = "ALL"; // ALL, ONGOING, COMPLETED, CANCELLED

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_ngo_campaign_list, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        loadCampaigns();

        return view;
    }

    private void initViews(View view) {
        rvCampaigns = view.findViewById(R.id.rvCampaigns);
        etSearch = view.findViewById(R.id.etSearch);
        chipAll = view.findViewById(R.id.chipAll);
        chipOngoing = view.findViewById(R.id.chipOngoing);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        chipCancelled = view.findViewById(R.id.chipCancelled);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        rvCampaigns.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NGOCampaignAdapter(
                getContext(),
                filteredList,
                new NGOCampaignAdapter.OnCampaignActionListener() {
                    @Override
                    public void onEditClick(Campaign campaign) {
                        // TODO: Implement edit campaign
                        Toast.makeText(getContext(),
                                "Chỉnh sửa: " + campaign.getName(),
                                Toast.LENGTH_SHORT).show();

                        // Example:
                        // Intent intent = new Intent(getContext(), activity_ngo_edit_campaign.class);
                        // intent.putExtra("campaignId", campaign.getId());
                        // startActivity(intent);
                    }

                    @Override
                    public void onViewDetailsClick(Campaign campaign) {
                        // Mở màn hình chi tiết
                        Intent intent = new Intent(getContext(), activity_ngo_campaign_detail.class);
                        intent.putExtra("campaignId", campaign.getId());
                        startActivity(intent);
                    }
                }
        );

        rvCampaigns.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCampaigns(s.toString(), currentFilter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> {
            selectChip(chipAll);
            currentFilter = "ALL";
            filterCampaigns(etSearch.getText().toString(), currentFilter);
        });

        chipOngoing.setOnClickListener(v -> {
            selectChip(chipOngoing);
            currentFilter = "ONGOING";
            filterCampaigns(etSearch.getText().toString(), currentFilter);
        });

        chipCompleted.setOnClickListener(v -> {
            selectChip(chipCompleted);
            currentFilter = "COMPLETED";
            filterCampaigns(etSearch.getText().toString(), currentFilter);
        });

        chipCancelled.setOnClickListener(v -> {
            selectChip(chipCancelled);
            currentFilter = "CANCELLED";
            filterCampaigns(etSearch.getText().toString(), currentFilter);
        });
    }

    private void selectChip(TextView selectedChip) {
        // Reset all chips
        resetChip(chipAll);
        resetChip(chipOngoing);
        resetChip(chipCompleted);
        resetChip(chipCancelled);

        // Highlight selected chip
        selectedChip.setBackgroundResource(R.drawable.bg_chip_selected);
        selectedChip.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    private void resetChip(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_rounded_corners_white);
        chip.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
    }

    private void loadCampaigns() {
        showLoading(true);

        if (repo == null) {
            repo = new CampaignRepository();
        }

        repo.getCampaignsByCurrentNgo().observe(getViewLifecycleOwner(), campaigns -> {
            showLoading(false);

            if (campaigns != null && !campaigns.isEmpty()) {
                campaignList.clear();
                campaignList.addAll(campaigns);

                // Sắp xếp: Campaign mới nhất lên đầu
                Collections.reverse(campaignList);

                // Apply filter
                filterCampaigns(etSearch.getText().toString(), currentFilter);
                showEmpty(false);
            } else {
                campaignList.clear();
                filteredList.clear();
                adapter.notifyDataSetChanged();
                showEmpty(true);
            }
        });
    }

    private void filterCampaigns(String searchText, String status) {
        filteredList.clear();

        for (Campaign campaign : campaignList) {
            boolean matchesSearch = true;
            boolean matchesStatus = true;

            // Filter by search text
            if (searchText != null && !searchText.isEmpty()) {
                String query = searchText.toLowerCase();
                matchesSearch = campaign.getName().toLowerCase().contains(query) ||
                        (campaign.getLocation() != null &&
                                campaign.getLocation().toLowerCase().contains(query));
            }

            // Filter by status
            if (!status.equals("ALL")) {
                matchesStatus = campaign.getStatus() != null &&
                        campaign.getStatus().equalsIgnoreCase(status);
            }

            // Add if matches both filters
            if (matchesSearch && matchesStatus) {
                filteredList.add(campaign);
            }
        }

        adapter.notifyDataSetChanged();

        // Show/hide empty state
        if (filteredList.isEmpty() && !campaignList.isEmpty()) {
            // Có data nhưng filter không có kết quả
            Toast.makeText(getContext(), "Không tìm thấy chiến dịch phù hợp", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvCampaigns != null) {
            rvCampaigns.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmpty(boolean show) {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvCampaigns != null) {
            rvCampaigns.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data khi quay lại fragment
        loadCampaigns();
    }
}