package com.example.little_share.ui.sponsor;

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

import java.util.ArrayList;

public class frm_sponsor_home extends Fragment {

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
        userRepository = new UserRepository();
        campaignRepository = new CampaignRepository();

        initViews(view);
        loadSponsorData();
        setupRecyclerViews();
        loadCampaignsNeedingSponsor();
    }

    private void loadCampaignsNeedingSponsor() {
        campaignRepository.getCampaignsNeedingSponsor().observe(getViewLifecycleOwner(), campaigns -> {
            if (campaigns != null && isAdded()) {
                needSponsorAdapter.updateData(campaigns);
            }
        });
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
    }

    private void openDonationForm(Campaign campaign) {
    }

    private void openCampaignDetail(Campaign campaign) {
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


    private void initViews(View view) {
        ivSponsorLogo = view.findViewById(R.id.imageView3);
        tvSponsorName = view.findViewById(R.id.textView3);
        tvTotalMoney = view.findViewById(R.id.tvTotalMoney);
        tvTotalCampaigns = view.findViewById(R.id.tvTotalCampaigns);

        // RecyclerViews
        rvSponsoredCampaigns = view.findViewById(R.id.rvSponsoredCampaigns);

    }


}

