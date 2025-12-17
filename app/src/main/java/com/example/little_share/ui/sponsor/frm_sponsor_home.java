package com.example.little_share.ui.sponsor;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.User;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.data.repositories.UserRepository;
import com.example.little_share.ui.sponsor.adapter.SponsorCampaignSponsoredAdapter;
import com.example.little_share.ui.sponsor.adapter.SponsorCampaignNeedAdapter;

import java.util.ArrayList;

public class frm_sponsor_home extends Fragment {
    private static final String TAG = "frm_sponsor_home";

    private ImageView ivSponsorLogo;
    private TextView tvSponsorName, tvTotalMoney, tvTotalCampaigns;

    // RecyclerViews
    private RecyclerView rvSponsoredCampaigns, rvNeedSponsorCampaigns;
    private TextView tvEmptySponsored, tvEmptyNeedSponsor;
    private SponsorCampaignNeedAdapter needSponsorAdapter;
    private SponsorCampaignSponsoredAdapter sponsoredAdapter;

    // Repository
    private UserRepository userRepository;
    private CampaignRepository campaignRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sponsor_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userRepository = new UserRepository();
        campaignRepository = new CampaignRepository();

        initViews(view);
        setupRecyclerViews();
        loadSponsorData();
        loadCampaignsNeedingSponsor();
        loadSponsoredCampaigns();
    }

    private void initViews(View view) {
        ivSponsorLogo = view.findViewById(R.id.imageView3);
        tvSponsorName = view.findViewById(R.id.textView3);
        tvTotalMoney = view.findViewById(R.id.tvTotalMoney);
        tvTotalCampaigns = view.findViewById(R.id.tvTotalCampaigns);

        // RecyclerViews
        rvSponsoredCampaigns = view.findViewById(R.id.rvSponsoredCampaigns);
        rvNeedSponsorCampaigns = view.findViewById(R.id.rvNeedSponsorCampaigns);

        // TextView empty states
        tvEmptySponsored = view.findViewById(R.id.tvEmptySponsored);
        tvEmptyNeedSponsor = view.findViewById(R.id.tvEmptyNeedSponsor);
    }

    private void setupRecyclerViews() {
        // Setup adapter cho campaigns cần sponsor
        needSponsorAdapter = new SponsorCampaignNeedAdapter(getContext(), new ArrayList<>(),
                new SponsorCampaignNeedAdapter.OnCampaignClickListener() {
                    @Override
                    public void onDonateClick(Campaign campaign) {
                        openDonationForm(campaign);
                    }

                    @Override
                    public void onCampaignClick(Campaign campaign) {
                        openCampaignDetail(campaign);
                    }
                });

        rvNeedSponsorCampaigns.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNeedSponsorCampaigns.setAdapter(needSponsorAdapter);

        // Setup adapter cho campaigns đã tài trợ
        sponsoredAdapter = new SponsorCampaignSponsoredAdapter(getContext(), new ArrayList<>(),
                new SponsorCampaignSponsoredAdapter.OnCampaignClickListener() {
                    @Override
                    public void onCampaignClick(Campaign campaign) {
                        openCampaignDetail(campaign);
                    }

                    @Override
                    public void onViewReportClick(Campaign campaign) {
                        openCampaignReport(campaign);
                    }
                });

        rvSponsoredCampaigns.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSponsoredCampaigns.setAdapter(sponsoredAdapter);
    }

    private void loadCampaignsNeedingSponsor() {
        Log.d(TAG, "Loading campaigns needing sponsor...");

        campaignRepository.getCampaignsNeedingSponsor().observe(getViewLifecycleOwner(), campaigns -> {
            if (campaigns != null && isAdded()) {
                Log.d(TAG, "Received " + campaigns.size() + " campaigns needing sponsor");

                if (campaigns.isEmpty()) {
                    rvNeedSponsorCampaigns.setVisibility(View.GONE);
                    if (tvEmptyNeedSponsor != null) {
                        tvEmptyNeedSponsor.setVisibility(View.VISIBLE);
                    }
                } else {
                    rvNeedSponsorCampaigns.setVisibility(View.VISIBLE);
                    if (tvEmptyNeedSponsor != null) {
                        tvEmptyNeedSponsor.setVisibility(View.GONE);
                    }
                    needSponsorAdapter.updateData(campaigns);
                }
            } else {
                Log.e(TAG, "Campaigns is null or fragment not added");
            }
        });
    }

    private void loadSponsoredCampaigns() {
        Log.d(TAG, "Loading sponsored campaigns...");

        // Lấy campaigns mà sponsor hiện tại đã tài trợ
        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getSponsorId() != null) {
                    campaignRepository.getCampaignsBySponsor(user.getSponsorId()).observe(getViewLifecycleOwner(), campaigns -> {
                        if (campaigns != null && isAdded()) {
                            Log.d(TAG, "Received " + campaigns.size() + " sponsored campaigns");

                            if (campaigns.isEmpty()) {
                                rvSponsoredCampaigns.setVisibility(View.GONE);
                                if (tvEmptySponsored != null) {
                                    tvEmptySponsored.setVisibility(View.VISIBLE);
                                }
                            } else {
                                rvSponsoredCampaigns.setVisibility(View.VISIBLE);
                                if (tvEmptySponsored != null) {
                                    tvEmptySponsored.setVisibility(View.GONE);
                                }
                                sponsoredAdapter.updateData(campaigns);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading user data: " + error);
            }
        });
    }

    private void openDonationForm(Campaign campaign) {
        Log.d(TAG, "Opening donation form for campaign: " + campaign.getName());
        
        // TODO: Implement donation flow
        // For now, just show a simple message
        if (getContext() != null) {
            Toast.makeText(getContext(), "Tính năng tài trợ sẽ được cập nhật sớm", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCampaignDetail(Campaign campaign) {
        Log.d(TAG, "Opening campaign detail for: " + campaign.getName());

        Intent intent = new Intent(getActivity(), activity_sponsor_campaign_detail.class);
        intent.putExtra("campaign_id", campaign.getId());
        intent.putExtra("campaign_name", campaign.getName());
        intent.putExtra("campaign_description", campaign.getDescription());
        intent.putExtra("campaign_location", campaign.getLocation());
        intent.putExtra("campaign_start_date", campaign.getStartDate());
        intent.putExtra("campaign_end_date", campaign.getEndDate());
        intent.putExtra("campaign_target_budget", campaign.getTargetBudget());
        intent.putExtra("campaign_current_budget", campaign.getCurrentBudget());
        intent.putExtra("campaign_image_url", campaign.getImageUrl());
        intent.putExtra("campaign_organization_name", campaign.getOrganizationName());
        intent.putExtra("campaign_category", campaign.getCategory());
        intent.putExtra("campaign_status", campaign.getStatus());
        startActivity(intent);
    }

    private void openCampaignReport(Campaign campaign) {
        Log.d(TAG, "Opening campaign report for: " + campaign.getName());

        Intent intent = new Intent(getActivity(), activity_sponsor_report_view.class);
        intent.putExtra("campaign_id", campaign.getId());
        intent.putExtra("campaign_name", campaign.getName());
        startActivity(intent);
    }

    private void loadSponsorData() {
        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if(user != null && isAdded()){
                    tvSponsorName.setText(user.getFullName());
                    tvTotalMoney.setText(formatMoney(user.getTotalDonations()));
                    tvTotalCampaigns.setText(String.valueOf(user.getTotalCampaigns()));

                    // Load avatar/logo nếu có
                    String avatarUrl = user.getAvatar();
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(frm_sponsor_home.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.logo_d_japan)
                                .error(R.drawable.logo_d_japan)
                                .circleCrop()
                                .into(ivSponsorLogo);
                    } else {
                        ivSponsorLogo.setImageResource(R.drawable.logo_d_japan);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải thông tin: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String formatMoney(int amount) {
        if (amount >= 1000000) {
            return (amount / 1000000) + "M";
        } else if (amount >= 1000) {
            return (amount / 1000) + "K";
        } else {
            return String.valueOf(amount);
        }
    }
}