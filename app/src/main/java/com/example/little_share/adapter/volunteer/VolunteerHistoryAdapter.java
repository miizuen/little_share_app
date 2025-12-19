package com.example.little_share.adapter.volunteer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.volunteer.VolunteerHistoryModel;

import java.util.ArrayList;
import java.util.List;

public class VolunteerHistoryAdapter extends RecyclerView.Adapter<VolunteerHistoryAdapter.HistoryViewHolder> {

    private List<VolunteerHistoryModel> historyList;
    private OnHistoryItemClickListener clickListener;

    public interface OnHistoryItemClickListener {
        void onItemClick(VolunteerHistoryModel history, int position);
    }

    public VolunteerHistoryAdapter() {
        this.historyList = new ArrayList<>();
    }

    public void setHistoryList(List<VolunteerHistoryModel> historyList) {
        this.historyList = historyList;
        notifyDataSetChanged();
    }

    public void setClickListener(OnHistoryItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_campaign_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        VolunteerHistoryModel history = historyList.get(position);
        holder.bind(history, position);
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStatus;
        private TextView tvPoints;
        private TextView tvCampaignTitle;
        private TextView tvRole;
        private TextView tvDate;
        private TextView tvTime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvCampaignTitle = itemView.findViewById(R.id.tvCampaignTitle);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(VolunteerHistoryModel history, int position) {
            // Set status
            tvStatus.setText(history.getStatus());

            // Set status color
            try {
                tvStatus.setTextColor(Color.parseColor(history.getStatusColor()));
            } catch (Exception e) {
                tvStatus.setTextColor(Color.parseColor("#22C55E")); // Default green
            }

            // Set points
            tvPoints.setText(String.valueOf(history.getPoints()));

            // Set campaign title
            tvCampaignTitle.setText(history.getCampaignTitle());

            // Set role
            tvRole.setText(history.getRole());

            // Set date
            tvDate.setText(history.getDate());

            // Set time
            tvTime.setText(history.getTime());

            // Handle item click
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(history, position);
                }
            });
        }
    }
}