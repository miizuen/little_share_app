package com.example.little_share.ui.volunteer.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Notification;
import com.example.little_share.utils.DateUtilsClass;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>{
    private Context context;
    private List<Notification> notificationList;

    private OnNotificationClickListener listener;

    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.titleText.setText(notification.getTitle());
        holder.descText.setText(notification.getMessage());

        if(notification.getCreatedAt() != null){
            CharSequence timeAgo = DateUtilsClass.getRelativeTime(notification.getCreatedAt());
            holder.timeText.setText(timeAgo);
        }else{
            holder.timeText.setText("Vừa xong");
        }

        setIconByType(holder.iconImage, notification.getTypeEnum());

        if(notification.isRead()){
            // Đã đọc: màu nhạt, không bold
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_lighter));
            holder.titleText.setTypeface(null, Typeface.NORMAL);
            holder.titleText.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.descText.setTextColor(ContextCompat.getColor(context, R.color.gray_dark));
        }else{
            //Chưa đọc: màu đậm, bold
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.titleText.setTypeface(null, Typeface.BOLD);
            holder.titleText.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.descText.setTextColor(ContextCompat.getColor(context, R.color.gray_dark));
        }

        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onNotificationClick(notification);
            }
        });

        holder.deleteBtn.setOnClickListener(v -> {
            if(listener != null){
                listener.onDeleteClick(notification, holder.getAdapterPosition());
            }
        });
    }

    public void setNotifications(List<Notification> notifications) {
        this.notificationList = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < notificationList.size()) {
            notificationList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notificationList.size());
        }
    }

    public void updateItem(int position, Notification notification) {
        if (position >= 0 && position < notificationList.size()) {
            notificationList.set(position, notification);
            notifyItemChanged(position);
        }
    }

    private void setIconByType(ImageView iconImage, Notification.NotificationType type) {
        int iconRes;

        switch (type) {
            // Campaign notifications
            case CAMPAIGN_NEW:
            case CAMPAIGN_UPDATE:
                iconRes = R.drawable.ic_megaphone;
                break;
            case CAMPAIGN_APPROVED:
                iconRes = R.drawable.icon_check;
                break;
            case CAMPAIGN_REMINDER:
                iconRes = R.drawable.ic_bell;
                break;

            // Donation notification icons (quyên góp vật phẩm)
            case DONATION_PENDING:
                iconRes = R.drawable.ic_clock; // Icon đồng hồ - chờ duyệt
                break;
            case DONATION_NEW:
                iconRes = R.drawable.ic_donation; // Icon cho NGO - có donation mới
                break;
            case DONATION_CONFIRMED:
                iconRes = R.drawable.icon_check; // Icon tick - đã xác nhận
                break;
            case DONATION_REJECTED:
                iconRes = R.drawable.ic_close_red; // Icon X đỏ - từ chối
                break;
            case DONATION_RECEIVED:
                iconRes = R.drawable.icon_check; // Icon tick - đã nhận đồ
                break;
            case DONATION_CAMPAIGN_NEW:
                iconRes = R.drawable.ic_donation; // Icon donation - campaign mới
                break;

            // Sponsorship (tài trợ tiền)
            case DONATION_SUCCESS:
            case SPONSORSHIP_SUCCESS:
                iconRes = R.drawable.ic_star_3d;
                break;

            // Registration
            case REGISTRATION_APPROVED:
                iconRes = R.drawable.icon_check;
                break;
            case REGISTRATION_REJECTED:
                iconRes = R.drawable.ic_close_red;
                break;

            // Gift
            case GIFT_AVAILABLE:
                iconRes = R.drawable.ic_gift;
                break;

            // System & Default
            case SYSTEM:
            case GENERAL:
            default:
                iconRes = R.drawable.ic_megaphone;
                break;
        }

        iconImage.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }


    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
        void onDeleteClick(Notification notification, int position);
    }

    public NotificationAdapter(Context context, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = new ArrayList<>();
        this.listener = listener;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView iconImage;
        TextView titleText;
        TextView descText;
        TextView timeText;
        ImageView deleteBtn;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            iconImage = itemView.findViewById(R.id.iconImage);
            titleText = itemView.findViewById(R.id.titleText);
            descText = itemView.findViewById(R.id.descText);
            timeText = itemView.findViewById(R.id.timeText);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }

    }




}
