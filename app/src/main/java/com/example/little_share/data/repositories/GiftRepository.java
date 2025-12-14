package com.example.little_share.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.little_share.data.models.Gift;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiftRepository {
    private static final String TAG = "GiftRepository";
    private static final String COLLECTION = "gifts";
    private static final String USER_COLLECTION = "users";
    private final FirebaseFirestore db;

    private final MutableLiveData<List<Gift>> giftsLiveData = new MutableLiveData<>();
    private ListenerRegistration giftListenerRegistration;

    public GiftRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

   public void createGift(Gift gift, OnGiftListener listener){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection(USER_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String organizationId  = userDoc.getString("organizationId");
                    if(organizationId  == null || organizationId .isEmpty()){
                        listener.onFailure("Không tìm thấy tổ chức");
                        return;
                    }

                    Map<String, Object> giftData = new HashMap<>();

                    giftData.put("name", gift.getName());
                    giftData.put("category", gift.getCategory());
                    giftData.put("description", gift.getDescription());
                    giftData.put("imageUrl", gift.getImageUrl());
                    giftData.put("pointsRequired", gift.getPointsRequired());
                    giftData.put("totalQuantity", gift.getTotalQuantity());
                    giftData.put("availableQuantity", gift.getTotalQuantity()); // Ban đầu = total
                    giftData.put("pickupLocation", gift.getPickupLocation());
                    giftData.put("organizationId", organizationId);
                    giftData.put("createdAt", FieldValue.serverTimestamp());
                    giftData.put("updatedAt", FieldValue.serverTimestamp());

                    db.collection(COLLECTION)
                            .add(giftData)
                            .addOnSuccessListener(docRef -> {
                                android.util.Log.d(TAG, "Gift created: " + docRef.getId());
                                listener.onSuccess("Thêm quà tặng thành công!");
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e(TAG, "Error creating gift: " + e.getMessage());
                                listener.onFailure(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
   }

    public LiveData<List<Gift>> getGiftsByOrganization() {

        if (giftListenerRegistration != null) {
            return giftsLiveData;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            giftsLiveData.setValue(new ArrayList<>());
            return giftsLiveData;
        }

        db.collection(USER_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String organizationId = userDoc.getString("organizationId");
                    if (organizationId == null || organizationId.isEmpty()) {
                        giftsLiveData.postValue(new ArrayList<>());
                        return;
                    }

                    giftListenerRegistration = db.collection(COLLECTION)
                            .whereEqualTo("organizationId", organizationId)
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .addSnapshotListener((snapshots, error) -> {
                                if (error != null) {
                                    android.util.Log.e(TAG, "Firestore listen error: ", error);

                                    giftsLiveData.postValue(new ArrayList<>());
                                    return;
                                }

                                List<Gift> gifts = new ArrayList<>();
                                if (snapshots != null) {
                                    for (var doc : snapshots.getDocuments()) {
                                        Gift gift = doc.toObject(Gift.class);
                                        if (gift != null) {
                                            gift.setId(doc.getId());
                                            gifts.add(gift);
                                        }
                                    }
                                }
                                android.util.Log.d(TAG, "Realtime gifts updated: " + gifts.size());
                                giftsLiveData.postValue(gifts);
                            });
                })
                .addOnFailureListener(e -> giftsLiveData.postValue(new ArrayList<>()));
        return giftsLiveData;
    }
   public void updateGift(String giftId, Map<String, Object> updates, OnGiftListener listener){
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION)
                .document(giftId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d(TAG, "Gift updated: " + giftId);
                    listener.onSuccess("Cập nhật thành công!");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Error updating gift: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
   }

    public void deleteGift(String giftId, OnGiftListener listener) {
        db.collection(COLLECTION)
                .document(giftId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d(TAG, "Gift deleted: " + giftId);
                    listener.onSuccess("Xóa quà tặng thành công!");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Error deleting gift: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }

    public void getGiftStats(OnStatsListener listener){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection(USER_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String organizationId = userDoc.getString("organizationId");
                    if(organizationId == null || organizationId.isEmpty()){
                        listener.onSuccess(0, 0, 0);
                        return;
                    }

                    db.collection(COLLECTION)
                            .whereEqualTo("organizationId", organizationId)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                int totalGifts = snapshot.size();
                                int availableGifts = 0;
                                int redeemedGifts = 0;
                                for(var doc : snapshot.getDocuments()){
                                    int available = doc.getLong("availableQuantity").intValue();
                                    int total = doc.getLong("totalQuantity").intValue();

                                    if(available > 0){
                                        availableGifts++;
                                    }

                                    redeemedGifts += (total - available);
                                }
                                listener.onSuccess(totalGifts, availableGifts, redeemedGifts);
                            })
                            .addOnFailureListener(e -> listener.onSuccess(0,0,0));
                })
                .addOnFailureListener(e -> listener.onSuccess(0,0,0));
    }

    public interface OnGiftListener {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface OnStatsListener {
        void onSuccess(int totalGifts, int availableGifts, int redeemedGifts);
    }


}
