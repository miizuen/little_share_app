package com.example.little_share.ui.ngo.adapter;

import android.content.Context;
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

    @NonNull
    @Override
    public GiftAdapter.GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ngo_gift_list, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftAdapter.GiftViewHolder holder, int position) {
        Gift gift = giftList.get(position);

        holder.tvGiftName.setText(gift.getName());

        holder.tvGiftPoints.setText(String.valueOf(gift.getPointsRequired()));


        holder.chipCategory.setText(gift.getCategory());


        switch (gift.getCategory()) {
            case "Đồ lưu niệm":
                holder.chipCategory.setChipBackgroundColorResource(android.R.color.holo_green_light);
                break;
            case "Quà gia dụng":
                holder.chipCategory.setChipBackgroundColorResource(android.R.color.holo_blue_light);
                break;
            case "Văn phòng phẩm":
                holder.chipCategory.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                break;
            default:
                holder.chipCategory.setChipBackgroundColorResource(android.R.color.darker_gray);
                break;
        }

        // Số lượng còn lại
        holder.tvStock.setText("Còn " + gift.getAvailableQuantity() + "/" + gift.getTotalQuantity());

        // Load ảnh
        if (gift.getImageUrl() != null && !gift.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(gift.getImageUrl())
                    .placeholder(R.drawable.teddy)
                    .error(R.drawable.teddy)
                    .into(holder.ivGiftImage);
        } else {
            holder.ivGiftImage.setImageResource(R.drawable.teddy);
        }

        // Xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(gift);
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


    static class GiftViewHolder extends RecyclerView.ViewHolder {
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
}
