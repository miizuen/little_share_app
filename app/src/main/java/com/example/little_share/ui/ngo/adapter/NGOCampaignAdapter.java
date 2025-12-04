package com.example.little_share.ui.ngo.adapter;

import android.content.Context;
import android.net.Uri;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.little_share.R;
import com.example.little_share.adapter.NotificationAdapter;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.Campain.CampaignRole;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NGOCampaignAdapter extends RecyclerView.Adapter<NGOCampaignAdapter.ViewHolder> {
    private Context context;
    private List<Campaign> campaigns;
    private OnItemClickListener listener;

    public NGOCampaignAdapter(Context context, List<Campaign> campaigns, OnItemClickListener listener) {
        this.context = context;
        this.campaigns = campaigns != null ? new ArrayList<>(campaigns) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public NGOCampaignAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_campaign, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NGOCampaignAdapter.ViewHolder holder, int position) {
        Campaign c = campaigns.get(position);

        holder.tvCampaignName.setText(c.getName() != null ? c.getName() : "Chiến dịch");
        holder.tvCategory.setText(c.getCategoryEnum() != null ? c.getCategoryEnum().getDisplayName() : "Khác");
        holder.tvStatus.setText(c.getStatusEnum() != null ? c.getStatusEnum().getDisplayName() : "Sắp tới");

        // Địa điểm + thời gian
        holder.tvCampaignDate.setText(c.getLocation() != null ? c.getLocation() : "Chưa xác định");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String time = sdf.format(c.getStartDate()) + " - " + sdf.format(c.getEndDate());
        holder.tvCampaignDateTime.setText(time);

        // Tình nguyện viên + điểm
        holder.tvVolunteers.setText(c.getCurrentVolunteers() + "/" + c.getMaxVolunteers());
        holder.tvPoints.setText(c.getPointsReward() + " điểm");

        // === FIX CRASH PROGRESSBAR ===
        int budgetProgress = c.getBudgetProgressPercentage();
        if (holder.progressBar != null) {
            holder.progressBar.setProgress(budgetProgress);
        }
        if (holder.tvProgressNumber != null) {
            holder.tvProgressNumber.setText(budgetProgress + "%");
        }

        // === FIX TIỀN TỆ ===
        holder.tvCurrentAmount.setText(formatMoney(c.getCurrentBudget()) + "đ");
        holder.tvTargetAmount.setText(formatMoney(c.getTargetBudget()) + "đ");

        if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) {
            String imageUrl = c.getImageUrl();

            if (imageUrl.startsWith("content://com.google.android.apps.photos")) {
                // URI từ Google Photos không work - dùng ảnh mặc định
                holder.ivCampaignIcon.setImageResource(R.drawable.img_nauanchoem);
            } else if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                // URI local - xử lý cẩn thận
                try {
                    Glide.with(context)
                            .load(Uri.parse(imageUrl))
                            .placeholder(R.drawable.img_nauanchoem)
                            .error(R.drawable.img_nauanchoem)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(holder.ivCampaignIcon);
                } catch (Exception e) {
                    holder.ivCampaignIcon.setImageResource(R.drawable.img_nauanchoem);
                }
            } else {
                // URL HTTP/HTTPS
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.img_nauanchoem)
                        .error(R.drawable.img_nauanchoem)
                        .into(holder.ivCampaignIcon);
            }
        } else {
            holder.ivCampaignIcon.setImageResource(R.drawable.img_nauanchoem);
        }

        // Nút
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(c));
        holder.btnViewDetails.setOnClickListener(v -> listener.onDetailClick(c));
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f", amount).replace(",", ".");
    }

    @Override
    public int getItemCount() {
        return campaigns.size();
    }

    public interface OnItemClickListener {
        void onEditClick(Campaign campaign);
        void onDetailClick(Campaign campaign);
    }

    public void updateData(List<Campaign> newData){
        campaigns.clear();
        campaigns.addAll(newData);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCampaignIcon;
        TextView tvCampaignName, tvCategory, tvStatus, tvCampaignDate, tvCampaignDateTime;
        TextView tvVolunteers, tvPoints, tvProgressNumber, tvCurrentAmount, tvTargetAmount;
        ProgressBar progressBar;
        Button btnEdit, btnViewDetails;

        ViewHolder(View v) {
            super(v);
            ivCampaignIcon = v.findViewById(R.id.ivCampaignIcon);
            tvCampaignName = v.findViewById(R.id.tvCampaignName);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvCampaignDate = v.findViewById(R.id.tvCampaignDate);
            tvCampaignDateTime = v.findViewById(R.id.tvCampaignDateTime);
            tvVolunteers = v.findViewById(R.id.tvVolunteers);
            tvPoints = v.findViewById(R.id.tvPoints);
            tvProgressNumber = v.findViewById(R.id.tvProgressNumber);
            progressBar = v.findViewById(R.id.progressBar);
            tvCurrentAmount = v.findViewById(R.id.tvCurrentAmount);
            tvTargetAmount = v.findViewById(R.id.tvTargetAmount);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnViewDetails = v.findViewById(R.id.btnViewDetails);
        }
    }


}
