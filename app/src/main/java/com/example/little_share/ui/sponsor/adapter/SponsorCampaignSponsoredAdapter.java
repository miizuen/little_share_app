package com.example.little_share.ui.sponsor.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.List;
import java.util.Locale;

// Import Campaign class
import com.example.little_share.data.models.Campain.Campaign;

public class SponsorCampaignSponsoredAdapter extends RecyclerView.Adapter<SponsorCampaignSponsoredAdapter.ViewHolder> {
    private static final String TAG = "SponsoredCampaignAdapter";
    private Context context;
    private List<Campaign> campaigns;
    private OnCampaignClickListener listener;

    public interface OnCampaignClickListener {
        void onCampaignClick(Campaign campaign);
        void onViewReportClick(Campaign campaign);
    }

    public SponsorCampaignSponsoredAdapter(Context context, List<Campaign> campaigns, OnCampaignClickListener listener) {
        this.context = context;
        this.campaigns = campaigns;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_campaign_sponsored, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (campaigns == null || position >= campaigns.size()) {
            return;
        }

        Campaign campaign = campaigns.get(position);
        if (campaign == null) {
            return;
        }

        Log.d(TAG, "Binding sponsored campaign: " + campaign.getName());

        // Tên chiến dịch
        if (holder.tvCampaignName != null) {
            holder.tvCampaignName.setText(campaign.getName() != null ? campaign.getName() : "Chưa có tên");
        }

        // Tổ chức
        if (holder.tvGroup != null) {
            String orgName = campaign.getOrganizationName();
            holder.tvGroup.setText(orgName != null ? orgName : "Chưa có thông tin");
        }

        // Địa điểm
        if (holder.tvLocation != null) {
            String location = campaign.getLocation();
            holder.tvLocation.setText(location != null ? location : "Chưa xác định");
        }

        // Category
        if (holder.tvCategory != null) {
            holder.tvCategory.setText(getCategoryDisplayName(campaign.getCategory()));
        }

        // Trạng thái
        if (holder.tvStatus != null) {
            holder.tvStatus.setText(getStatusDisplayName(campaign.getStatus()));
        }

        // Ngày tháng
        if (holder.tvDate != null) {
            displayDateRange(holder.tvDate, campaign.getStartDate(), campaign.getEndDate());
        }

        // Progress bar
        if (holder.progressBar != null) {
            int progress = campaign.getBudgetProgressPercentage();
            holder.progressBar.setProgress(Math.min(progress, 100));
        }

        // Hiển thị tiến độ ngân sách
        if (holder.tvProgress != null) {
            double current = campaign.getCurrentBudget();
            double target = campaign.getTargetBudget();
            String progressText = formatMoney(current) + " / " + formatMoney(target);
            holder.tvProgress.setText(progressText);
        }

        // Load image
        if (holder.imgCampaign != null) {
            loadCampaignImage(holder.imgCampaign, campaign.getImageUrl());
        }

        // Hiển thị nút phù hợp theo trạng thái
        updateButtonVisibility(holder, campaign.getStatus());

        // Click listeners
        setupClickListeners(holder, campaign);
    }

    private void displayDateRange(TextView tvDate, Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            String startStr = startDate != null ? sdf.format(startDate) : "N/A";
            String endStr = endDate != null ? sdf.format(endDate) : "N/A";
            String dateRange = startStr + " - " + endStr;
            tvDate.setText(dateRange);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage());
            tvDate.setText("Chưa xác định");
        }
    }

    private void loadCampaignImage(ImageView imgCampaign, String imageUrl) {
        if (context == null) return;
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_quyengop_dochoi)
                    .error(R.drawable.img_quyengop_dochoi)
                    .into(imgCampaign);
        } else {
            imgCampaign.setImageResource(R.drawable.img_quyengop_dochoi);
        }
    }

    private void setupClickListeners(ViewHolder holder, Campaign campaign) {
        if (holder.btnViewReport != null) {
            holder.btnViewReport.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewReportClick(campaign);
                }
            });
        }

        if (holder.itemView != null) {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCampaignClick(campaign);
                }
            });
        }
    }

    private void updateButtonVisibility(ViewHolder holder, String status) {
        if (holder.btnViewReport == null) return;
        
        if (status != null && (status.equals("COMPLETED") || status.equals("FINISHED"))) {
            holder.btnViewReport.setVisibility(View.VISIBLE);
            holder.btnViewReport.setText("Xem báo cáo");
        } else {
            holder.btnViewReport.setVisibility(View.VISIBLE);
            holder.btnViewReport.setText("Chi tiết");
        }
    }

    private String getCategoryDisplayName(String category) {
        if (category == null) return "Khác";

        try {
            Campaign.CampaignCategory categoryEnum = Campaign.CampaignCategory.valueOf(category);
            return categoryEnum.getDisplayName();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing category: " + category);
            return "Khác";
        }
    }

    private String getStatusDisplayName(String status) {
        if (status == null) return "Chưa xác định";

        switch (status) {
            case "ACTIVE":
                return "Đang hoạt động";
            case "ONGOING":
                return "Đang diễn ra";
            case "COMPLETED":
                return "Hoàn thành";
            case "FINISHED":
                return "Kết thúc";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }

    @Override
    public int getItemCount() {
        return campaigns != null ? campaigns.size() : 0;
    }

    public void updateData(List<Campaign> newCampaigns) {
        Log.d(TAG, "Updating adapter with " + (newCampaigns != null ? newCampaigns.size() : 0) + " sponsored campaigns");
        this.campaigns = newCampaigns;
        notifyDataSetChanged();
    }

    private String formatMoney(double amount) {
        if (amount >= 1000000) {
            return String.format(Locale.getDefault(), "%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format(Locale.getDefault(), "%.0fK", amount / 1000);
        } else {
            return String.format(Locale.getDefault(), "%.0f", amount);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCampaign;
        TextView tvCategory, tvCampaignName, tvGroup, tvLocation, tvDate, tvProgress, tvStatus;
        ProgressBar progressBar;
        Button btnViewReport;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews(itemView);
        }

        private void initViews(View itemView) {
            imgCampaign = itemView.findViewById(R.id.imgCampaign);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCampaignName = itemView.findViewById(R.id.tvCampaignName);
            tvGroup = itemView.findViewById(R.id.tvGroup);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnViewReport = itemView.findViewById(R.id.btnViewReport);
        }
    }
}