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

        // Bind data
        holder.tvCampaignName.setText(campaign.getName());
        holder.tvOrganization.setText(campaign.getOrganizationName());
        holder.tvLocation.setText(campaign.getLocation());

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateRange = sdf.format(campaign.getStartDate()) + " - " + sdf.format(campaign.getEndDate());
        holder.tvDate.setText(dateRange);

        // Progress
        int progress = campaign.getBudgetProgressPercentage();
        holder.progressBar.setProgress(progress);
        holder.tvProgress.setText(progress + "%");

        // Load image
        if (campaign.getImageUrl() != null && !campaign.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(campaign.getImageUrl())
                    .placeholder(R.drawable.img_quyengop_dochoi)
                    .error(R.drawable.img_quyengop_dochoi)
                    .into(holder.imgCampaign);
        } else {
            holder.imgCampaign.setImageResource(R.drawable.img_quyengop_dochoi);
        }

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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCampaign;
        TextView tvCampaignName, tvOrganization, tvLocation, tvDate, tvProgress;
        ProgressBar progressBar;
        Button btnViewDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCampaign = itemView.findViewById(R.id.imgCampaign);
            tvCampaignName = itemView.findViewById(R.id.tvCampaignName);
            tvOrganization = itemView.findViewById(R.id.tvOrganization);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnViewDetail = itemView.findViewById(R.id.btnDetail);

        }
    }
}
