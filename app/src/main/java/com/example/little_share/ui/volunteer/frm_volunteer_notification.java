package com.example.little_share.ui.volunteer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.little_share.R;
import com.example.little_share.adapter.NotificationAdapter;
import com.example.little_share.data.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class frm_volunteer_notification extends Fragment {

    private RecyclerView rvNotification;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList;
    private TextView tvNumberNotify;
    private LinearLayout markAllRead;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_volunteer_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo views
        initViews(view);

        // Tạo dữ liệu mẫu
        createSampleData();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Cập nhật số lượng thông báo
        updateNotificationCount();
    }

    private void initViews(View view) {
        rvNotification = view.findViewById(R.id.rvNotification);
        tvNumberNotify = view.findViewById(R.id.tvNumberNotify);
        markAllRead = view.findViewById(R.id.markAllRead);
    }

    private void createSampleData() {
        notificationList = new ArrayList<>();

        // Thêm dữ liệu mẫu
        NotificationModel notification1 = new NotificationModel(
                "Chiến dịch mới: Mùa đông ấm áp",
                "Tham gia ngay để giúp đỡ bà con vùng cao",
                "2 giờ trước",
                R.drawable.ic_megaphone
        );
        notification1.setRead(false);
        notificationList.add(notification1);

        NotificationModel notification2 = new NotificationModel(
                "Hoạt động được duyệt",
                "Hoạt động 'Tặng sách cho trẻ em' đã được duyệt",
                "5 giờ trước",
                R.drawable.ic_megaphone
        );
        notification2.setRead(false);
        notificationList.add(notification2);

        NotificationModel notification3 = new NotificationModel(
                "Lời mời tham gia",
                "Bạn được mời tham gia chiến dịch 'Trồng cây xanh'",
                "1 ngày trước",
                R.drawable.ic_megaphone
        );
        notification3.setRead(true);
        notificationList.add(notification3);

        NotificationModel notification4 = new NotificationModel(
                "Cảm ơn đóng góp",
                "Cảm ơn bạn đã tham gia chiến dịch vừa qua",
                "2 ngày trước",
                R.drawable.ic_megaphone
        );
        notification4.setRead(true);
        notificationList.add(notification4);

        NotificationModel notification5 = new NotificationModel(
                "Nhắc nhở sự kiện",
                "Sự kiện 'Ngày hội tình nguyện' sẽ diễn ra vào cuối tuần này",
                "3 ngày trước",
                R.drawable.ic_megaphone
        );
        notification5.setRead(true);
        notificationList.add(notification5);
    }

    private void setupRecyclerView() {
        // Khởi tạo adapter với constructor có sẵn
        adapter = new NotificationAdapter(notificationList);

        // Setup RecyclerView
        rvNotification.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotification.setAdapter(adapter);

        // Set delete listener
        adapter.setDeleteListener(new NotificationAdapter.OnNotificationDeleteListener() {
            @Override
            public void onDelete(int position) {
                // Xóa thông báo
                adapter.removeItem(position);
                updateNotificationCount();
                Toast.makeText(getContext(),
                        "Đã xóa thông báo",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Xử lý nút "Đánh dấu là đã đọc"
        markAllRead.setOnClickListener(v -> {
            markAllAsRead();
        });
    }

    private void markAllAsRead() {
        boolean hasUnread = false;
        for (NotificationModel notification : notificationList) {
            if (!notification.isRead()) {
                notification.setRead(true);
                hasUnread = true;
            }
        }

        if (hasUnread) {
            adapter.notifyDataSetChanged();
            updateNotificationCount();
            Toast.makeText(getContext(),
                    "Đã đánh dấu tất cả là đã đọc",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNotificationCount() {
        int unreadCount = 0;
        for (NotificationModel notification : notificationList) {
            if (!notification.isRead()) {
                unreadCount++;
            }
        }
        tvNumberNotify.setText(String.valueOf(unreadCount));
    }
}