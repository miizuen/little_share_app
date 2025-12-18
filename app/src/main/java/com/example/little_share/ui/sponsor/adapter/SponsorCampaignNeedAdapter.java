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

public class SponsorCampaignNeedAdapter extends RecyclerView.Adapter<SponsorCampaignNeedAdapter.ViewHolder> {
    private static final String TAG = "SponsorCampaignAdapter";
    private Context context;
    private List<Campaign> campaigns;
    private OnCampaignClickListener listener;

    public interface OnCampaignClickListener {
        void onDonateClick(Campaign campaign);
        void onCampaignClick(Campaign campaign);
    }

    public SponsorCampaignNeedAdapter(Context context, List<Campaign> campaigns, OnCampaignClickListener listener) {
        this.context = context;
        this.campaigns = campaigns;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SponsorCampaignNeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_campaign_need_sponsor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SponsorCampaignNeedAdapter.ViewHolder holder, int position) {
        Campaign campaign = campaigns.get(position);

        Log.d(TAG, "Binding campaign: " + campaign.getName());

        // Tên chiến dịch
        holder.tvCampaignName.setText(campaign.getName());

        // Tổ chức
        String orgName = campaign.getOrganizationName();
        holder.tvGroup.setText(orgName != null ? orgName : "Chưa có thông tin");

        // Địa điểm
        String location = campaign.getLocation();
        holder.tvLocation.setText(location != null ? location : "Chưa xác định");

        // Category
        holder.tvCategory.setText(getCategoryDisplayName(campaign.getCategory()));

        // Ngày tháng - FIX: Xử lý Date đúng cách
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date startDate = campaign.getStartDate();
            Date endDate = campaign.getEndDate();

            String startStr = startDate != null ? sdf.format(startDate) : "N/A";
            String endStr = endDate != null ? sdf.format(endDate) : "N/A";
            String dateRange = startStr + " - " + endStr;
            holder.tvDate.setText(dateRange);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage());
            holder.tvDate.setText("Chưa xác định");
        }

        // Progress bar
        int progress = campaign.getBudgetProgressPercentage();
        holder.progressBar.setProgress(Math.min(progress, 100));

        // Hiển thị tiến độ ngân sách
        double current = campaign.getCurrentBudget();
        double target = campaign.getTargetBudget();
        String progressText = formatMoney(current) + " / " + formatMoney(target);
        holder.tvProgress.setText(progressText);

        // Load image
        String imageUrl = campaign.getImageUrl();
        if(imageUrl != null && !imageUrl.isEmpty()){
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_quyengop_dochoi)
                    .error(R.drawable.img_quyengop_dochoi)
                    .into(holder.imgCampaign);
        } else {
            holder.imgCampaign.setImageResource(R.drawable.img_quyengop_dochoi);
        }

        // Click listeners
        holder.btnDonate.setOnClickListener(v -> {
            android.util.Log.d("ADAPTER", "=== DONATE BUTTON CLICKED ===");
            android.util.Log.d("ADAPTER", "Campaign: " + campaign.getName());
            if(listener != null){
                android.util.Log.d("ADAPTER", "Calling onDonateClick");
                listener.onDonateClick(campaign);
            } else {
                android.util.Log.e("ADAPTER", "Listener is NULL!");
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onCampaignClick(campaign);
            }
        });
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

    @Override
    public int getItemCount() {
        return campaigns != null ? campaigns.size() : 0;
    }

    public void updateData(List<Campaign> newCampaigns) {
        Log.d(TAG, "Updating adapter with " + (newCampaigns != null ? newCampaigns.size() : 0) + " campaigns");
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCampaign;
        TextView tvCategory, tvCampaignName, tvGroup, tvLocation, tvDate, tvProgress;
        ProgressBar progressBar;
        Button btnDonate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCampaign = itemView.findViewById(R.id.imgCampaign);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCampaignName = itemView.findViewById(R.id.tvCampaignName);
            tvGroup = itemView.findViewById(R.id.tvGroup);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnDonate = itemView.findViewById(R.id.btnDonate);
        }
    }
}