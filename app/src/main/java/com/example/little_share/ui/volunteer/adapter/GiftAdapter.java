package com.example.little_share.ui.volunteer.adapter;

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
import com.google.android.material.button.MaterialButton;
import com.example.little_share.R;
import com.example.little_share.data.models.Gift;
import com.example.little_share.ui.volunteer.activity_volunteer_gift_detail;
import java.util.List;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {
    private Context context;
    private List<Gift> giftList;

    public GiftAdapter(Context context, List<Gift> giftList) {
        this.context = context;
        this.giftList = giftList;
    }
    public void updateGiftList(List<Gift> newGiftList) {
        android.util.Log.d("GiftAdapter", "updateGiftList called with " + (newGiftList != null ? newGiftList.size() : "null") + " gifts");

        if (newGiftList != null) {
            this.giftList.clear();
            this.giftList.addAll(newGiftList);
            notifyDataSetChanged();

            android.util.Log.d("GiftAdapter", "Adapter updated, total items: " + getItemCount());
        }
    }


    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gift, parent, false);
        return new GiftViewHolder(view);
    }

    @Override

    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        Gift gift = giftList.get(position);

        holder.tvGiftName.setText(gift.getName());
        holder.tvPoints.setText("🌟 +" + gift.getPointsRequired());

        // THÊM MỚI: Hiển thị số lượng còn lại
        holder.tvQuantity.setText("Còn " + gift.getAvailableQuantity() + "/" + gift.getTotalQuantity());

        // Load image từ URL bằng Glide
        if (gift.getImageUrl() != null && !gift.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(gift.getImageUrl())
                    .placeholder(R.drawable.gift_teddy_bear)
                    .error(R.drawable.gift_teddy_bear)
                    .into(holder.ivGift);
        } else {
            holder.ivGift.setImageResource(R.drawable.gift_teddy_bear);
        }

        // Hiển thị HOT badge cho quà có điểm cao
        if (gift.getPointsRequired() > 800) {
            holder.tvHotBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvHotBadge.setVisibility(View.GONE);
        }

        // Xử lý trạng thái hết hàng
        if (!gift.isAvailable()) {
            holder.viewOutOfStockOverlay.setVisibility(View.VISIBLE);
            holder.tvOutOfStock.setVisibility(View.VISIBLE);
            holder.btnExchange.setVisibility(View.GONE);
            holder.btnOutOfStock.setVisibility(View.VISIBLE);

            // THÊM: Đổi màu text số lượng khi hết hàng
            holder.tvQuantity.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.viewOutOfStockOverlay.setVisibility(View.GONE);
            holder.tvOutOfStock.setVisibility(View.GONE);
            holder.btnExchange.setVisibility(View.VISIBLE);
            holder.btnOutOfStock.setVisibility(View.GONE);

            // THÊM: Màu bình thường khi còn hàng
            holder.tvQuantity.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Click listeners
        holder.btnExchange.setOnClickListener(v -> {
            Intent intent = new Intent(context, activity_volunteer_gift_detail.class);
            intent.putExtra("gift", gift);
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            if (gift.isAvailable()) {
                Intent intent = new Intent(context, activity_volunteer_gift_detail.class);
                intent.putExtra("gift", gift);
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public static class GiftViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGift;
        TextView tvGiftName, tvPoints, tvQuantity; // THÊM tvQuantity
        MaterialButton tvHotBadge, btnExchange, btnOutOfStock, tvOutOfStock;
        View viewOutOfStockOverlay;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGift = itemView.findViewById(R.id.ivGift);
            tvGiftName = itemView.findViewById(R.id.tvGiftName);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvQuantity = itemView.findViewById(R.id.tvQuantity); // THÊM dòng này
            tvHotBadge = itemView.findViewById(R.id.tvHotBadge);
            btnExchange = itemView.findViewById(R.id.btnExchange);
            btnOutOfStock = itemView.findViewById(R.id.btnOutOfStock);
            tvOutOfStock = itemView.findViewById(R.id.tvOutOfStock);
            viewOutOfStockOverlay = itemView.findViewById(R.id.viewOutOfStockOverlay);
        }
    }

}
