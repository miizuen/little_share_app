package com.example.little_share.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationModel> notificationList;
    private OnNotificationDeleteListener deleteListener;
    private AdapterView.OnItemClickListener listener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnNotificationDeleteListener {
        void onDelete(int position);
    }

    public NotificationAdapter() {
        this.notificationList = new ArrayList<>();
    }

    public void setNotificationList(List<NotificationModel> notificationList) {
        this.notificationList = notificationList;
        notifyDataSetChanged();
    }
    public NotificationAdapter(List<NotificationModel> notificationList) {
        this.notificationList = notificationList;
    }

    public void setDeleteListener(OnNotificationDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);
        holder.bind(notification, position);
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < notificationList.size()) {
            notificationList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notificationList.size());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(NotificationModel notification, int position);
        void onDeleteClick(NotificationModel notification, int position);
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconImage;
        private TextView titleText;
        private TextView descText;
        private TextView timeText;
        private ImageView deleteBtn;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.iconImage);
            titleText = itemView.findViewById(R.id.titleText);
            descText = itemView.findViewById(R.id.descText);
            timeText = itemView.findViewById(R.id.timeText);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }

        public void bind(NotificationModel notification, int position) {
            titleText.setText(notification.getTitle());
            descText.setText(notification.getDescription());
            timeText.setText(notification.getTime());
            iconImage.setImageResource(notification.getIconResId());

            deleteBtn.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(position);
                }
            });

            itemView.setOnClickListener(v -> {
                // Xử lý khi click vào notification
                // Có thể mở chi tiết hoặc đánh dấu đã đọc
            });
        }
    }
}