package com.example.little_share.ui.sponsor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.User;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.data.repositories.UserRepository;
import com.example.little_share.ui.sponsor.adapter.SponsorCampaignNeedAdapter;
import com.example.little_share.ui.sponsor.adapter.SponsorCampaignSponsoredAdapter;

import java.util.ArrayList;

public class frm_sponsor_home extends Fragment {
    private static final String TAG = "frm_sponsor_home";

    private SponsorCampaignSponsoredAdapter sponsoredCampaignAdapter;
    private SponsorCampaignNeedAdapter needSponsorAdapter;

    private ImageView ivSponsorLogo;
    private TextView tvSponsorName, tvTotalMoney, tvTotalCampaigns;

    // RecyclerViews
    private RecyclerView rvSponsoredCampaigns, rvNeedSponsorCampaigns;
    private TextView tvEmptySponsored, tvEmptyNeedSponsor;

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

        // Load data
        loadSponsorData();
        loadCampaignsNeedingSponsor();
        loadSponsoredCampaigns();

        checkAndHandleRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "=== ON RESUME ===");
    }

    private void checkAndHandleRefresh() {
        if (getArguments() != null) {
            boolean shouldRefresh = getArguments().getBoolean("should_refresh", false);
            if (shouldRefresh) {
                Log.d(TAG, "Should refresh from arguments");
                getArguments().remove("should_refresh");
            }
        }
    }

    /**
     * Public method to refresh all data
     * Called from activity when returning from donation
     */
    public void refreshData() {
        Log.d(TAG, "=== REFRESHING ALL DATA ===");

        if (!isAdded()) {
            Log.e(TAG, "Fragment not added, cannot refresh");
            return;
        }

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

        // TextView empty state
        tvEmptySponsored = view.findViewById(R.id.tvEmptySponsored);
        tvEmptyNeedSponsor = view.findViewById(R.id.tvEmptyNeedSponsor);
    }

    private void setupRecyclerViews() {
        // Setup adapter cho campaigns cần sponsor (chưa tài trợ)
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

        // THAY ĐỔI: Setup adapter cho campaigns ĐÃ tài trợ
        sponsoredCampaignAdapter = new SponsorCampaignSponsoredAdapter(getContext(), new ArrayList<>(),
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
        rvSponsoredCampaigns.setAdapter(sponsoredCampaignAdapter);
    }

    private void loadCampaignsNeedingSponsor() {
        Log.d(TAG, "Loading campaigns needing sponsor...");

        campaignRepository.getCampaignsNeedingSponsor().observe(getViewLifecycleOwner(), campaigns -> {
            if (!isAdded()) {
                Log.e(TAG, "Fragment not added");
                return;
            }

            if (campaigns != null) {
                Log.d(TAG, "Received " + campaigns.size() + " campaigns needing sponsor");
                for (Campaign c : campaigns) {
                    Log.d(TAG, "Campaign: " + c.getName() +
                            " | Status: " + c.getStatus() +
                            " | Budget: " + c.getCurrentBudget() + "/" + c.getTargetBudget() +
                            " | NeedsSponsor: " + c.isNeedsSponsor());
                }

                if (campaigns.isEmpty()) {
                    tvEmptyNeedSponsor.setVisibility(View.VISIBLE);
                    rvNeedSponsorCampaigns.setVisibility(View.GONE);
                } else {
                    tvEmptyNeedSponsor.setVisibility(View.GONE);
                    rvNeedSponsorCampaigns.setVisibility(View.VISIBLE);
                }

                needSponsorAdapter.updateData(campaigns);
            } else {
                Log.e(TAG, "Campaigns is null");
                tvEmptyNeedSponsor.setVisibility(View.VISIBLE);
                rvNeedSponsorCampaigns.setVisibility(View.GONE);
            }
        });
    }

    private void loadSponsoredCampaigns() {
        Log.d(TAG, "=== LOADING SPONSORED CAMPAIGNS ===");

        campaignRepository.getSponsoredCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            if (!isAdded()) {
                Log.e(TAG, "Fragment not added");
                return;
            }

            Log.d(TAG, "Received sponsored campaigns: " + (campaigns != null ? campaigns.size() : 0));

            if (campaigns != null && !campaigns.isEmpty()) {
                Log.d(TAG, "Updating adapter with " + campaigns.size() + " sponsored campaigns");

                // Log chi tiết từng campaign
                for (Campaign c : campaigns) {
                    Log.d(TAG, "Sponsored Campaign: " + c.getName() +
                            " | ID: " + c.getId() +
                            " | Status: " + c.getStatus());
                }

                // THAY ĐỔI: Sử dụng adapter đúng
                sponsoredCampaignAdapter.updateData(campaigns);
                tvEmptySponsored.setVisibility(View.GONE);
                rvSponsoredCampaigns.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "No sponsored campaigns, showing empty state");
                sponsoredCampaignAdapter.updateData(new ArrayList<>());
                tvEmptySponsored.setVisibility(View.VISIBLE);
                rvSponsoredCampaigns.setVisibility(View.GONE);
            }
        });
    }

    private void openCampaignReport(Campaign campaign) {
        if (isAdded()) {
            Intent intent = new Intent(getActivity(), activity_sponsor_report_view.class);
            intent.putExtra("campaign_id", campaign.getId());
            startActivity(intent);
        }
    }

    private void openDonationForm(Campaign campaign) {
        frm_sponsor_donation_form donationFragment = new frm_sponsor_donation_form();

        Bundle bundle = new Bundle();
        bundle.putString("campaign_id", campaign.getId());
        bundle.putString("campaign_name", campaign.getName());
        bundle.putSerializable("campaign_data", campaign);
        donationFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, donationFragment)
                .addToBackStack("donation_form")
                .commit();
    }

    private void openCampaignDetail(Campaign campaign) {
        if (isAdded()) {
            Log.d(TAG, "=== CAMPAIGN SELECTED ===");
            Log.d(TAG, "Name: " + campaign.getName());
            Log.d(TAG, "ID: " + campaign.getId());
            Log.d(TAG, "Organization: " + campaign.getOrganizationName());
            Log.d(TAG, "Location: " + campaign.getLocation());
            Log.d(TAG, "Description: " + campaign.getDescription());

            frm_campaign_detail_sponsor detailFragment = new frm_campaign_detail_sponsor();

            Bundle bundle = new Bundle();
            bundle.putString("campaign_id", campaign.getId());
            bundle.putString("campaign_name", campaign.getName());
            bundle.putSerializable("campaign_data", campaign);
            detailFragment.setArguments(bundle);

            if (getActivity() instanceof activity_sponsor_main) {
                Log.d(TAG, "Navigating to detail fragment");
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, detailFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Log.e(TAG, "Activity is not instance of activity_sponsor_main");
            }
        } else {
            Log.e(TAG, "Fragment is not added");
        }
    }

    private void loadSponsorData() {
        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null && isAdded()) {
                    tvSponsorName.setText(user.getFullName());
                    tvTotalMoney.setText(formatMoney(user.getTotalDonations()));
                    tvTotalCampaigns.setText(String.valueOf(user.getTotalCampaigns()));

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