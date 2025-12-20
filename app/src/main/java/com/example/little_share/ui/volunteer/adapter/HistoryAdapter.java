package com.example.little_share.ui.volunteer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.CampaignRegistration;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private Context context;
    private List<CampaignRegistration> historyList;
    private OnHistoryItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public HistoryAdapter(Context context, List<CampaignRegistration> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_card, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        CampaignRegistration registration = historyList.get(position);

        // Hiển thị tên chiến dịch
        holder.tvCampaignTitle.setText(registration.getCampaignName() != null ?
                registration.getCampaignName() : "Chiến dịch thiện nguyện");

        // Hiển thị vai trò
        holder.tvRole.setText(registration.getRoleName() != null ?
                registration.getRoleName() : "Tình nguyện viên");

        // Hiển thị ngày
        if (registration.getWorkDate() != null) {
            holder.tvDate.setText(dateFormat.format(registration.getWorkDate()));
        } else {
            holder.tvDate.setText("Chưa xác định");
        }

        // Hiển thị thời gian
        holder.tvTime.setText(registration.getShiftTime() != null ?
                registration.getShiftTime() : "Cả ngày");

        // Xử lý click QR code
        holder.imgQR.setOnClickListener(v -> {
            if (listener != null && registration.getQrCode() != null) {
                listener.onQRCodeClick(registration);
            }
        });

        // Xử lý click toàn bộ item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(registration);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateData(List<CampaignRegistration> newList) {
        if (newList != null && !newList.isEmpty()) {
            this.historyList.clear();
            this.historyList.addAll(newList);
            notifyDataSetChanged();

            android.util.Log.d("HistoryAdapter", "Data updated with " + newList.size() + " items");
        } else {
            android.util.Log.w("HistoryAdapter", "Attempted to update with empty or null list");
        }
    }


    public void setOnHistoryItemClickListener(OnHistoryItemClickListener listener) {
        this.listener = listener;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCampaignTitle, tvRole, tvDate, tvTime;
        ImageView imgQR;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCampaignTitle = itemView.findViewById(R.id.tvCampaignTitle);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            imgQR = itemView.findViewById(R.id.imgQR);
        }
    }

    public interface OnHistoryItemClickListener {
        void onItemClick(CampaignRegistration registration);
        void onQRCodeClick(CampaignRegistration registration);
    }
}
