package com.example.little_share.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.little_share.data.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private static final String COLLECTION = "notifications";
    private final FirebaseFirestore db;
    private final String currentUserId;

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    // Lấy tất cả notifications của user hiện tại
    public LiveData<List<Notification>> getUserNotifications() {
        MutableLiveData<List<Notification>> liveData = new MutableLiveData<>();
        
        if (currentUserId == null) {
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        db.collection(COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting notifications", error);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<Notification> notifications = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Notification notification = doc.toObject(Notification.class);
                            notification.setId(doc.getId());
                            notifications.add(notification);
                        }
                    }

                    liveData.setValue(notifications);
                    Log.d(TAG, "Loaded " + notifications.size() + " notifications");
                });

        return liveData;
    }

    // Đếm số notifications chưa đọc
    public LiveData<Integer> getUnreadCount() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        
        if (currentUserId == null) {
            liveData.setValue(0);
            return liveData;
        }

        db.collection(COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting unread count", error);
                        liveData.setValue(0);
                        return;
                    }

                    int count = snapshots != null ? snapshots.size() : 0;
                    liveData.setValue(count);
                });

        return liveData;
    }

    // Tạo notification mới
    public void createNotification(Notification notification, OnNotificationListener listener) {
        db.collection(COLLECTION)
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notification created: " + documentReference.getId());
                    if (listener != null) {
                        listener.onSuccess("Notification created");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating notification", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    // Đánh dấu notification là đã đọc
    public void markAsRead(String notificationId, OnNotificationListener listener) {
        db.collection(COLLECTION)
                .document(notificationId)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification marked as read: " + notificationId);
                    if (listener != null) {
                        listener.onSuccess("Marked as read");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking as read", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    // Đánh dấu tất cả notifications là đã đọc
    public void markAllAsRead(OnNotificationListener listener) {
        if (currentUserId == null) {
            if (listener != null) {
                listener.onFailure("User not logged in");
            }
            return;
        }

        db.collection(COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        if (listener != null) {
                            listener.onSuccess("No unread notifications");
                        }
                        return;
                    }

                    // Update all unread notifications
                    for (QueryDocumentSnapshot doc : snapshots) {
                        doc.getReference().update("isRead", true);
                    }

                    if (listener != null) {
                        listener.onSuccess("All marked as read");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking all as read", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    // Xóa notification
    public void deleteNotification(String notificationId, OnNotificationListener listener) {
        db.collection(COLLECTION)
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification deleted: " + notificationId);
                    if (listener != null) {
                        listener.onSuccess("Notification deleted");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting notification", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    // Tạo notification khi có donation thành công
    public void createDonationSuccessNotification(String campaignName, double amount) {
        if (currentUserId == null) return;

        String title = "Tài trợ thành công!";
        String description = "Bạn đã tài trợ thành công " + formatMoney(amount) + " VNĐ cho chiến dịch \"" + campaignName + "\"";
        
        Notification notification = new Notification(currentUserId, title, description, Notification.NotificationType.DONATION_SUCCESS.getValue());
        createNotification(notification, null);
    }

    // Tạo notification khi có campaign mới
    public void createNewCampaignNotification(String campaignName) {
        if (currentUserId == null) return;

        String title = "Chiến dịch mới: " + campaignName;
        String description = "Tham gia ngay để giúp đỡ cộng đồng!";
        
        Notification notification = new Notification(currentUserId, title, description, Notification.NotificationType.CAMPAIGN_NEW.getValue());
        createNotification(notification, null);
    }

    // Thông báo cho tất cả volunteers về campaign mới
    public void notifyVolunteersAboutNewCampaign(String campaignId, String campaignName, String orgName, OnNotificationListener listener) {
        // Query tất cả users có role là "volunteer"
        db.collection("users")
                .whereEqualTo("role", "volunteer")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        if (listener != null) {
                            listener.onSuccess("No volunteers found");
                        }
                        return;
                    }

                    String title = "Chiến dịch mới: " + campaignName;
                    String description = "Tổ chức " + orgName + " vừa tạo chiến dịch mới. Tham gia ngay để giúp đỡ cộng đồng!";
                    
                    int totalUsers = snapshots.size();
                    int[] completedCount = {0};

                    // Tạo notification cho từng volunteer
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshots) {
                        String userId = doc.getId();
                        
                        Notification notification = new Notification(userId, title, description, 
                            Notification.NotificationType.CAMPAIGN_NEW.getValue(), campaignId);
                        
                        createNotification(notification, new OnNotificationListener() {
                            @Override
                            public void onSuccess(String message) {
                                completedCount[0]++;
                                if (completedCount[0] == totalUsers && listener != null) {
                                    listener.onSuccess("Notified " + totalUsers + " volunteers");
                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                completedCount[0]++;
                                Log.e(TAG, "Failed to notify user " + userId + ": " + error);
                                if (completedCount[0] == totalUsers && listener != null) {
                                    listener.onSuccess("Completed with some errors");
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting volunteers", e);
                    if (listener != null) {
                        listener.onFailure("Error getting volunteers: " + e.getMessage());
                    }
                });
    }

    private String formatMoney(double amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.0fK", amount / 1000);
        } else {
            return String.format("%.0f", amount);
        }
    }

    // Tạo mock notifications để test
    public void createMockNotifications() {
        if (currentUserId == null) return;

        List<Notification> mockNotifications = new ArrayList<>();
        
        // Notification 1: Donation success
        Notification n1 = new Notification(currentUserId, 
            "Tài trợ thành công!", 
            "Bạn đã tài trợ thành công 500.000 VNĐ cho chiến dịch \"Nấu ăn cho em\"", 
            Notification.NotificationType.DONATION_SUCCESS.getValue());
        mockNotifications.add(n1);
        
        // Notification 2: New campaign
        Notification n2 = new Notification(currentUserId, 
            "Chiến dịch mới: Mùa đông ấm áp", 
            "Tham gia ngay để giúp đỡ bà con vùng cao", 
            Notification.NotificationType.CAMPAIGN_NEW.getValue());
        mockNotifications.add(n2);
        
        // Notification 3: Campaign update
        Notification n3 = new Notification(currentUserId, 
            "Cập nhật chiến dịch", 
            "Chiến dịch \"Trồng cây xanh\" đã đạt 80% mục tiêu!", 
            Notification.NotificationType.CAMPAIGN_UPDATE.getValue());
        mockNotifications.add(n3);
        
        // Notification 4: System notification
        Notification n4 = new Notification(currentUserId, 
            "Chào mừng đến với Little Share!", 
            "Cảm ơn bạn đã tham gia cộng đồng chia sẻ yêu thương", 
            Notification.NotificationType.SYSTEM.getValue());
        mockNotifications.add(n4);
        
        // Notification 5: Sponsorship success
        Notification n5 = new Notification(currentUserId, 
            "Tài trợ được chấp nhận!", 
            "Chiến dịch \"Nấu ăn cho em\" đã chấp nhận khoản tài trợ của bạn", 
            Notification.NotificationType.SPONSORSHIP_SUCCESS.getValue());
        mockNotifications.add(n5);

        // Create all mock notifications
        for (Notification notification : mockNotifications) {
            createNotification(notification, null);
        }
    }

    // Thông báo khi campaign được approve
    public void createCampaignApprovedNotification(String campaignId, String campaignName, String userId) {
        String title = "Chiến dịch được duyệt!";
        String description = "Chiến dịch \"" + campaignName + "\" của bạn đã được phê duyệt và có thể bắt đầu nhận tình nguyện viên.";
        
        Notification notification = new Notification(userId, title, description, 
            Notification.NotificationType.CAMPAIGN_APPROVED.getValue(), campaignId);
        createNotification(notification, null);
    }

    // Thông báo reminder cho volunteers
    public void createCampaignReminderNotification(String campaignId, String campaignName, String userId) {
        String title = "Nhắc nhở chiến dịch";
        String description = "Chiến dịch \"" + campaignName + "\" sẽ bắt đầu trong 24 giờ tới. Hãy chuẩn bị sẵn sàng!";
        
        Notification notification = new Notification(userId, title, description, 
            Notification.NotificationType.CAMPAIGN_REMINDER.getValue(), campaignId);
        createNotification(notification, null);
    }

    // Thông báo khi có donation được confirm
    public void createDonationConfirmedNotification(String campaignName, double amount, String userId) {
        String title = "Donation được xác nhận";
        String description = "Khoản donation " + formatMoney(amount) + " VNĐ cho chiến dịch \"" + campaignName + "\" đã được xác nhận.";
        
        Notification notification = new Notification(userId, title, description, 
            Notification.NotificationType.DONATION_CONFIRMED.getValue());
        createNotification(notification, null);
    }

    // Thông báo khi có gift available
    public void createGiftAvailableNotification(String giftName, String userId) {
        String title = "Quà tặng mới!";
        String description = "Bạn có thể đổi quà tặng \"" + giftName + "\" với điểm tích lũy của mình.";
        
        Notification notification = new Notification(userId, title, description, 
            Notification.NotificationType.GIFT_AVAILABLE.getValue());
        createNotification(notification, null);
    }

    // Thông báo sponsorship success
    public void createSponsorshipSuccessNotification(String campaignName, String userId) {
        String title = "Tài trợ được chấp nhận!";
        String description = "Chiến dịch \"" + campaignName + "\" đã chấp nhận khoản tài trợ của bạn. Cảm ơn sự đóng góp!";
        
        Notification notification = new Notification(userId, title, description, 
            Notification.NotificationType.SPONSORSHIP_SUCCESS.getValue());
        createNotification(notification, null);
    }

    public interface OnNotificationListener {
        void onSuccess(String message);
        void onFailure(String error);
    }
}