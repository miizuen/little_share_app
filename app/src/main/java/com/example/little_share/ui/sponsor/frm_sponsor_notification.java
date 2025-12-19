package com.example.little_share.ui.sponsor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Notification;
import com.example.little_share.data.repositories.NotificationRepository;
import com.example.little_share.ui.volunteer.adapter.NotificationAdapter;

import java.util.ArrayList;

public class frm_sponsor_notification extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private NotificationRepository notificationRepository;
    private TextView tvNumberNotify;
    private LinearLayout markAllRead;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sponsor_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        notificationRepository = new NotificationRepository();
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadNotifications();
        loadUnreadCount();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        tvNumberNotify = view.findViewById(R.id.tvNumberNotify);
        markAllRead = view.findViewById(R.id.markAllRead);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(getContext(),
                new NotificationAdapter.OnNotificationClickListener() {
                    @Override
                    public void onNotificationClick(Notification notification) {
                        handleNotificationClick(notification);
                    }

                    @Override
                    public void onDeleteClick(Notification notification, int position) {
                        deleteNotification(notification, position);
                    }
                });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        markAllRead.setOnClickListener(v -> markAllNotificationsAsRead());
        
        // TODO: Remove this in production - only for testing
        // Long click to create mock notifications
        markAllRead.setOnLongClickListener(v -> {
            createMockNotifications();
            return true;
        });
    }
    
    // TODO: Remove this method in production
    private void createMockNotifications() {
        notificationRepository.createMockNotifications();
        Toast.makeText(getContext(), "Đã tạo notifications mẫu", Toast.LENGTH_SHORT).show();
    }

    private void loadNotifications() {
        android.util.Log.d("NOTIFICATION", "Loading notifications...");
        
        notificationRepository.getUserNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications != null && isAdded()) {
                android.util.Log.d("NOTIFICATION", "Received " + notifications.size() + " notifications");
                adapter.setNotifications(notifications);
            } else {
                android.util.Log.e("NOTIFICATION", "Notifications is null or fragment not added");
            }
        });
    }

    private void loadUnreadCount() {
        notificationRepository.getUnreadCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && isAdded()) {
                tvNumberNotify.setText(String.valueOf(count));
                
                // Hide mark all read button if no unread notifications
                markAllRead.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void handleNotificationClick(Notification notification) {
        android.util.Log.d("NOTIFICATION", "Clicked notification: " + notification.getTitle());
        
        // Mark as read if not already read
        if (!notification.isRead()) {
            notificationRepository.markAsRead(notification.getId(), new NotificationRepository.OnNotificationListener() {
                @Override
                public void onSuccess(String message) {
                    android.util.Log.d("NOTIFICATION", "Marked as read: " + notification.getId());
                }

                @Override
                public void onFailure(String error) {
                    android.util.Log.e("NOTIFICATION", "Failed to mark as read: " + error);
                }
            });
        }

        // Handle navigation based on notification type
        handleNotificationNavigation(notification);
    }

    private void handleNotificationNavigation(Notification notification) {
        Notification.NotificationType type = notification.getTypeEnum();
        String relatedId = notification.getRelatedId();
        
        switch (type) {
            case CAMPAIGN_NEW:
            case CAMPAIGN_UPDATE:
            case CAMPAIGN_APPROVED:
                // Navigate to campaign detail if relatedId is campaignId
                if (relatedId != null) {
                    navigateToCampaignDetail(relatedId);
                }
                break;
                
            case DONATION_SUCCESS:
            case SPONSORSHIP_SUCCESS:
                // Navigate to journey/history page
                navigateToJourney();
                break;
                
            case GIFT_AVAILABLE:
                // Navigate to gifts/rewards page
                Toast.makeText(getContext(), "Chuyển đến trang quà tặng", Toast.LENGTH_SHORT).show();
                break;
                
            default:
                // Default action or no action
                break;
        }
    }

    private void navigateToCampaignDetail(String campaignId) {
        // TODO: Navigate to campaign detail
        android.util.Log.d("NOTIFICATION", "Navigate to campaign: " + campaignId);
        Toast.makeText(getContext(), "Chuyển đến chi tiết chiến dịch", Toast.LENGTH_SHORT).show();
    }

    private void navigateToJourney() {
        // Navigate to journey tab
        if (getActivity() instanceof activity_sponsor_main) {
            // Switch to journey tab
            android.util.Log.d("NOTIFICATION", "Navigate to journey");
            Toast.makeText(getContext(), "Chuyển đến chặng đường chia sẻ", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteNotification(Notification notification, int position) {
        notificationRepository.deleteNotification(notification.getId(), new NotificationRepository.OnNotificationListener() {
            @Override
            public void onSuccess(String message) {
                android.util.Log.d("NOTIFICATION", "Notification deleted: " + notification.getId());
                adapter.removeItem(position);
                Toast.makeText(getContext(), "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                android.util.Log.e("NOTIFICATION", "Failed to delete: " + error);
                Toast.makeText(getContext(), "Lỗi xóa thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAllNotificationsAsRead() {
        notificationRepository.markAllAsRead(new NotificationRepository.OnNotificationListener() {
            @Override
            public void onSuccess(String message) {
                android.util.Log.d("NOTIFICATION", "All notifications marked as read");
                Toast.makeText(getContext(), "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                android.util.Log.e("NOTIFICATION", "Failed to mark all as read: " + error);
                Toast.makeText(getContext(), "Lỗi đánh dấu đã đọc", Toast.LENGTH_SHORT).show();
            }
        });
    }
}