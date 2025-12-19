package com.example.little_share.ui.sponsor.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.little_share.ui.sponsor.activity_sponsor_campaign_detail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SponsorshipHistoryAdapter extends RecyclerView.Adapter<SponsorshipHistoryAdapter.ViewHolder> {
    private Context context;
    private List<Campaign> sponsoredCampaigns;
    private OnSponsorshipClickListener listener;

    public interface OnSponsorshipClickListener {
        void onViewDetailClick(Campaign campaign);
        void onViewReportClick(Campaign campaign);
    }

    public SponsorshipHistoryAdapter(Context context, List<Campaign> sponsoredCampaigns, OnSponsorshipClickListener listener) {
        this.context = context;
        this.sponsoredCampaigns = sponsoredCampaigns;
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
        Campaign campaign = sponsoredCampaigns.get(position);

        // Campaign name
        holder.tvCampaignName.setText(campaign.getName());
        
        // Organization
        holder.tvOrganization.setText(campaign.getOrganizationName());
        
        // Location
        holder.tvLocation.setText(campaign.getLocation());

        // Category
        String category = getCategoryDisplayName(campaign.getCategory());
        holder.tvCategory.setText(category);
        
        // Status
        String status = getStatusDisplayName(campaign.getStatus());
        holder.tvStatus.setText(status);
        holder.tvStatus.setBackgroundResource(getStatusBackground(campaign.getStatus()));

        // Format date
        if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateRange = sdf.format(campaign.getStartDate()) + " - " + sdf.format(campaign.getEndDate());
            holder.tvDate.setText(dateRange);
        }

        // Progress
        int progress = campaign.getBudgetProgressPercentage();
        holder.progressBar.setProgress(progress);
        
        // Progress text with money format
        String progressText = formatMoney(campaign.getCurrentBudget()) + " / " + formatMoney(campaign.getTargetBudget());
        holder.tvProgress.setText(progressText);

        // Load image
        loadCampaignImage(holder.imgCampaign, campaign);

        // Click listeners
        holder.btnViewDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetailClick(campaign);
            }
        });


        // Card click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetailClick(campaign);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sponsoredCampaigns != null ? sponsoredCampaigns.size() : 0;
    }

    public void updateData(List<Campaign> newCampaigns) {
        this.sponsoredCampaigns = newCampaigns;
        notifyDataSetChanged();
    }

    private String getCategoryDisplayName(String category) {
        if (category == null) return "Khác";
        
        switch (category.toUpperCase()) {
            case "FOOD": return "Thực phẩm";
            case "EDUCATION": return "Giáo dục";
            case "HEALTH": return "Y tế";
            case "ENVIRONMENT": return "Môi trường";
            case "URGENT": return "Khẩn cấp";
            default: return category;
        }
    }

    private String getStatusDisplayName(String status) {
        if (status == null) return "Không rõ";
        
        switch (status.toUpperCase()) {
            case "ONGOING": return "Đang diễn ra";
            case "UPCOMING": return "Sắp diễn ra";
            case "COMPLETED": return "Hoàn thành";
            case "PENDING": return "Chờ duyệt";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }

    private int getStatusBackground(String status) {
        if (status == null) return R.drawable.bg_status_badge;
        
        switch (status.toUpperCase()) {
            case "ONGOING": return R.drawable.bg_status_active;
            case "UPCOMING": return R.drawable.bg_status_pending;
            case "COMPLETED": return R.drawable.bg_status_completed;
            case "CANCELLED": return R.drawable.bg_status_cancelled;
            default: return R.drawable.bg_status_badge;
        }
    }

    private void loadCampaignImage(ImageView imageView, Campaign campaign) {
        if (campaign.getImageUrl() != null && !campaign.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(campaign.getImageUrl())
                    .placeholder(R.drawable.img_quyengop_dochoi)
                    .error(R.drawable.img_quyengop_dochoi)
                    .centerCrop()
                    .into(imageView);
        } else {
            int defaultImage = getDefaultImageForCategory(campaign.getCategory());
            imageView.setImageResource(defaultImage);
        }
    }

    private int getDefaultImageForCategory(String category) {
        if (category == null) return R.drawable.img_quyengop_dochoi;
        
        switch (category.toUpperCase()) {
            case "FOOD": return R.drawable.img_nauanchoem;
            case "EDUCATION":
            case "HEALTH":
            case "ENVIRONMENT":
            case "URGENT":
            default: return R.drawable.img_quyengop_dochoi;
        }
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCampaign;
        TextView tvCampaignName, tvOrganization, tvLocation, tvDate, tvProgress;
        TextView tvCategory, tvStatus;
        ProgressBar progressBar;
        Button btnViewDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCampaign = itemView.findViewById(R.id.imgCampaign);
            tvCampaignName = itemView.findViewById(R.id.tvCampaignName);
            tvOrganization = itemView.findViewById(R.id.tvGroup);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnViewDetail = itemView.findViewById(R.id.btnViewReport);
        }
    }
}
