package com.example.little_share.ui.sponsor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.adapter.NotificationAdapter;
import com.example.little_share.data.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class frm_sponsor_notification extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView tvNumberNotify;
    private LinearLayout markAllRead;
    private List<NotificationModel> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sponsor_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadNotifications();
        setupClickListeners();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        tvNumberNotify = view.findViewById(R.id.tvNumberNotify);
        markAllRead = view.findViewById(R.id.markAllRead);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Xử lý sự kiện xóa thông báo
        adapter.setDeleteListener(position -> {
            adapter.removeItem(position);
            updateNotificationCount();
        });
    }

    private void loadNotifications() {
        notificationList = new ArrayList<>();

        // Thêm dữ liệu mẫu
        notificationList.add(new NotificationModel(
                "Chiến dịch mới: Mùa đông ấm áp",
                "Tham gia ngay để giúp đỡ bà con vùng cao",
                "2 giờ trước",
                R.drawable.ic_megaphone
        ));

        notificationList.add(new NotificationModel(
                "Cập nhật: Dự án Ánh sáng học đường",
                "Đã quyên góp được 80% mục tiêu",
                "5 giờ trước",
                R.drawable.ic_megaphone
        ));

        notificationList.add(new NotificationModel(
                "Cảm ơn: Đóng góp thành công",
                "Cảm ơn bạn đã đóng góp 500,000đ",
                "1 ngày trước",
                R.drawable.ic_megaphone
        ));

        notificationList.add(new NotificationModel(
                "Nhắc nhở: Chiến dịch sắp kết thúc",
                "Chiến dịch 'Học bổng tương lai' sắp đóng",
                "2 ngày trước",
                R.drawable.ic_megaphone
        ));

        notificationList.add(new NotificationModel(
                "Hoàn thành: Dự án Nước sạch",
                "Dự án đã hoàn thành và đi vào hoạt động",
                "3 ngày trước",
                R.drawable.ic_megaphone
        ));

        adapter.setNotificationList(notificationList);
        updateNotificationCount();
    }

    private void setupClickListeners() {
        markAllRead.setOnClickListener(v -> {
            // Xử lý đánh dấu tất cả đã đọc
            // Có thể clear list hoặc update trạng thái
            if (notificationList != null && !notificationList.isEmpty()) {
                notificationList.clear();
                adapter.setNotificationList(notificationList);
                updateNotificationCount();
            }
        });
    }

    private void updateNotificationCount() {
        int count = adapter.getItemCount();
        tvNumberNotify.setText(String.valueOf(count));
    }
}