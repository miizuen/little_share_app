package com.example.little_share.ui.ngo.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.VolunteerEventDetail;

import java.util.ArrayList;
import java.util.List;

public class VolunteerEventAdapter extends RecyclerView.Adapter<VolunteerEventAdapter.ViewHolder> {

    private List<VolunteerEventDetail> events = new ArrayList<>();

    public void setData(List<VolunteerEventDetail> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ngo_volunteer_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(VolunteerEventDetail event) {
            tvEventName.setText(event.getEventName());
            tvEventDate.setText(event.getEventDate());

            // Set status với màu sắc tương ứng
            String status = event.getStatus();

            if ("completed".equals(status)) {
                tvStatus.setText("Đã hoàn thành");
                tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                tvStatus.setTextColor(Color.parseColor("#16A34A")); // Xanh lá

            } else if ("approved".equals(status) || "joined".equals(status) || "attended".equals(status)) {
                tvStatus.setText("Đang tham gia");
                tvStatus.setBackgroundResource(R.drawable.bg_status_joined);
                tvStatus.setTextColor(Color.parseColor("#2563EB")); // Xanh dương

            } else if ("pending".equals(status)) {
                tvStatus.setText("Đã đăng ký");
                tvStatus.setBackgroundResource(R.drawable.bg_status_registered);
                tvStatus.setTextColor(Color.parseColor("#6B7280")); // Xám

            } else {
                // Trạng thái khác (cancelled, rejected, etc.)
                tvStatus.setText(status);
                tvStatus.setBackgroundResource(R.drawable.bg_status_registered);
                tvStatus.setTextColor(Color.parseColor("#6B7280"));
            }
        }


    }
}
