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
import com.example.little_share.data.models.VolunteerRegistration;
import com.example.little_share.data.repositories.NotificationRepository;
import com.example.little_share.ui.volunteer.adapter.NotificationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class frm_volunteer_notification extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotification;
    private TextView tvNumberNotify;
    private LinearLayout markAllRead;

    private NotificationAdapter adapter;
    private NotificationRepository repository;
    private FirebaseFirestore db;

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
        db = FirebaseFirestore.getInstance();
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
        repository.getUserNotifications().observe(getViewLifecycleOwner(), new Observer<List<Notification>>() {
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
                    android.util.Log.e("Notification", "Failed to mark as read: " + error);
                }
            });
        }

        // Kiểm tra loại thông báo và chuyển trang tương ứng
        if (notification.getType() != null) {
            switch (notification.getType()) {
                case "REGISTRATION_APPROVED":
                    loadRegistrationAndNavigate(notification.getRelatedId());
                    break;

                case "REGISTRATION_REJECTED":
                    Toast.makeText(getContext(), "Đăng ký bị từ chối", Toast.LENGTH_SHORT).show();
                    break;

                // ===== XỬ LÝ DONATION NOTIFICATIONS =====
                case "DONATION_PENDING":
                    Toast.makeText(getContext(), "Quyên góp của bạn đang chờ xét duyệt", Toast.LENGTH_SHORT).show();
                    // Navigate đến lịch sử donation
                    navigateToDonationHistory();
                    break;

                case "DONATION_CONFIRMED":
                    Toast.makeText(getContext(), "Quyên góp đã được xác nhận!", Toast.LENGTH_SHORT).show();
                    navigateToDonationHistory();
                    break;

                case "DONATION_REJECTED":
                    Toast.makeText(getContext(), "Quyên góp bị từ chối. Vui lòng liên hệ tổ chức", Toast.LENGTH_SHORT).show();
                    navigateToDonationHistory();
                    break;

                case "DONATION_RECEIVED":
                    Toast.makeText(getContext(), "Tổ chức đã nhận đồ quyên góp của bạn", Toast.LENGTH_SHORT).show();
                    navigateToDonationHistory();
                    break;

                case "DONATION_CAMPAIGN_NEW":
                case "CAMPAIGN_NEW":
                    if (notification.getRelatedId() != null && !notification.getRelatedId().isEmpty()) {
                        navigateToCampaignDetail(notification.getRelatedId());
                    }
                    break;

                default:
                    if (notification.getRelatedId() != null && !notification.getRelatedId().isEmpty()) {
                        navigateToCampaignDetail(notification.getRelatedId());
                    }
                    break;
            }
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
    private void navigateToDonationHistory() {
        // Option 1: Nếu có Activity riêng cho donation history
        // Intent intent = new Intent(getContext(), DonationHistoryActivity.class);
        // startActivity(intent);

        // Option 2: Nếu là fragment trong bottom nav, chuyển đến tab đó
        // ((MainActivity) getActivity()).navigateToDonationTab();

        // Option 3: Tạm thời toast thông báo
        Toast.makeText(getContext(), "Đang chuyển đến lịch sử quyên góp...", Toast.LENGTH_SHORT).show();

        // TODO: Implement navigation based on your app structure
    }

    /**
     * Load registration data từ Firebase và navigate
     */
    private void loadRegistrationAndNavigate(String registrationId) {
        if (registrationId == null || registrationId.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin đăng ký", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Đang tải thông tin...", Toast.LENGTH_SHORT).show();

        db.collection("volunteer_registrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        VolunteerRegistration registration = documentSnapshot.toObject(VolunteerRegistration.class);
                        if (registration != null) {
                            registration.setId(documentSnapshot.getId());

                            Intent intent = new Intent(getContext(), activity_volunteer_detail_calendar.class);
                            intent.putExtra("registration", registration);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), "Lỗi: Không thể đọc dữ liệu đăng ký", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy thông tin đăng ký", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    android.util.Log.e("Notification", "Error loading registration", e);
                });
    }

    private void navigateToCampaignDetail(String campaignId) {
        Toast.makeText(requireContext(), "Mở chi tiết chiến dịch: " + campaignId, Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to campaign detail
    }


    private void showEmptyState() {
        Toast.makeText(requireContext(), "Không có thông báo nào", Toast.LENGTH_SHORT).show();
    }
}