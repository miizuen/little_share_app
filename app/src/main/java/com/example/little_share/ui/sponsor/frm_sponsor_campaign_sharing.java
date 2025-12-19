package com.example.little_share.ui.sponsor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
                        openCampaignDetail(campaign);
                    }

                    @Override
                    public void onDetailClick(Campaign campaign) {
                        openCampaignDetail(campaign);
                    }
                });

        rvCampaignsHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCampaignsHistory.setAdapter(adapter);
    }

    private void loadSponsoredCampaigns() {
        android.util.Log.d("CAMPAIGN_SHARING", "Loading sponsored campaigns...");
        
        campaignRepository.getSponsoredCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            if (campaigns != null && isAdded()) {
                android.util.Log.d("CAMPAIGN_SHARING", "Received " + campaigns.size() + " sponsored campaigns");
                adapter.updateData(campaigns);
                
                // Show/hide empty state
                if (campaigns.isEmpty()) {
                    android.util.Log.d("CAMPAIGN_SHARING", "No sponsored campaigns found");
                    rvCampaignsHistory.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    android.util.Log.d("CAMPAIGN_SHARING", "Displaying " + campaigns.size() + " campaigns");
                    rvCampaignsHistory.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);
                }
            } else {
                android.util.Log.e("CAMPAIGN_SHARING", "Campaigns is null or fragment not added");
            }
        });
    }

    private void openCampaignDetail(Campaign campaign) {
        if (isAdded()) {
            android.util.Log.d("CAMPAIGN_SHARING", "Opening detail for: " + campaign.getName());
            
            // Create detail fragment
            frm_campaign_detail_sponsor detailFragment = new frm_campaign_detail_sponsor();

            // Pass campaign data via Bundle
            Bundle bundle = new Bundle();
            bundle.putString("campaign_id", campaign.getId());
            bundle.putString("campaign_name", campaign.getName());
            bundle.putSerializable("campaign_data", campaign);
            detailFragment.setArguments(bundle);

            // Navigate to detail fragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, detailFragment)
                    .addToBackStack("campaign_detail")
                    .commit();
        }
    }
}