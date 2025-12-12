package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Notification;
import com.example.little_share.data.repositories.NotificationRepository;
import com.example.little_share.ui.volunteer.adapter.NotificationAdapter;

import java.util.List;

public class frm_volunteer_notification extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotification;
    private TextView tvNumberNotify;
    private LinearLayout markAllRead;

    private NotificationAdapter adapter;
    private NotificationRepository repository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_volunteer_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadNotifications();
        observeUnreadCount();
    }

    private void initViews(View view) {
        rvNotification = view.findViewById(R.id.rvNotification);
        tvNumberNotify = view.findViewById(R.id.tvNumberNotify);
        markAllRead = view.findViewById(R.id.markAllRead);

        repository = new NotificationRepository();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(requireContext(), this);
        rvNotification.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotification.setAdapter(adapter);
    }

    private void setupClickListeners() {
        markAllRead.setOnClickListener(v -> showMarkAllReadDialog());
    }

    private void loadNotifications() {
        repository.getNotificationsByUser().observe(getViewLifecycleOwner(), new Observer<List<Notification>>() {
            @Override
            public void onChanged(List<Notification> notifications) {
                if (notifications != null) {
                    adapter.setNotifications(notifications);

                    // Show empty state if needed
                    if (notifications.isEmpty()) {
                        showEmptyState();
                    }
                }
            }
        });
    }

    private void observeUnreadCount() {
        repository.getUnreadCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                tvNumberNotify.setText(String.valueOf(count));

                // Hide mark all read button if no unread
                if (count == 0) {
                    markAllRead.setVisibility(View.GONE);
                } else {
                    markAllRead.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Đánh dấu là đã đọc
        if (!notification.isRead()) {
            repository.markAsRead(notification.getId(), new NotificationRepository.OnNotificationListener() {
                @Override
                public void onSuccess(String result) {
                    // Notification đã được cập nhật qua LiveData
                }

                @Override
                public void onFailure(String error) {
                    // Log error nhưng không hiện toast
                    android.util.Log.e("Notification", "Failed to mark as read: " + error);
                }
            });
        }

        // Navigate đến campaign detail nếu có referenceId
        if (notification.getReferenceId() != null && !notification.getReferenceId().isEmpty()) {
            navigateToCampaignDetail(notification.getReferenceId());
        }
    }

    @Override
    public void onDeleteClick(Notification notification, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc muốn xóa thông báo này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    repository.deleteNotification(notification.getId(), new NotificationRepository.OnNotificationListener() {
                        @Override
                        public void onSuccess(String result) {
                            adapter.removeItem(position);
                            Toast.makeText(requireContext(), "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showMarkAllReadDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đánh dấu tất cả đã đọc")
                .setMessage("Bạn có muốn đánh dấu tất cả thông báo là đã đọc?")
                .setPositiveButton("Có", (dialog, which) -> {
                    repository.markAllAsRead(new NotificationRepository.OnNotificationListener() {
                        @Override
                        public void onSuccess(String result) {
                            Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void navigateToCampaignDetail(String campaignId) {
        // TODO: Implement navigation to campaign detail
        // Example:
        // Intent intent = new Intent(requireContext(), CampaignDetailActivity.class);
        // intent.putExtra("campaignId", campaignId);
        // startActivity(intent);

        Toast.makeText(requireContext(), "Mở chi tiết chiến dịch: " + campaignId, Toast.LENGTH_SHORT).show();
    }

    private void showEmptyState() {
        // TODO: Show empty state view
        Toast.makeText(requireContext(), "Không có thông báo nào", Toast.LENGTH_SHORT).show();
    }
}