package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.ui.volunteer.adapter.UrgentCampaignAdapter;

import java.util.ArrayList;
import java.util.List;

public class fragment_donation_campagin extends Fragment {

    private static final String TAG = "DonationCampaignFrag";
    private RecyclerView rvDonationCampaigns;
    private LinearLayout layoutEmptyState;
    private TextView tvEmptyState;
    private UrgentCampaignAdapter adapter;
    private CampaignRepository repository;

    public fragment_donation_campagin() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donation_campagin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "Fragment created");

        repository = new CampaignRepository();
        initViews(view);
        setupRecyclerView();
        loadDonationCampaigns();
    }

    private void initViews(View view) {
        rvDonationCampaigns = view.findViewById(R.id.rvDonationCampaigns);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        if (rvDonationCampaigns == null) {
            Log.e(TAG, "RecyclerView is NULL! Check layout file.");
        } else {
            Log.d(TAG, "RecyclerView found");
        }
    }

    private void loadDonationCampaigns() {
        Log.d(TAG, "=== STARTING TO LOAD DONATION CAMPAIGNS ===");

        repository.getDonationCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            Log.d(TAG, "Observer triggered!");

            if (campaigns == null) {
                Log.e(TAG, "Campaigns is NULL!");
                return;
            }

            Log.d(TAG, "Received " + campaigns.size() + " campaigns");

            // Show/hide empty state
            if (campaigns.isEmpty()) {
                rvDonationCampaigns.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing empty state");
            } else {
                rvDonationCampaigns.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);

                // Log chi tiết từng campaign
                for (int i = 0; i < campaigns.size(); i++) {
                    Campaign c = campaigns.get(i);
                    Log.d(TAG, "Campaign " + i + ":");
                    Log.d(TAG, "  - Name: " + c.getName());
                    Log.d(TAG, "  - Type: " + c.getCampaignType());
                    Log.d(TAG, "  - Category: " + c.getCategory());
                    Log.d(TAG, "  - Location: " + c.getLocation());
                }
            }

            adapter.updateData(campaigns);
            Log.d(TAG, "Adapter now has " + adapter.getItemCount() + " items");
        });
    }

    private void setupRecyclerView() {
        if (rvDonationCampaigns == null) return;

        adapter = new UrgentCampaignAdapter(getContext(), new ArrayList<>(),
                campaign -> {
                    Log.d(TAG, "Campaign clicked: " + campaign.getName());
                    // Navigate to donation campaign detail
                    Intent intent = new Intent(getContext(), activity_volunteer_donation_campaign_detail.class);
                    intent.putExtra("campaign", campaign);
                    startActivity(intent);
                });

        rvDonationCampaigns.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDonationCampaigns.setAdapter(adapter);

        Log.d(TAG, "RecyclerView setup complete");
    }
}