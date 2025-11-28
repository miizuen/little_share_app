package com.example.little_share.ui.volunteer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.CampaignViewHolder> {

    private Context context;
    private List<Campaign> campaignList;
    private OnCampaignClickListener listener;

    public interface OnCampaignClickListener {
        void onCampaignClick(Campaign campaign);
        void onDetailClick(Campaign campaign);
    }

    public CampaignAdapter(Context context, List<Campaign> campaignList, OnCampaignClickListener listener) {
        this.context = context;
        this.campaignList = campaignList != null ? campaignList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CampaignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_campaign_card, parent, false);
        return new CampaignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignViewHolder holder, int position) {
        Campaign campaign = campaignList.get(position);
        holder.bind(campaign, listener);
    }

    @Override
    public int getItemCount() {
        return campaignList.size();
    }

    public void updateData(List<Campaign> newList) {
        this.campaignList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class CampaignViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCampaign;
        TextView tvCategory, tvPoints, tvCampaignName;
        TextView tvOrganization, tvLocation, tvDate;
        TextView tvProgressNumber;
        ProgressBar progressBar;
        Button btnDetail;

        public CampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCampaign = itemView.findViewById(R.id.imgCampaign);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvCampaignName = itemView.findViewById(R.id.tvCampaignName);
            tvOrganization = itemView.findViewById(R.id.tvOrganization);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvProgressNumber = itemView.findViewById(R.id.tvProgressNumber);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }

        public void bind(Campaign campaign, OnCampaignClickListener listener) {
            // Set name
            tvCampaignName.setText(campaign.getName());

            // Set category
            try {
                Campaign.CampaignCategory category = campaign.getCategoryEnum();
                tvCategory.setText(category.getDisplayName());
                setCategoryColor(category);
            } catch (Exception e) {
                tvCategory.setText(campaign.getCategory());
            }

            // Set organization
            tvOrganization.setText(campaign.getOrganizationName());

            // Set location
            tvLocation.setText(campaign.getLocation());

            // Set date range
            if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateRange = sdf.format(campaign.getStartDate()) + " - " +
                        sdf.format(campaign.getEndDate());
                tvDate.setText(dateRange);
            }

            // Set points
            tvPoints.setText("+" + campaign.getPointsReward());

            // Set progress
            int progress = campaign.getProgressPercentage();
            tvProgressNumber.setText(progress + "%");
            progressBar.setProgress(progress);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCampaignClick(campaign);
            });

            btnDetail.setOnClickListener(v -> {
                if (listener != null) listener.onDetailClick(campaign);
            });
        }

        private void setCategoryColor(Campaign.CampaignCategory category) {
            int color;
            switch (category) {
                case FOOD:
                    color = 0xFFFF6F00; // Orange
                    break;
                case EDUCATION:
                    color = 0xFF1976D2; // Blue
                    break;
                case ENVIRONMENT:
                    color = 0xFF388E3C; // Green
                    break;
                case HEALTH:
                    color = 0xFFD32F2F; // Red
                    break;
                case URGENT:
                    color = 0xFFF44336; // Bright Red
                    break;
                default:
                    color = 0xFF757575; // Grey
            }
            tvCategory.setTextColor(color);
        }
    }
}