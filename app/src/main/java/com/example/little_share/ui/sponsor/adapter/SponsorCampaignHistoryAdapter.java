package com.example.little_share.ui.sponsor.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SponsorCampaignHistoryAdapter extends RecyclerView.Adapter<SponsorCampaignHistoryAdapter.ViewHolder> {
    private Context context;
    private List<Campaign> campaigns;
    private OnCampaignHistoryClickListener listener;

    public interface OnCampaignHistoryClickListener {
        void onCampaignClick(Campaign campaign);
        void onDetailClick(Campaign campaign);
    }

    public SponsorCampaignHistoryAdapter(Context context, List<Campaign> campaigns, OnCampaignHistoryClickListener listener) {
        this.context = context;
        this.campaigns = campaigns;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sponsor_campaign_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Campaign campaign = campaigns.get(position);

        // Campaign name
        holder.tvCampaignName.setText(campaign.getName());

        // Location
        holder.tvLocation.setText(campaign.getLocation());

        // Date range
        if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String dateRange = sdf.format(campaign.getStartDate()) + " - " + sdf.format(campaign.getEndDate());
            holder.tvDate.setText(dateRange);
        }

        // Get actual donation amount from SponsorDonation
        CampaignRepository repository = new CampaignRepository();
        repository.getTotalDonationForCampaign(campaign.getId(), amount -> {
            String donationText = "Đã đóng góp: " + formatMoney(amount) + " VNĐ";
            holder.tvDonation.setText(donationText);
        });

        // Button text changed to "Xem báo cáo"
        holder.btnDetail.setText("Xem báo cáo");
        holder.btnDetail.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3)); // Blue color

        // Load campaign image
        loadCampaignImage(holder.imgCampaign, campaign);

        // Click listeners
        holder.btnDetail.setOnClickListener(v -> {
            Log.d("HistoryAdapter", "=== DETAIL BUTTON CLICKED ===");
            Log.d("HistoryAdapter", "Campaign: " + campaign.getName());
            if (listener != null) {
                Log.d("HistoryAdapter", "Calling listener.onDetailClick()");
                listener.onDetailClick(campaign);
            } else {
                Log.e("HistoryAdapter", "Listener is NULL!");
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Log.d("HistoryAdapter", "=== CARD CLICKED ===");
            Log.d("HistoryAdapter", "Campaign: " + campaign.getName());
            if (listener != null) {
                Log.d("HistoryAdapter", "Calling listener.onCampaignClick()");
                listener.onCampaignClick(campaign);
            } else {
                Log.e("HistoryAdapter", "Listener is NULL!");
            }
        });
    }

    @Override
    public int getItemCount() {
        return campaigns != null ? campaigns.size() : 0;
    }

    public void updateData(List<Campaign> newCampaigns) {
        this.campaigns = newCampaigns;
        notifyDataSetChanged();
    }

    private void loadCampaignImage(ImageView imageView, Campaign campaign) {
        // Load specific image based on campaign name first
        int specificImage = getSpecificImageForCampaign(campaign);
        if (specificImage != -1) {
            imageView.setImageResource(specificImage);
            return;
        }

        // Load from URL if available
        if (campaign.getImageUrl() != null && !campaign.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(campaign.getImageUrl())
                    .placeholder(getDefaultImageForCategory(campaign.getCategory()))
                    .error(getDefaultImageForCategory(campaign.getCategory()))
                    .centerCrop()
                    .into(imageView);
        } else {
            // Use default image based on category
            int defaultImage = getDefaultImageForCategory(campaign.getCategory());
            imageView.setImageResource(defaultImage);
        }
    }

    private int getSpecificImageForCampaign(Campaign campaign) {
        String campaignName = campaign.getName().toLowerCase().trim();

        if (campaignName.equals("nấu ăn cho em")) {
            return R.drawable.img_nauanchoem;
        } else if (campaignName.contains("bữa cơm")) {
            return R.drawable.logo_buacomnghiatinh;
        }

        return -1; // No specific image found
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
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCampaign;
        TextView tvCampaignName, tvLocation, tvDate, tvDonation;
        Button btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCampaign = itemView.findViewById(R.id.imgCampaign);
            tvCampaignName = itemView.findViewById(R.id.campaignName);
            tvLocation = itemView.findViewById(R.id.location);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDonation = itemView.findViewById(R.id.tvDonation);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}