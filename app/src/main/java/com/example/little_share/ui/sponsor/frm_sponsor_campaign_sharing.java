package com.example.little_share.ui.sponsor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.ui.sponsor.adapter.SponsorCampaignHistoryAdapter;

import java.util.ArrayList;

public class frm_sponsor_campaign_sharing extends Fragment {
    private static final String TAG = "CampaignSharing";

    private RecyclerView rvCampaignsHistory;
    private SponsorCampaignHistoryAdapter adapter;
    private CampaignRepository campaignRepository;
    private View layoutEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sponsor_campaign_sharing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        campaignRepository = new CampaignRepository();

        initViews(view);
        setupRecyclerView();
        loadSponsoredCampaigns();
    }

    private void initViews(View view) {
        rvCampaignsHistory = view.findViewById(R.id.rvCampaignsHistory);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
    }

    private void setupRecyclerView() {
        adapter = new SponsorCampaignHistoryAdapter(getContext(), new ArrayList<>(),
                new SponsorCampaignHistoryAdapter.OnCampaignHistoryClickListener() {
                    @Override
                    public void onCampaignClick(Campaign campaign) {
                        Log.d(TAG, "=== onCampaignClick called ===");
                        Log.d(TAG, "Campaign: " + campaign.getName());
                        // Bấm vào card → Xem detail campaign (để donate thêm)
                        openCampaignDetail(campaign);
                    }

                    @Override
                    public void onDetailClick(Campaign campaign) {
                        Log.d(TAG, "=== onDetailClick called ===");
                        Log.d(TAG, "Campaign: " + campaign.getName());
                        // Bấm vào nút "Xem báo cáo" → Xem báo cáo trực tiếp
                        openReportDetail(campaign);
                    }
                });

        rvCampaignsHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCampaignsHistory.setAdapter(adapter);
    }

    private void loadSponsoredCampaigns() {
        Log.d(TAG, "Loading sponsored campaigns...");

        campaignRepository.getSponsoredCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            if (campaigns != null && isAdded()) {
                Log.d(TAG, "Received " + campaigns.size() + " sponsored campaigns");
                adapter.updateData(campaigns);

                // Show/hide empty state
                if (campaigns.isEmpty()) {
                    Log.d(TAG, "No sponsored campaigns found");
                    rvCampaignsHistory.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "Displaying " + campaigns.size() + " campaigns");
                    rvCampaignsHistory.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);
                }
            } else {
                Log.e(TAG, "Campaigns is null or fragment not added");
            }
        });
    }

    private void openCampaignDetail(Campaign campaign) {
        if (!isAdded() || getActivity() == null) {
            Log.e(TAG, "Fragment not added or activity is null");
            return;
        }

        Log.d(TAG, "Opening campaign detail for: " + campaign.getName());

        // Create campaign detail fragment
        frm_campaign_detail_sponsor detailFragment = new frm_campaign_detail_sponsor();

        // Pass campaign data via Bundle
        Bundle bundle = new Bundle();
        bundle.putString("campaign_id", campaign.getId());
        bundle.putString("campaign_name", campaign.getName());
        bundle.putSerializable("campaign_data", campaign);
        detailFragment.setArguments(bundle);

        // Navigate to campaign detail fragment
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack("campaign_detail")
                .commit();

        Log.d(TAG, "Transaction committed for campaign detail");
    }

    private void openReportDetail(Campaign campaign) {
        if (!isAdded() || getActivity() == null) {
            Log.e(TAG, "Fragment not added or activity is null");
            return;
        }

        Log.d(TAG, "=== OPENING REPORT DETAIL ===");
        Log.d(TAG, "Campaign ID: " + campaign.getId());
        Log.d(TAG, "Campaign Name: " + campaign.getName());

        // Create report detail fragment
        frm_sponsor_campaign_detail_report reportFragment = new frm_sponsor_campaign_detail_report();

        // Pass campaign data via Bundle
        Bundle bundle = new Bundle();
        bundle.putString("campaign_id", campaign.getId());
        bundle.putString("campaign_name", campaign.getName());
        bundle.putSerializable("campaign_data", campaign);
        reportFragment.setArguments(bundle);

        // Navigate to report detail fragment
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, reportFragment)
                .addToBackStack("report_detail")
                .commit();

        Log.d(TAG, "=== TRANSACTION COMMITTED FOR REPORT ===");
    }
}