package com.example.little_share.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.little_share.data.models.Notification;
import com.example.little_share.data.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private static final String COLLECTION = "notifications";


    private final FirebaseFirestore db;

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    //Tạo thông báo cho tất cả volunteer khi có chiến dịch mới
    public void notifyVolunteerAboutNewCampaign(String campaignId, String campaignName, String organizationName, OnNotificationListener listener){
        db.collection("user")
                .whereEqualTo("role", "VOLUNTEER")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalVolunteer = querySnapshot.size();
                    if(totalVolunteer == 0){
                        listener.onSuccess("Không có volunteer nào để thông báo!");
                        return;
                    }

                    int[] successCount = {0};
                    int[] failCount = {0};

                    for (QueryDocumentSnapshot doc : querySnapshot){
                        String volunteerID = doc.getId();

                        Notification notification = new Notification(
                                volunteerID,
                                "Chiến dịch mới "+campaignName,
                                organizationName + " vừa tạo chiến dịch mới. Hãy tham gia ngay!",
                                Notification.NotificationType.CAMPAIGN_NEW
                        );
                        notification.setReferenceId(campaignId);

                        createNotification(notification, new OnNotificationListener() {

                            @Override
                            public void onSuccess(String result) {
                                successCount[0]++;
                                checkCompletion();
                            }

                            @Override
                            public void onFailure(String error) {
                                failCount[0]++;
                                Log.e(TAG, "Failed to notify volunteer " + volunteerID + ": " + error);
                                checkCompletion();
                            }

                            private void checkCompletion(){
                                if(successCount[0] + failCount[0] == totalVolunteer){
                                    String message = String.format("Đã gửi thông báo: %d thành công, %d thất bại", successCount[0], failCount[0]);
                                    listener.onSuccess(message);
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting volunteers: " + e.getMessage());
                    listener.onFailure("Lỗi khi lấy danh sách volunteer: " + e.getMessage());
                });
    }

    public void createNotification(Notification notification, OnNotificationListener listener) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", notification.getUserId());
        notificationData.put("title", notification.getTitle());
        notificationData.put("message", notification.getMessage());
        notificationData.put("type", notification.getType());
        notificationData.put("referenceId", notification.getReferenceId());
        notificationData.put("isRead", notification.isRead());
        notificationData.put("createdAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION)
                .add(notificationData)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Notification created with ID: " + docRef.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating notification"+ e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }

    public LiveData<List<Notification>> getNotificationsByUser(){
        MutableLiveData<List<Notification>> liveData = new MutableLiveData<>();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if(currentUserId == null){
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        db.collection(COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if(error != null){
                        Log.e(TAG, "Error listening to notifications", error);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    if(snapshots != null) {
                        List<Notification> notifications = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Notification notification = doc.toObject(Notification.class);
                            notification.setId(doc.getId());
                            notifications.add(notification);
                        }
                        liveData.setValue(notifications);
                        Log.d(TAG, "Loaded " + notifications.size() + " notifications");
                    }
                    else {
                        liveData.setValue(new ArrayList<>());
                    }
                });
        return liveData;
    }

    //Đếm số thông báo chưa đọc
    public LiveData<Integer> getUnreadCount(){
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if(currentUserId == null){
            liveData.setValue(0);
            return liveData;
        }

        db.collection(COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        liveData.setValue(0);
                        return;
                    }
                    liveData.setValue(snapshots.size());
                });
        return liveData;
    }

    public void markAsRead(String notificationId, OnNotificationListener listener){
        db.collection(COLLECTION)
                .document(notificationId)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification marked as read: "+notificationId);
                    listener.onSuccess(notificationId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to mark notification as read: "+notificationId);
                    listener.onFailure(e.getMessage());
                });
    }

    public void markAllAsRead(OnNotificationListener listener){
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if(currentUserId == null){
            listener.onFailure("User not logged in");
            return;
        }

        db.collection(COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if(querySnapshot.isEmpty()){
                        listener.onSuccess("Không có thông báo nào để đánh dấu");
                        return;
                    }

                    int[] count = {0};
                    int total = querySnapshot.size();

                    for (QueryDocumentSnapshot doc : querySnapshot){
                        doc.getReference().update("isRead", true)
                                .addOnSuccessListener(aVoid -> {
                                    count[0]++;
                                    if(count[0] == total){
                                        listener.onSuccess("Đã đánh dấu "+total+ " thông báo");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    count[0]++;
                                    if (count[0] == total){
                                        listener.onSuccess("Đã đánh dấu "+total+" thông báo");
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    //Xóa thông báo
    public void deleteNotification(String notificationId, OnNotificationListener listener) {
        db.collection(COLLECTION)
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification deleted: " + notificationId);
                    listener.onSuccess(notificationId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete notification: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }

    //XÓa thông báo đã đọc
    public void deleteAllReadNotifications(OnNotificationListener listener){
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if(currentUserId == null){
            listener.onFailure("User not logged in");
            return;
        }

        db.collection(COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if(querySnapshot.isEmpty()){
                        listener.onSuccess("Không có thông báo nào để xóa");
                        return;
                    }

                    int[] count = {0};
                    int total = querySnapshot.size();

                    for (QueryDocumentSnapshot doc : querySnapshot){
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    count[0]++;
                                    if(count[0] == total){
                                        listener.onSuccess("Đã xóa "+total+ " thông báo");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    count[0]++;
                                    if(count[0] == total){
                                        listener.onSuccess("Đã xóa thông báo");
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));

    }


    public interface OnNotificationListener {
        void onSuccess(String result);
        void onFailure(String error);
    }


}
