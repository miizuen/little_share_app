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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Notification;
import com.example.little_share.data.models.VolunteerRegistration;
import com.example.little_share.ui.volunteer.adapter.NotificationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class frm_volunteer_notification extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotification;
    private NotificationAdapter adapter;
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


        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(requireContext(), this);
        rvNotification.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotification.setAdapter(adapter);
    }

    private void setupClickListeners() {

    }

    private void loadNotifications() {
        String currentUserId = getCurrentUserId();

        if (currentUserId == null) {
            showEmptyState();
            return;
        }

        db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Notification> notifications = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                Notification notification = doc.toObject(Notification.class);
                                notification.setId(doc.getId());
                                notifications.add(notification);
                            } catch (Exception ex) {
                                // Skip invalid notifications
                            }
                        }

                        adapter.setNotifications(notifications);

                        if (notifications.isEmpty()) {
                            showEmptyState();
                        }
                    }
                });
    }

    private void observeUnreadCount() {
        String currentUserId = getCurrentUserId();

        if (currentUserId == null) {
                       return;
        }

        db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {

                        return;
                    }

                    int unreadCount = queryDocumentSnapshots != null ? queryDocumentSnapshots.size() : 0;

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {


                            if (unreadCount == 0) {


                            }
                        });
                    }
                });
    }

    private String getCurrentUserId() {
        // Firebase Auth
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        com.google.firebase.auth.FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }

        // SharedPreferences user_prefs
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId != null) return userId;

        // SharedPreferences login_prefs
        android.content.SharedPreferences loginPrefs = requireContext().getSharedPreferences("login_prefs", android.content.Context.MODE_PRIVATE);
        return loginPrefs.getString("user_id", null);
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Đánh dấu là đã đọc
        if (!notification.isRead()) {
            markNotificationAsRead(notification.getId());
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

                case "DONATION_PENDING":
                    Toast.makeText(getContext(), "Quyên góp của bạn đang chờ xét duyệt", Toast.LENGTH_SHORT).show();
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

    private void markNotificationAsRead(String notificationId) {
        db.collection("notifications")
                .document(notificationId)
                .update("isRead", true);
    }

    @Override
    public void onDeleteClick(Notification notification, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc muốn xóa thông báo này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("notifications")
                            .document(notification.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                adapter.removeItem(position);
                                Toast.makeText(requireContext(), "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    markAllNotificationsAsRead();
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void markAllNotificationsAsRead() {
        String currentUserId = getCurrentUserId();

        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.update(doc.getReference(), "isRead", true);
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Đã đánh dấu tất cả thông báo là đã đọc", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToDonationHistory() {
        Toast.makeText(getContext(), "Đang chuyển đến lịch sử quyên góp...", Toast.LENGTH_SHORT).show();
    }

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
                });
    }

    private void navigateToCampaignDetail(String campaignId) {
        Toast.makeText(requireContext(), "Mở chi tiết chiến dịch: " + campaignId, Toast.LENGTH_SHORT).show();
    }

    private void showEmptyState() {
        Toast.makeText(requireContext(), "Không có thông báo nào", Toast.LENGTH_SHORT).show();
    }
}
