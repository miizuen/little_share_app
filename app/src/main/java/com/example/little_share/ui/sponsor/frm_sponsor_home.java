package com.example.little_share.ui.sponsor;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.little_share.ui.sponsor.adapter.SponsorCampaignNeedAdapter;
import com.example.little_share.ui.sponsor.adapter.SponsorshipHistoryAdapter;

import java.util.ArrayList;

public class frm_sponsor_home extends Fragment {

    private SponsorshipHistoryAdapter sponsorshipHistoryAdapter;
    private ImageView ivSponsorLogo;
    private TextView tvSponsorName, tvTotalMoney, tvTotalCampaigns;

    // RecyclerViews
    private RecyclerView rvSponsoredCampaigns, rvNeedSponsorCampaigns;
    private TextView tvEmptySponsored;
    private SponsorCampaignNeedAdapter needSponsorAdapter;

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

        // TextView empty state
        tvEmptySponsored = view.findViewById(R.id.tvEmptySponsored);
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

        // Setup adapter cho campaigns đang tài trợ
        sponsorshipHistoryAdapter = new SponsorshipHistoryAdapter(getContext(), new ArrayList<>(),
                new SponsorshipHistoryAdapter.OnSponsorshipClickListener() {
                    @Override
                    public void onViewDetailClick(Campaign campaign) {
                        openCampaignDetail(campaign);
                    }

                    @Override
                    public void onViewReportClick(Campaign campaign) {
                        openCampaignReport(campaign);
                    }
                });

        rvSponsoredCampaigns.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSponsoredCampaigns.setAdapter(sponsorshipHistoryAdapter);
    }

    private void loadCampaignsNeedingSponsor() {
        android.util.Log.d("frm_sponsor_home", "Loading campaigns needing sponsor...");

        campaignRepository.getCampaignsNeedingSponsor().observe(getViewLifecycleOwner(), campaigns -> {
            if (campaigns != null && isAdded()) {
                android.util.Log.d("frm_sponsor_home", "Received " + campaigns.size() + " campaigns");
                for (Campaign c : campaigns) {
                    android.util.Log.d("frm_sponsor_home", "Campaign: " + c.getName() +
                            " | Status: " + c.getStatus() +
                            " | Budget: " + c.getCurrentBudget() + "/" + c.getTargetBudget() +
                            " | NeedsSponsor: " + c.isNeedsSponsor());
                }
                needSponsorAdapter.updateData(campaigns);
            } else {
                android.util.Log.e("frm_sponsor_home", "Campaigns is null or fragment not added");
            }
        });
    }

    private void loadSponsoredCampaigns() {
        campaignRepository.getSponsoredCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            if (campaigns != null && isAdded()) {
                sponsorshipHistoryAdapter.updateData(campaigns);
                tvEmptySponsored.setVisibility(campaigns == null || campaigns.size() == 0 ? View.VISIBLE : View.GONE);
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
            // Log để debug
            android.util.Log.d("DEBUG", "=== CAMPAIGN SELECTED ===");
            android.util.Log.d("DEBUG", "Name: " + campaign.getName());
            android.util.Log.d("DEBUG", "ID: " + campaign.getId());
            android.util.Log.d("DEBUG", "Organization: " + campaign.getOrganizationName());
            android.util.Log.d("DEBUG", "Location: " + campaign.getLocation());
            android.util.Log.d("DEBUG", "Description: " + campaign.getDescription());

            // Tạo fragment chi tiết
            frm_campaign_detail_sponsor detailFragment = new frm_campaign_detail_sponsor();

            // Truyền dữ liệu qua Bundle
            Bundle bundle = new Bundle();
            bundle.putString("campaign_id", campaign.getId());
            bundle.putString("campaign_name", campaign.getName());
            bundle.putSerializable("campaign_data", campaign);
            detailFragment.setArguments(bundle);

            // Chuyển fragment sử dụng activity parent
            if (getActivity() instanceof activity_sponsor_main) {
                android.util.Log.d("DEBUG", "Navigating to detail fragment");
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, detailFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                android.util.Log.e("DEBUG", "Activity is not instance of activity_sponsor_main");
            }
        } else {
            android.util.Log.e("DEBUG", "Fragment is not added");
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
