package com.example.little_share.ui.ngo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.ui.common.ImageViewerDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NGOCampaignAdapter extends RecyclerView.Adapter<NGOCampaignAdapter.ViewHolder> {

    private Context context;
    private List<Campaign> campaignList;
    private OnCampaignActionListener listener;

    public interface OnCampaignActionListener {
        void onEditClick(Campaign campaign);
        void onViewDetailsClick(Campaign campaign);
    }

    public NGOCampaignAdapter(Context context, List<Campaign> campaignList, OnCampaignActionListener listener) {
        this.context = context;
        this.campaignList = campaignList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_campaign, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Campaign campaign = campaignList.get(position);

        // Tên chiến dịch
        if (holder.tvCampaignName != null) {
            holder.tvCampaignName.setText(campaign.getName() != null ? campaign.getName() : "");
        }

        // Category chip
        if (holder.chipCategory != null) {
            holder.chipCategory.setText(campaign.getCategory() != null ? campaign.getCategory() : "");
        }

        // Status chip
        if (holder.chipStatus != null) {
            String status = campaign.getStatus() != null ? campaign.getStatus() : "";
            switch (status.toUpperCase()) {
                case "ONGOING":
                    holder.chipStatus.setText("Đang diễn ra");
                    holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                    break;
                case "UPCOMING":
                    holder.chipStatus.setText("Sắp diễn ra");
                    holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_blue_light);
                    break;
                case "COMPLETED":
                    holder.chipStatus.setText("Đã hoàn thành");
                    holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_light);
                    break;
                default:
                    holder.chipStatus.setText(status);
                    break;
            }
        }

        // Địa điểm (tvCampaignDate trong layout - tên sai nhưng dùng cho location)
        if (holder.tvLocation != null) {
            holder.tvLocation.setText(campaign.getLocation() != null ? campaign.getLocation() : "");
        }

        // Thời gian (tvCampaignDateTime)
        if (holder.tvDateTime != null && campaign.getStartDate() != null && campaign.getEndDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateRange = sdf.format(campaign.getStartDate()) + " – " + sdf.format(campaign.getEndDate());
            holder.tvDateTime.setText(dateRange);
        }

        // Tình nguyện viên
        if (holder.tvVolunteers != null) {
            String volunteers = campaign.getCurrentVolunteers() + "/" + campaign.getMaxVolunteers();
            holder.tvVolunteers.setText(volunteers);
        }

        // Điểm thưởng
        if (holder.tvPoints != null) {
            holder.tvPoints.setText(campaign.getPointsReward() + " điểm");
        }

        // Progress bar và tiền gây quỹ
        if (campaign.isNeedsSponsor()) {
            double currentAmount = campaign.getCurrentBudget();
            double targetAmount = campaign.getTargetBudget();
            int progress = targetAmount > 0 ? (int) ((currentAmount / targetAmount) * 100) : 0;

            if (holder.progressBar != null) {
                holder.progressBar.setProgress(progress);
            }

            if (holder.tvProgressNumber != null) {
                holder.tvProgressNumber.setText(progress + "%");
            }

            if (holder.tvCurrentAmount != null) {
                holder.tvCurrentAmount.setText(formatMoney(currentAmount) + "đ");
            }
        } else {
            // Ẩn phần gây quỹ nếu không cần
            if (holder.progressBar != null) holder.progressBar.setVisibility(View.GONE);
            if (holder.tvProgressNumber != null) holder.tvProgressNumber.setVisibility(View.GONE);
            if (holder.tvCurrentAmount != null) holder.tvCurrentAmount.setVisibility(View.GONE);
        }

        // Load image
        if (holder.ivCampaignIcon != null) {
            if (campaign.getImageUrl() != null && !campaign.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(campaign.getImageUrl())
                        .placeholder(R.drawable.img_nauanchoem)
                        .error(R.drawable.img_nauanchoem)
                        .into(holder.ivCampaignIcon);
                
                // Click listener để xem ảnh phóng to
                holder.ivCampaignIcon.setOnClickListener(v -> {
                    ImageViewerDialog dialog = new ImageViewerDialog(context, campaign.getImageUrl());
                    dialog.show();
                });
            } else {
                holder.ivCampaignIcon.setImageResource(R.drawable.img_nauanchoem);
                holder.ivCampaignIcon.setOnClickListener(null);
            }
        }

        // Button listeners
        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(campaign);
                }
            });
        }

        if (holder.btnViewDetails != null) {
            holder.btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailsClick(campaign);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return campaignList != null ? campaignList.size() : 0;
    }

    private String formatMoney(double amount) {
        return String.format(Locale.getDefault(), "%,.0f", amount).replace(",", ".");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCampaignName, tvLocation, tvDateTime, tvVolunteers, tvPoints;
        TextView tvProgressNumber, tvCurrentAmount;
        ImageView ivCampaignIcon;
        Chip chipCategory, chipStatus;
        ProgressBar progressBar;
        MaterialButton btnEdit, btnViewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Khớp với ID trong layout
            tvCampaignName = itemView.findViewById(R.id.tvCampaignName);
            chipCategory = itemView.findViewById(R.id.chipCategory);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            tvLocation = itemView.findViewById(R.id.tvCampaignDate); // ID sai trong layout nhưng dùng cho location
            tvDateTime = itemView.findViewById(R.id.tvCampaignDateTime);
            tvVolunteers = itemView.findViewById(R.id.tvVolunteers);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            ivCampaignIcon = itemView.findViewById(R.id.ivCampaignIcon);

            // Progress bar
            progressBar = itemView.findViewById(R.id.progressBar);
            tvProgressNumber = itemView.findViewById(R.id.tvProgressNumber);
            tvCurrentAmount = itemView.findViewById(R.id.tvCurrentAmount);

            // Buttons
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}