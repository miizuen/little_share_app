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

        // Load image từ URL bằng Glide
        if (gift.getImageUrl() != null && !gift.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(gift.getImageUrl())
                    .placeholder(R.drawable.gift_teddy_bear) // placeholder image
                    .error(R.drawable.gift_teddy_bear) // error image
                    .into(holder.ivGift);
        } else {
            holder.ivGift.setImageResource(R.drawable.gift_teddy_bear);
        }

        // Hiển thị HOT badge cho quà có điểm cao (ví dụ > 800 điểm)
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
        } else {
            holder.viewOutOfStockOverlay.setVisibility(View.GONE);
            holder.tvOutOfStock.setVisibility(View.GONE);
            holder.btnExchange.setVisibility(View.VISIBLE);
            holder.btnOutOfStock.setVisibility(View.GONE);
        }

        // Trong phương thức onBindViewHolder, cập nhật click listeners:
        holder.btnExchange.setOnClickListener(v -> {
            Intent intent = new Intent(context, activity_volunteer_gift_detail.class);
            intent.putExtra("gift", gift); // Truyền toàn bộ object Gift
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            if (gift.isAvailable()) {
                Intent intent = new Intent(context, activity_volunteer_gift_detail.class);
                intent.putExtra("gift", gift); // Truyền toàn bộ object Gift
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
        TextView tvGiftName, tvPoints;
        MaterialButton tvHotBadge, btnExchange, btnOutOfStock, tvOutOfStock;
        View viewOutOfStockOverlay;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGift = itemView.findViewById(R.id.ivGift);
            tvGiftName = itemView.findViewById(R.id.tvGiftName);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvHotBadge = itemView.findViewById(R.id.tvHotBadge);
            btnExchange = itemView.findViewById(R.id.btnExchange);
            btnOutOfStock = itemView.findViewById(R.id.btnOutOfStock);
            tvOutOfStock = itemView.findViewById(R.id.tvOutOfStock);
            viewOutOfStockOverlay = itemView.findViewById(R.id.viewOutOfStockOverlay);

        }
    }
}
