package com.example.little_share.ui.sponsor;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.ui.common.ImageViewerDialog;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class frm_campaign_detail_sponsor extends Fragment {

    private ImageView imgBanner;
    private TextView tvCampaignName, tvOrganization, tvLocation, tvDescription;
    private TextView tvSponsorAmount, tvBeneficiaries, tvDuration, tvLocation2, tvActivity;
    private TextView tvProgressPercent;
    private ProgressBar progressBarBudget;
    private Campaign currentCampaign;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_campaign_detail_sponsor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        Bundle args = getArguments();
        if (args != null) {
            Campaign campaign = (Campaign) args.getSerializable("campaign_data");
            if (campaign != null) {
                android.util.Log.d("CAMPAIGN_DETAIL", "=== RECEIVED CAMPAIGN ===");
                android.util.Log.d("CAMPAIGN_DETAIL", "Name: " + campaign.getName());
                android.util.Log.d("CAMPAIGN_DETAIL", "Organization: " + campaign.getOrganizationName());

                displayCampaignData(campaign);
            } else {
                android.util.Log.e("CAMPAIGN_DETAIL", "Campaign data is NULL!");
                String campaignId = args.getString("campaign_id");
                if (campaignId != null) {
                    loadCampaignById(campaignId);
                }
            }
        } else {
            android.util.Log.e("CAMPAIGN_DETAIL", "Arguments is NULL!");
        }
    }

    private void initViews(View view) {
        imgBanner = view.findViewById(R.id.imgBanner);
        tvCampaignName = view.findViewById(R.id.tvCampaignName);
        tvOrganization = view.findViewById(R.id.tvOrganization);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvSponsorAmount = view.findViewById(R.id.tvSponsorAmount);
        tvBeneficiaries = view.findViewById(R.id.tvBeneficiaries);
        tvDuration = view.findViewById(R.id.tvDuration);
        tvLocation2 = view.findViewById(R.id.tvLocation2);
        tvActivity = view.findViewById(R.id.tvActivity);

        // THÊM 2 DÒNG NÀY
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        progressBarBudget = view.findViewById(R.id.progressBarBudget);

        // Click listener để xem ảnh phóng to
        imgBanner.setOnClickListener(v -> {
            if (currentCampaign != null) {
                String imageUrl = currentCampaign.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    ImageViewerDialog dialog = new ImageViewerDialog(getContext(), imageUrl);
                    dialog.show();
                }
            }
        });
    }

    private void loadCampaignById(String campaignId) {
        CampaignRepository campaignRepository = new CampaignRepository();
        campaignRepository.getCampaignById(campaignId).observe(getViewLifecycleOwner(), campaign -> {
            if (campaign != null) {
                displayCampaignData(campaign);
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không tìm thấy chiến dịch", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayCampaignData(Campaign campaign) {
        android.util.Log.d("CAMPAIGN_DETAIL", "=== SETTING UI DATA ===");

        // Lưu campaign hiện tại
        this.currentCampaign = campaign;

        // FORCE SET campaign name - quan trọng nhất
        tvCampaignName.setText(campaign.getName());
        android.util.Log.d("CAMPAIGN_DETAIL", "Set campaign name: " + campaign.getName());

        // Set organization
        if (campaign.getOrganizationName() != null) {
            tvOrganization.setText(campaign.getOrganizationName());
            tvLocation.setText(campaign.getOrganizationName());
        }

        // Set location
        if (campaign.getLocation() != null) {
            tvLocation2.setText(campaign.getLocation());
        }

        // Set description
        if (campaign.getDescription() != null) {
            tvDescription.setText(campaign.getDescription());
        }

        // Format date
        if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateRange = sdf.format(campaign.getStartDate()) + " - " + sdf.format(campaign.getEndDate());
            tvDuration.setText(dateRange);
        }

        // Set amounts
        tvSponsorAmount.setText(formatMoney(campaign.getCurrentBudget()));
        tvBeneficiaries.setText(String.valueOf(campaign.getMaxVolunteers()));

        // Set activity
        tvActivity.setText(campaign.getActivities() != null ? campaign.getActivities() : "Chưa có thông tin");

        // ===== CẬP NHẬT PROGRESS BAR =====
        int progressPercentage = campaign.getBudgetProgressPercentage();
        android.util.Log.d("CAMPAIGN_DETAIL", "Progress: " + progressPercentage + "%");
        android.util.Log.d("CAMPAIGN_DETAIL", "Current Budget: " + campaign.getCurrentBudget());
        android.util.Log.d("CAMPAIGN_DETAIL", "Target Budget: " + campaign.getTargetBudget());

        // Cập nhật ProgressBar
        if (progressBarBudget != null) {
            progressBarBudget.setProgress(progressPercentage);
            android.util.Log.d("CAMPAIGN_DETAIL", "ProgressBar updated to: " + progressPercentage);
        } else {
            android.util.Log.e("CAMPAIGN_DETAIL", "ProgressBar is NULL!");
        }

        // Cập nhật TextView phần trăm
        if (tvProgressPercent != null) {
            tvProgressPercent.setText(progressPercentage + "%");
            android.util.Log.d("CAMPAIGN_DETAIL", "Progress text updated to: " + progressPercentage + "%");
        } else {
            android.util.Log.e("CAMPAIGN_DETAIL", "tvProgressPercent is NULL!");
        }

        // Load image
        loadCampaignImage(campaign);

        android.util.Log.d("CAMPAIGN_DETAIL", "UI updated successfully");
    }

    private void loadCampaignImage(Campaign campaign) {
        android.util.Log.d("CAMPAIGN_DETAIL", "=== LOADING IMAGE - SAME AS ADAPTER ===");
        android.util.Log.d("CAMPAIGN_DETAIL", "Campaign: " + campaign.getName());
        android.util.Log.d("CAMPAIGN_DETAIL", "ImageURL: " + (campaign.getImageUrl() != null ? campaign.getImageUrl() : "NULL"));

        // SỬ DỤNG LOGIC GIỐNG HỆT ADAPTER SponsorCampaignNeedAdapter
        String imageUrl = campaign.getImageUrl();
        if(imageUrl != null && !imageUrl.isEmpty()){
            android.util.Log.d("CAMPAIGN_DETAIL", "Loading from URL (same as adapter): " + imageUrl);

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_quyengop_dochoi)
                    .error(R.drawable.img_quyengop_dochoi)
                    .into(imgBanner);
        } else {
            android.util.Log.d("CAMPAIGN_DETAIL", "No URL, using default (same as adapter)");
            imgBanner.setImageResource(R.drawable.img_quyengop_dochoi);
        }
    }

    private int getDefaultImageForCategory(String category) {
        android.util.Log.d("CAMPAIGN_DETAIL", "Getting default image for category: " + category);

        if (category != null) {
            switch (category.toUpperCase()) {
                case "FOOD":
                    return R.drawable.img_nauanchoem;
                case "ENVIRONMENT":
                case "EDUCATION":
                case "HEALTH":
                case "URGENT":
                default:
                    return R.drawable.img_quyengop_dochoi;
            }
        }

        return R.drawable.img_quyengop_dochoi;
    }

    private String formatMoney(double amount) {
        if (amount >= 1000000) {
            return String.format(Locale.getDefault(), "%.1fM VNĐ", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format(Locale.getDefault(), "%.0fK VNĐ", amount / 1000);
        } else {
            return String.format(Locale.getDefault(), "%.0f VNĐ", amount);
        }
    }
}
