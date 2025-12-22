package com.example.little_share.ui.ngo;

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
import com.example.little_share.ui.volunteer.adapter.NotificationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class frm_ngo_notification extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotification;
    private TextView tvNumberNotify;
    private LinearLayout markAllRead;

    private NotificationAdapter adapter;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_ngo_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        rvNotification = view.findViewById(R.id.rvNotification);
        tvNumberNotify = view.findViewById(R.id.tvNumberNotify);
        markAllRead = view.findViewById(R.id.markAllRead);

        // Setup RecyclerView
        adapter = new NotificationAdapter(requireContext(), this);
        rvNotification.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotification.setAdapter(adapter);

        // Setup click
        markAllRead.setOnClickListener(v -> markAllAsRead());

        // Load notifications
        loadNotifications();
    }

    private void loadNotifications() {
        if (currentUserId == null) {
            tvNumberNotify.setText("0");
            return;
        }

        android.util.Log.d("NGO_NOTIF", "Loading notifications for: " + currentUserId);

        // Query đơn giản không có orderBy (tránh lỗi index)
        db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;

                    List<Notification> notifications = new ArrayList<>();
                    int unreadCount = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Notification notification = doc.toObject(Notification.class);
                            notification.setId(doc.getId());
                            notifications.add(notification);

                            if (!notification.isRead()) {
                                unreadCount++;
                            }
                        } catch (Exception e) {
                            android.util.Log.e("NGO_NOTIF", "Error parsing: " + e.getMessage());
                        }
                    }

                    android.util.Log.d("NGO_NOTIF", "Found " + notifications.size() + " notifications");

                    adapter.setNotifications(notifications);
                    tvNumberNotify.setText(String.valueOf(unreadCount));
                    markAllRead.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("NGO_NOTIF", "Error: " + e.getMessage());
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Đánh dấu đã đọc
        if (!notification.isRead()) {
            db.collection("notifications")
                    .document(notification.getId())
                    .update("isRead", true);
        }

        // Hiển thị chi tiết
        new AlertDialog.Builder(requireContext())
                .setTitle(notification.getTitle())
                .setMessage(notification.getMessage())
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    public void onDeleteClick(Notification notification, int position) {
        db.collection("notifications")
                .document(notification.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        loadNotifications(); // Reload
                        Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markAllAsRead() {
        if (currentUserId == null) return;

        db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        doc.getReference().update("isRead", true);
                    }
                    if (isAdded()) {
                        loadNotifications();
                        Toast.makeText(getContext(), "Đã đánh dấu tất cả đã đọc", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
