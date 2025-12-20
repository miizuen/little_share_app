package com.example.little_share.adapter;

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
import com.example.little_share.data.models.CampaignModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.CampaignViewHolder> {

    private List<CampaignModel> campaignList;
    private OnCampaignClickListener listener;

    public interface OnCampaignClickListener {
        void onEditClick(CampaignModel campaign, int position);
        void onViewDetailsClick(CampaignModel campaign, int position);
    }

    public CampaignAdapter() {
        this.campaignList = new ArrayList<>();
    }

    public CampaignAdapter(List<CampaignModel> campaignList) {
        this.campaignList = campaignList;
    }

    public void setOnCampaignClickListener(OnCampaignClickListener listener) {
        this.listener = listener;
    }

    public void setCampaignList(List<CampaignModel> campaignList) {
        this.campaignList = campaignList;
        notifyDataSetChanged();
    }

    public void updateList(List<CampaignModel> newList) {
        this.campaignList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CampaignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_campaign, parent, false);
        return new CampaignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignViewHolder holder, int position) {
        CampaignModel campaign = campaignList.get(position);
        holder.bind(campaign, position);
    }

    @Override
    public int getItemCount() {
        return campaignList != null ? campaignList.size() : 0;
    }

    class CampaignViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCampaignIcon;
        private TextView tvCampaignName, tvCategory, tvStatus;
        private TextView tvCampaignDate, tvCampaignDateTime;
        private TextView tvVolunteers, tvPoints;
        private TextView tvProgressNumber, tvCurrentAmount, tvTargetAmount;
        private Button btnEdit, btnViewDetails;

        public CampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCampaignIcon = itemView.findViewById(R.id.ivCampaignIcon);
            tvCampaignName = itemView.findViewById(R.id.tvCampaignName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCampaignDate = itemView.findViewById(R.id.tvCampaignDate);
            tvCampaignDateTime = itemView.findViewById(R.id.tvCampaignDateTime);
            tvVolunteers = itemView.findViewById(R.id.tvVolunteers);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvProgressNumber = itemView.findViewById(R.id.tvProgressNumber);
            tvCurrentAmount = itemView.findViewById(R.id.tvCurrentAmount);
            tvTargetAmount = itemView.findViewById(R.id.tvTargetAmount);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(CampaignModel campaign, int position) {
            try {
                // Set basic info
                tvCampaignName.setText(campaign.getName());
                tvCategory.setText(campaign.getCategory());
                tvStatus.setText(campaign.getStatus());
                tvCampaignDate.setText(campaign.getLocation());
                tvCampaignDateTime.setText(campaign.getDateRange());
                tvVolunteers.setText(campaign.getVolunteerStatus());
                tvPoints.setText(campaign.getPoints() + " điểm");

                // Set icon
                ivCampaignIcon.setImageResource(campaign.getIconResId());

                // Set progress (chỉ hiển thị số %)
                int progress = campaign.getProgressPercentage();
                tvProgressNumber.setText(progress + "%");

                // Format currency
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvCurrentAmount.setText(formatter.format(campaign.getCurrentAmount()) + "đ");
                tvTargetAmount.setText(formatter.format(campaign.getTargetAmount()) + "đ");

                // Disable item click to prevent navigation
                itemView.setClickable(false);
                itemView.setFocusable(false);

                // Set button listeners
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onEditClick(campaign, position);
                        }
                    }
                });

                btnViewDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onViewDetailsClick(campaign, position);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}