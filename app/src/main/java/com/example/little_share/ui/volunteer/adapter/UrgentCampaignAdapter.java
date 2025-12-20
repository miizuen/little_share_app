package com.example.little_share.ui.volunteer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;

import java.util.ArrayList;
import java.util.List;

public class UrgentCampaignAdapter extends RecyclerView.Adapter<UrgentCampaignAdapter.UrgentCampaignViewHolder> {

    private static final String TAG = "UrgentCampaignAdapter";
    private Context context;
    private List<Campaign> campaignList;
    private OnCampaignClickListener listener;

    public interface OnCampaignClickListener {
        void onCampaignClick(Campaign campaign);
    }

    public UrgentCampaignAdapter(Context context, List<Campaign> campaignList, OnCampaignClickListener listener) {
        this.context = context;
        this.campaignList = campaignList != null ? campaignList : new ArrayList<>();
        this.listener = listener;
        Log.d(TAG, "Adapter initialized with " + this.campaignList.size() + " items");
    }

    @NonNull
    @Override
    public UrgentCampaignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_urgent_campaign, parent, false);
        return new UrgentCampaignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UrgentCampaignViewHolder holder, int position) {
        Campaign campaign = campaignList.get(position);
        holder.bind(campaign, listener, context);
    }

    @Override
    public int getItemCount() {
        int count = campaignList.size();
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    public void updateData(List<Campaign> newList) {
        this.campaignList = newList != null ? newList : new ArrayList<>();
        Log.d(TAG, "Data updated: " + this.campaignList.size() + " items");
        notifyDataSetChanged();
    }

    public static class UrgentCampaignViewHolder extends RecyclerView.ViewHolder {
        TextView tvUrgentBadge;
        TextView tvCampaignTitle;
        TextView tvOrganizationName;
        TextView tvLocation;
        TextView tvDescription;
        TextView tvCategory;
        CardView btnDonateNow;

        public UrgentCampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUrgentBadge = itemView.findViewById(R.id.tvUrgentBadge);
            tvCampaignTitle = itemView.findViewById(R.id.tvCampaignTitle);
            tvOrganizationName = itemView.findViewById(R.id.tvOrganizationName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnDonateNow = itemView.findViewById(R.id.btnDonateNow);
        }

        public void bind(Campaign campaign, OnCampaignClickListener listener, Context context) {
            // Campaign name
            tvCampaignTitle.setText(campaign.getName());

            // Organization name
            String orgName = campaign.getOrganizationName();
            tvOrganizationName.setText(orgName != null && !orgName.isEmpty()
                    ? orgName
                    : "Tổ chức từ thiện");

            // Location + Working hours
            String location = campaign.getLocation();
            String workingHours = campaign.getSpecificLocation();
            String locationText = location;
            if (workingHours != null && !workingHours.isEmpty()) {
                locationText = location + " • " + workingHours;
            }
            tvLocation.setText(locationText);

            // Description
            String description = campaign.getDescription();
            tvDescription.setText(description != null && !description.isEmpty()
                    ? description
                    : "Chiến dịch quyên góp vật phẩm");

            // ===== THAY ĐỔI QUAN TRỌNG: Hiển thị DONATION TYPE thay vì CATEGORY =====
            String donationType = campaign.getDonationType();
            if (donationType != null && !donationType.isEmpty()) {
                try {
                    Campaign.DonationType type = Campaign.DonationType.valueOf(donationType);
                    tvCategory.setText(type.getDisplayName());
                    setDonationTypeColor(type);

                    Log.d(TAG, "Campaign: " + campaign.getName() + " | Type: " + type.getDisplayName());
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid donation type: " + donationType);
                    tvCategory.setText("Quyên góp");
                    tvCategory.setTextColor(0xFF666666);
                }
            } else {
                Log.w(TAG, "Campaign " + campaign.getName() + " has no donation type");
                tvCategory.setText("Quyên góp");
                tvCategory.setTextColor(0xFF666666);
            }

            // Show/hide urgent badge based on CATEGORY (not donation type)
            if ("URGENT".equals(campaign.getCategory())) {
                tvUrgentBadge.setVisibility(View.VISIBLE);
            } else {
                tvUrgentBadge.setVisibility(View.GONE);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    Log.d(TAG, "Item clicked: " + campaign.getName());
                    listener.onCampaignClick(campaign);
                }
            });

            btnDonateNow.setOnClickListener(v -> {
                if (listener != null) {
                    Log.d(TAG, "Donate button clicked: " + campaign.getName());
                    listener.onCampaignClick(campaign);
                }
            });
        }

        private void setDonationTypeColor(Campaign.DonationType donationType) {
            int color;
            switch (donationType) {
                case BOOKS:
                    color = 0xFF1976D2; // Blue for books
                    break;
                case CLOTHES:
                    color = 0xFF9C27B0; // Purple for clothes
                    break;
                case TOYS:
                    color = 0xFFFF9800; // Orange for toys
                    break;
                case ESSENTIALS:
                    color = 0xFF4CAF50; // Green for essentials
                    break;
                case MIXED:
                    color = 0xFF607D8B; // Grey for mixed
                    break;
                default:
                    color = 0xFF666666;
            }
            tvCategory.setTextColor(color);
        }
    }
}