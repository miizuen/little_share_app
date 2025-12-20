package com.example.little_share.ui.ngo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Gift;
import com.example.little_share.ui.volunteer.activity_volunteer_gift_detail;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {
    private Context context;
    private List<Gift> giftList;
    private OnGiftActionListener listener;

    public interface OnGiftActionListener {
        void onDeleteClick(Gift gift);
    }

    public GiftAdapter(Context context, List<Gift> giftList, OnGiftActionListener listener) {
        this.context = context;
        this.giftList = giftList;
        this.listener = listener;
    }

    public static class GiftViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGiftImage, btnDelete;
        TextView tvGiftName, tvGiftPoints, tvStock;
        Chip chipCategory;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGiftImage = itemView.findViewById(R.id.ivGiftImage);
            tvGiftName = itemView.findViewById(R.id.tvGiftName);
            tvGiftPoints = itemView.findViewById(R.id.tvGiftPoints);
            chipCategory = itemView.findViewById(R.id.chipCategory);
            tvStock = itemView.findViewById(R.id.tvStock);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public GiftAdapter.GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ngo_gift_list, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        Gift gift = giftList.get(position);

        holder.tvGiftName.setText(gift.getName());
        holder.tvGiftPoints.setText(gift.getPointsRequired() + " điểm");

        // Hiển thị số lượng còn lại
        holder.tvStock.setText("Còn " + gift.getAvailableQuantity() + "/" + gift.getTotalQuantity());

        // Hiển thị category
        holder.chipCategory.setText(gift.getCategory());

        // Load image từ URL bằng Glide
        if (gift.getImageUrl() != null && !gift.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(gift.getImageUrl())
                    .placeholder(R.drawable.gift_teddy_bear)
                    .error(R.drawable.gift_teddy_bear)
                    .into(holder.ivGiftImage);
        } else {
            holder.ivGiftImage.setImageResource(R.drawable.gift_teddy_bear);
        }

        // Đổi màu text số lượng khi hết hàng
        if (!gift.isAvailable()) {
            holder.tvStock.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvStock.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Click delete
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(gift);
            }
        });

        // Click item
        holder.itemView.setOnClickListener(v -> {
            // Có thể mở dialog xem chi tiết hoặc chỉnh sửa
            if (listener != null) {
                // listener.onItemClick(gift);
            }
        });
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public void updateData(List<Gift> newList) {
        this.giftList = newList;
        notifyDataSetChanged();
    }

    public void updateGiftList(List<Gift> newGiftList) {
        this.giftList.clear();
        this.giftList.addAll(newGiftList);
        notifyDataSetChanged();
    }

    public void updateGift(Gift updatedGift) {
        for (int i = 0; i < giftList.size(); i++) {
            if (giftList.get(i).getId().equals(updatedGift.getId())) {
                giftList.set(i, updatedGift);
                notifyItemChanged(i);
                break;
            }
        }
    }
}