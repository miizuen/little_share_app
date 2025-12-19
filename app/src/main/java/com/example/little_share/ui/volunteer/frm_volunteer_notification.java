package com.example.little_share.ui.volunteer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.adapter.NotificationAdapter;
import com.example.little_share.data.models.Notification;
import com.example.little_share.data.models.NotificationModel;
import com.example.little_share.data.repositories.NotificationRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class frm_volunteer_notification extends Fragment {

    private static final String TAG = "VolunteerNotification";

    // Views - SỬ DỤNG ID ĐÚNG TRONG frm_volunteer_notification.xml
    private RecyclerView rvNotification;
    private TextView tvNumberNotify;
    private LinearLayout markAllRead;

    // Data & Repository
    private NotificationRepository notificationRepository;
    private NotificationAdapter adapter;
    private List<Notification> originalNotifications;
    private List<NotificationModel> notificationModels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_volunteer_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initRepository();
        setupRecyclerView();
        setupClickListeners();
        loadNotifications();
    }

    private void initViews(View view) {
        // SỬ DỤNG ID ĐÚNG TỪ XML
        rvNotification = view.findViewById(R.id.rvNotification);
        tvNumberNotify = view.findViewById(R.id.tvNumberNotify);
        markAllRead = view.findViewById(R.id.markAllRead);
    }

    private void initRepository() {
        notificationRepository = new NotificationRepository();
        originalNotifications = new ArrayList<>();
        notificationModels = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        rvNotification.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotification.setAdapter(adapter);

        // Handle delete click
        adapter.setDeleteListener(new NotificationAdapter.OnNotificationDeleteListener() {
            @Override
            public void onDelete(int position) {
                deleteNotificationAtPosition(position);
            }
        });

        // Handle item click
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < originalNotifications.size()) {
                    handleNotificationClick(originalNotifications.get(position), position);
                }
            }
        });
    }

    private void setupClickListeners() {
        // Handle "Đánh dấu là đã đọc" button
        if (markAllRead != null) {
            markAllRead.setOnClickListener(v -> {
                Log.d(TAG, "Mark all as read clicked");
                markAllAsRead();
            });
        }
    }

    private void loadNotifications() {
        Log.d(TAG, "Loading notifications using NotificationRepository...");

        // Load notifications using NotificationRepository
        notificationRepository.getNotificationsByUser().observe(getViewLifecycleOwner(), new Observer<List<Notification>>() {
            @Override
            public void onChanged(List<Notification> notifications) {
                if (notifications != null) {
                    Log.d(TAG, "Received " + notifications.size() + " notifications");

                    // Filter notifications for volunteers
                    List<Notification> volunteerNotifications = filterVolunteerNotifications(notifications);

                    // Store original notifications
                    originalNotifications.clear();
                    originalNotifications.addAll(volunteerNotifications);

                    // Convert to NotificationModel for adapter
                    notificationModels.clear();
                    for (Notification notification : volunteerNotifications) {
                        NotificationModel model = convertToNotificationModel(notification);
                        notificationModels.add(model);
                    }

                    // Update adapter
                    adapter.setNotificationList(notificationModels);

                    // Update notification count
                    updateNotificationCount(volunteerNotifications.size());
                } else {
                    Log.w(TAG, "Received null notifications");
                    originalNotifications.clear();
                    notificationModels.clear();
                    adapter.setNotificationList(notificationModels);
                    updateNotificationCount(0);
                }
            }
        });

        // Observe unread count
        notificationRepository.getUnreadCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer unreadCount) {
                Log.d(TAG, "Unread count: " + unreadCount);
                // Update the number display
                if (tvNumberNotify != null) {
                    tvNumberNotify.setText(String.valueOf(unreadCount));
                }
            }
        });
    }

    private List<Notification> filterVolunteerNotifications(List<Notification> allNotifications) {
        List<Notification> volunteerNotifications = new ArrayList<>();
        for (Notification notification : allNotifications) {
            Notification.NotificationType type = notification.getTypeEnum();
            // Filter notifications relevant to volunteers
            if (type == Notification.NotificationType.CAMPAIGN_NEW ||
                    type == Notification.NotificationType.CAMPAIGN_REMINDER ||
                    type == Notification.NotificationType.DONATION_CONFIRMED ||
                    type == Notification.NotificationType.GIFT_AVAILABLE ||
                    type == Notification.NotificationType.GENERAL) {
                volunteerNotifications.add(notification);
            }
        }
        return volunteerNotifications;
    }

    private NotificationModel convertToNotificationModel(Notification notification) {
        String timeText = "--";
        if (notification.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            timeText = sdf.format(notification.getCreatedAt());
        }

        int iconResId = getIconForNotificationType(notification.getTypeEnum());

        NotificationModel model = new NotificationModel(
                notification.getTitle(),
                notification.getMessage(),
                timeText,
                iconResId
        );

        model.setRead(notification.isRead());
        return model;
    }

    private int getIconForNotificationType(Notification.NotificationType type) {
        switch (type) {
            case CAMPAIGN_NEW:
                return R.drawable.ic_campaign;
            case CAMPAIGN_REMINDER:
                return R.drawable.ic_notification;
            case DONATION_CONFIRMED:
                return R.drawable.ic_gift;
            case GIFT_AVAILABLE:
                return R.drawable.ic_gift;
            case GENERAL:
            default:
                return R.drawable.ic_notification;
        }
    }

    private void updateNotificationCount(int count) {
        if (tvNumberNotify != null) {
            tvNumberNotify.setText(String.valueOf(count));
        }
    }

    private void handleNotificationClick(Notification notification, int position) {
        Log.d(TAG, "Notification clicked: " + notification.getTitle());

        // Mark as read if not already read
        if (!notification.isRead()) {
            markNotificationAsRead(notification, position);
        }

        // Show notification details
        String message = notification.getMessage();
        if (notification.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            message += "\n\nThời gian: " + sdf.format(notification.getCreatedAt());
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(notification.getTitle())
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .setNeutralButton("Đánh dấu đã đọc", (dialog, which) -> {
                    if (!notification.isRead()) {
                        markNotificationAsRead(notification, position);
                    }
                })
                .show();
    }

    private void markNotificationAsRead(Notification notification, int position) {
        if (notification.isRead()) return;

        Log.d(TAG, "Marking notification as read: " + notification.getId());

        notificationRepository.markAsRead(notification.getId(), new NotificationRepository.OnNotificationListener() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Notification marked as read successfully");
                notification.setRead(true);
                if (position < notificationModels.size()) {
                    notificationModels.get(position).setRead(true);
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to mark notification as read: " + error);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi đánh dấu đã đọc: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteNotificationAtPosition(int position) {
        if (position < 0 || position >= originalNotifications.size()) {
            Log.e(TAG, "Invalid position for delete: " + position);
            return;
        }

        Notification notification = originalNotifications.get(position);
        Log.d(TAG, "Deleting notification: " + notification.getId());

        notificationRepository.deleteNotification(notification.getId(), new NotificationRepository.OnNotificationListener() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Notification deleted successfully");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                }

                originalNotifications.remove(position);
                notificationModels.remove(position);
                adapter.removeItem(position);
                updateNotificationCount(originalNotifications.size());
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to delete notification: " + error);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi xóa thông báo: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Public utility method - called by button click
    private void markAllAsRead() {
        Log.d(TAG, "Marking all notifications as read");

        notificationRepository.markAllAsRead(new NotificationRepository.OnNotificationListener() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "All notifications marked as read: " + result);
                if (getContext() != null) {
                    Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to mark all as read: " + error);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Fragment destroyed");
    }
}
