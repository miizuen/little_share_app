package com.example.little_share.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.little_share.data.models.Sponsor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.invoke.MutableCallSite;
import java.util.HashMap;
import java.util.Map;

public class SponsorRepository {
    private static final String COLLECTION = "sponsors";
    private static final String USER_COLLECTION = "users";
    private final FirebaseFirestore db;

    public SponsorRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void createSponsor(String sponsorId, String name, String email, String phone, String address, OnCreateSponsorListener listener) {
        Map<String, Object> sponsorData = new HashMap<>();
        sponsorData.put("name", name);
        sponsorData.put("email", email);
        sponsorData.put("phone", phone);
        sponsorData.put("address", address);
        sponsorData.put("avatar", "");
        sponsorData.put("totalDonated", 0.0);
        sponsorData.put("totalCampaignsSponsored", 0);
        sponsorData.put("createdAt", FieldValue.serverTimestamp());
        sponsorData.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION)
                .document(sponsorId)
                .set(sponsorData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SponsorRepository", "Sponsor created successfully: " + sponsorId);
                    listener.onSuccess(sponsorId);
                })
                .addOnFailureListener(e -> {
                    Log.e("SponsorRepository", "Failed to create sponsor: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }

    public LiveData<Sponsor> getSponsorById(String sponsorId) {
        MutableLiveData<Sponsor> liveData = new MutableLiveData<>();

        db.collection(COLLECTION)
                .document(sponsorId)
                .addSnapshotListener((snapshot, error) -> {
                    if(error != null){
                        Log.e("SponsorRepository", "Error listening to sponsor: " + error.getMessage());
                        liveData.setValue(null);
                        return;
                    }

                    if(snapshot != null && snapshot.exists()){
                        Sponsor sponsor = snapshot.toObject(Sponsor.class);
                        if(sponsor != null){
                            sponsor.setId(snapshot.getId());
                            liveData.setValue(sponsor);
                        }
                    } else {
                        liveData.setValue(null);
                    }
                });
        return liveData;
    }

    public void getCurrentSponsor(OnSponsorListener listener) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("SponsorRepository", "Getting sponsor for userId: " + currentUserId);

        // Bước 1: Lấy sponsorId từ user document
        db.collection(USER_COLLECTION)
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.e("SponsorRepository", "User document NOT FOUND!");
                        listener.onFailure("Không tìm thấy thông tin người dùng");
                        return;
                    }

                    Log.d("SponsorRepository", "User document found");
                    String sponsorId = documentSnapshot.getString("sponsorId");
                    Log.d("SponsorRepository", "sponsorId from user: " + sponsorId);

                    if (sponsorId == null || sponsorId.isEmpty()) {
                        Log.e("SponsorRepository", "sponsorId is NULL or EMPTY!");
                        listener.onFailure("Người dùng chưa có thông tin nhà tài trợ");
                        return;
                    }

                    // Bước 2: Lấy thông tin sponsor
                    Log.d("SponsorRepository", "Fetching sponsor document: " + sponsorId);
                    db.collection(COLLECTION)
                            .document(sponsorId)
                            .get()
                            .addOnSuccessListener(sponsorSnapshot -> {
                                if (!sponsorSnapshot.exists()) {
                                    Log.e("SponsorRepository", "Sponsor document NOT FOUND!");
                                    listener.onFailure("Không tìm thấy thông tin nhà tài trợ");
                                    return;
                                }

                                Log.d("SponsorRepository", "Sponsor document found");
                                Sponsor sponsor = sponsorSnapshot.toObject(Sponsor.class);
                                if (sponsor != null) {
                                    sponsor.setId(sponsorSnapshot.getId());
                                    Log.d("SponsorRepository", "Sponsor loaded: " + sponsor.getName());
                                    listener.onSuccess(sponsor);
                                } else {
                                    Log.e("SponsorRepository", "Failed to convert to Sponsor object");
                                    listener.onFailure("Lỗi chuyển đổi dữ liệu nhà tài trợ");
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("SponsorRepository", "Error fetching sponsor: " + e.getMessage());
                                listener.onFailure(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("SponsorRepository", "Error fetching user: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }
    // ========== UPDATE SPONSOR PROFILE ==========
    public void updateSponsorProfile(String sponsorId, Map<String, Object> updates, OnUpdateListener listener) {
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION)
                .document(sponsorId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SponsorRepository", "Sponsor profile updated successfully");
                    if (listener != null) listener.onSuccess(sponsorId);
                })
                .addOnFailureListener(e -> {
                    Log.e("SponsorRepository", "Failed to update sponsor profile: " + e.getMessage());
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }


    public interface OnSponsorListener {
        void onSuccess(Sponsor sponsor);
        void onFailure(String error);
    }

    public interface OnUpdateListener {
        void onSuccess(String sponsorId);
        void onFailure(String error);
    }

    public interface OnCreateSponsorListener {
        void onSuccess(String sponsorId);
        void onFailure(String error);
    }
}
