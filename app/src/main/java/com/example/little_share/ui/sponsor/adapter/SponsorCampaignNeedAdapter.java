package com.example.little_share.ui.sponsor.adapter;

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

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.adapter.CampaignHistoryAdapter;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SponsorCampaignNeedAdapter extends RecyclerView.Adapter<SponsorCampaignNeedAdapter.ViewHolder> {
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

        holder.tvCampaignName.setText(campaign.getName());
        holder.tvGroup.setText(campaign.getOrganizationName());
        holder.tvLocation.setText(campaign.getLocation());

        holder.tvCategory.setText(getCategoryDisplayName(campaign.getCategory()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateRange = sdf.format(campaign.getStartDate() + " - "+ sdf.format(campaign.getEndDate()));
        holder.tvDate.setText(dateRange);

        int progress = campaign.getBudgetProgressPercentage();
        holder.progressBar.setProgress(progress);

        if(campaign.getImageUrl() != null && !campaign.getImageUrl().isEmpty()){
            Glide.with(context)
                    .load(campaign.getImageUrl())
                    .placeholder(R.drawable.img_quyengop_dochoi)
                    .error(R.drawable.img_quyengop_dochoi)
                    .into(holder.imgCampaign);
        }else{
            holder.imgCampaign.setImageResource(R.drawable.img_quyengop_dochoi);
        }

        holder.btnDonate.setOnClickListener(v -> {
            if(listener != null){
                listener.onDonateClick(campaign);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onCampaignClick(campaign);
            }
        });


    }

    private String getCategoryDisplayName(String category) {
        try {
            Campaign.CampaignCategory categoryEnum = Campaign.CampaignCategory.valueOf(category);
            return categoryEnum.getDisplayName();
        } catch (Exception e) {
            return "Khác";
        }
    }

    @Override
    public int getItemCount() {
        return campaigns.size();
    }

    public void updateData(List<Campaign> newCampaigns) {
        this.campaigns = newCampaigns;
        notifyDataSetChanged();
    }

    private String formatMoney(double amount) {
        if (amount >= 1000000) {
            return String.format("%.0fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.0fK", amount / 1000);
        } else {
            return String.format("%.0f", amount);
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
