package com.example.little_share.ui.volunteer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.VolunteerRegistration;

import java.util.List;

public class MyRegistrationAdapter extends RecyclerView.Adapter<MyRegistrationAdapter.ViewHolder> {

    private Context context;
    private List<VolunteerRegistration> registrations;
    private OnViewQRListener listener;

    public interface OnViewQRListener {
        void onViewQR(VolunteerRegistration registration);
    }

    public MyRegistrationAdapter(Context context, List<VolunteerRegistration> registrations, OnViewQRListener listener) {
        this.context = context;
        this.registrations = registrations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_campaign_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VolunteerRegistration reg = registrations.get(position);

        // Tên chiến dịch
        holder.tvCampaignTitle.setText(reg.getCampaignName());

        // Vai trò
        holder.tvRole.setText(reg.getRoleName());

        // Ngày
        holder.tvDate.setText(reg.getDate());

        // Giờ (ca làm)
        holder.tvTime.setText(reg.getShiftTime());

        // Trạng thái
        String status = reg.getStatus();
        if ("approved".equals(status)) {
            holder.tvStatus.setText("✓ Đã duyệt");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.primary_green));
        } else if ("pending".equals(status)) {
            holder.tvStatus.setText("⏳ Chờ duyệt");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.primary_orange));
        } else if ("rejected".equals(status)) {
            holder.tvStatus.setText("✕ Từ chối");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.error));
        }

        // Điểm (có thể để 0 hoặc lấy từ role nếu có)
        holder.tvPoints.setText("0");

        // Click để xem QR (chỉ khi đã duyệt)
        holder.itemView.setOnClickListener(v -> {
            if ("approved".equals(status) && listener != null) {
                listener.onViewQR(reg);
            }
        });
    }

    @Override
    public int getItemCount() {
        return registrations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvPoints, tvCampaignTitle, tvRole, tvDate, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvCampaignTitle = itemView.findViewById(R.id.tvCampaignTitle);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
