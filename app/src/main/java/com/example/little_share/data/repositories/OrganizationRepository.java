package com.example.little_share.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.little_share.data.models.Organization;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OrganizationRepository {
    private static final String COLLECTION = "organization";
    private static final String USER_COLLECTION = "users";
    private final FirebaseFirestore db;

    public OrganizationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ========== CREATE ORGANIZATION (Dùng khi đăng ký NGO) ==========
    public void createOrganization(String organizationId, String name, String email, String phone, String address, OnCreateOrgListener listener) {
        Map<String, Object> orgData = new HashMap<>();
        orgData.put("name", name);
        orgData.put("email", email);
        orgData.put("phone", phone);
        orgData.put("address", address);
        orgData.put("logo", "");
        orgData.put("description", "Tổ chức từ thiện " + name);
        orgData.put("totalVolunteers", 0);
        orgData.put("totalCampaigns", 0);
        orgData.put("totalPointsGiven", 0);
        orgData.put("totalSponsors", 0);
        orgData.put("isActive", true);
        orgData.put("createdAt", FieldValue.serverTimestamp());
        orgData.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION)
                .document(organizationId)
                .set(orgData)
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess(organizationId);
                })
                .addOnFailureListener(e -> {
                    listener.onFailure(e.getMessage());
                });
    }

    // ========== GET ORGANIZATION BY ID ==========
    public LiveData<Organization> getOrganizationById(String orgId) {
        MutableLiveData<Organization> liveData = new MutableLiveData<>();

        db.collection(COLLECTION)
                .document(orgId)
                .addSnapshotListener((snapshot, error) -> {
                    if(error != null){
                        liveData.setValue(null);
                        return;
                    }

                    if(snapshot != null && snapshot.exists()){
                        Organization organization = snapshot.toObject(Organization.class);
                        if(organization != null){
                            organization.setId(snapshot.getId());
                            liveData.setValue(organization);
                        }
                    } else {
                        liveData.setValue(null);
                    }
                });
        return liveData;
    }

    // ========== GET CURRENT USER'S ORGANIZATION ==========
    public void getCurrentOrganization(OnOrganizationListener listener) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        android.util.Log.d("OrgRepository", "Getting org for userId: " + currentUserId);

        // Bước 1: Lấy organizationId từ user document
        db.collection(USER_COLLECTION)
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        android.util.Log.e("OrgRepository", "User document NOT FOUND!");
                        listener.onFailure("Không tìm thấy thông tin người dùng");
                        return;
                    }

                    android.util.Log.d("OrgRepository", "User document found");
                    String organizationId = documentSnapshot.getString("organizationId");
                    android.util.Log.d("OrgRepository", "organizationId from user: " + organizationId);

                    if (organizationId == null || organizationId.isEmpty()) {
                        android.util.Log.e("OrgRepository", "organizationId is NULL or EMPTY!");
                        listener.onFailure("Người dùng chưa thuộc tổ chức nào");
                        return;
                    }

                    // Bước 2: Lấy thông tin organization
                    android.util.Log.d("OrgRepository", "Fetching org document: " + organizationId);
                    db.collection(COLLECTION)
                            .document(organizationId)
                            .get()
                            .addOnSuccessListener(orgSnapshot -> {
                                if (!orgSnapshot.exists()) {
                                    android.util.Log.e("OrgRepository", "Organization document NOT FOUND!");
                                    listener.onFailure("Không tìm thấy tổ chức");
                                    return;
                                }

                                android.util.Log.d("OrgRepository", "Organization document found");
                                Organization organization = orgSnapshot.toObject(Organization.class);
                                if (organization != null) {
                                    organization.setId(orgSnapshot.getId());
                                    android.util.Log.d("OrgRepository", "Organization loaded: " + organization.getName());
                                    listener.onSuccess(organization);
                                } else {
                                    android.util.Log.e("OrgRepository", "Failed to convert to Organization object");
                                    listener.onFailure("Lỗi chuyển đổi dữ liệu tổ chức");
                                }
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("OrgRepository", "Error fetching org: " + e.getMessage());
                                listener.onFailure(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("OrgRepository", "Error fetching user: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }

    public void updateOrganizationProfile(String organizationId, Map<String, Object> updates, OnUpdateListener listener) {
        // Add timestamp
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION)
                .document(organizationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("OrgRepository", "Organization profile updated successfully");
                    if (listener != null) listener.onSuccess(organizationId);
                })
                .addOnFailureListener(e -> {
                    Log.e("OrgRepository", "Failed to update organization profile: " + e.getMessage());
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    // ========== CALLBACKS ==========
    public interface OnCreateOrgListener {
        void onSuccess(String organizationId);
        void onFailure(String error);
    }

    public interface OnOrganizationListener {
        void onSuccess(Organization organization);
        void onFailure(String error);
    }

    public interface OnUpdateListener {
        void onSuccess(String organizationId);
        void onFailure(String error);
    }

}