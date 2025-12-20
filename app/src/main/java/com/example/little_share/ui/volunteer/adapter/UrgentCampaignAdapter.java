package com.example.little_share.ui.volunteer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;

import java.util.List;

public class UrgentCampaignAdapter extends RecyclerView.Adapter<UrgentCampaignAdapter.UrgentCampaignViewHolder> {

    private Context context;
    private List<Campaign> campaigns;
    private OnUrgentCampaignClickListener listener;

    public interface OnUrgentCampaignClickListener {
        void onCampaignClick(Campaign campaign);
    }

    public UrgentCampaignAdapter(Context context, List<Campaign> campaigns, OnUrgentCampaignClickListener listener) {
        this.context = context;
        this.campaigns = campaigns;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UrgentCampaignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_urgent_campaign, parent, false);
        return new UrgentCampaignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UrgentCampaignViewHolder holder, int position) {
        Campaign campaign = campaigns.get(position);
        holder.bind(campaign);
    }

    @Override
    public int getItemCount() {
        return campaigns != null ? campaigns.size() : 0;
    }

    public void updateData(List<Campaign> newCampaigns) {
        this.campaigns = newCampaigns;
        notifyDataSetChanged();
    }

    class UrgentCampaignViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCampaignTitle, tvOrganizationName, tvLocation, tvDescription, tvCategory;
        private CardView btnDonateNow;

        public UrgentCampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvCampaignTitle = itemView.findViewById(R.id.tvCampaignTitle);
            tvOrganizationName = itemView.findViewById(R.id.tvOrganizationName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnDonateNow = itemView.findViewById(R.id.btnDonateNow);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onCampaignClick(campaigns.get(getAdapterPosition()));
                }
            });

            btnDonateNow.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onCampaignClick(campaigns.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Campaign campaign) {
            tvCampaignTitle.setText(campaign.getName());
            tvOrganizationName.setText(campaign.getOrganizationName());
            tvDescription.setText(campaign.getDescription());

            // Format location with working hours
            String locationText = campaign.getLocation();
            if (campaign.getSpecificLocation() != null && !campaign.getSpecificLocation().isEmpty()) {
                locationText += " • " + campaign.getSpecificLocation();
            }
            tvLocation.setText(locationText);

            // Set category display
            String categoryDisplay = getCategoryDisplayName(campaign.getCategory());
            tvCategory.setText(categoryDisplay);
        }

        private String getCategoryDisplayName(String category) {
            if (category == null) return "Khác";
            
            switch (category.toUpperCase()) {
                case "BOOKS":
                case "EDUCATION":
                    return "Sách vở";
                case "CLOTHES":
                    return "Quần áo";
                case "TOYS":
                    return "Đồ chơi";
                case "ESSENTIALS":
                case "FOOD":
                    return "Nhu yếu phẩm";
                default:
                    return "Khác";
            }
        }
    }
}