package com.example.little_share.ui.volunteer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;

import java.util.List;

public class UrgentCampaignAdapter extends RecyclerView.Adapter<UrgentCampaignAdapter.UrgentViewHolder> {

    public interface OnUrgentCampaignClickListener {
        void onUrgentCampaignClick(Campaign campaign);
    }

    private Context context;
    private List<Campaign> urgentCampaigns;
    private OnUrgentCampaignClickListener listener;

    public UrgentCampaignAdapter(Context context, List<Campaign> urgentCampaigns, OnUrgentCampaignClickListener listener) {
        this.context = context;
        this.urgentCampaigns = urgentCampaigns;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UrgentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_urgent_campaign, parent, false);
        return new UrgentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UrgentViewHolder holder, int position) {
        Campaign campaign = urgentCampaigns.get(position);
        holder.bind(campaign);
    }

    @Override
    public int getItemCount() {
        return urgentCampaigns.size();
    }

    public void updateData(List<Campaign> newCampaigns) {
        this.urgentCampaigns = newCampaigns;
        notifyDataSetChanged();
    }

    class UrgentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUrgentTitle, tvUrgentLocation;

        public UrgentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUrgentTitle = itemView.findViewById(R.id.tvUrgentTitle);
            tvUrgentLocation = itemView.findViewById(R.id.tvUrgentLocation);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onUrgentCampaignClick(urgentCampaigns.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Campaign campaign) {
            tvUrgentTitle.setText(campaign.getName());

            String locationText = campaign.getLocation();
            if (campaign.getSpecificLocation() != null && !campaign.getSpecificLocation().isEmpty()) {
                locationText += " • " + campaign.getSpecificLocation();
            }
            locationText += " • Cần gấp";

            tvUrgentLocation.setText(locationText);
        }
    }
}
