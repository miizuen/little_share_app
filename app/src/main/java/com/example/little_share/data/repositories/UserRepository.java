package com.example.little_share.data.repositories;

import android.util.Log;

import com.example.little_share.data.models.User;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String USER_COLLECTION = "users";

    private final FirebaseFirestore db;

    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ========== CREATE USER DOCUMENT ===========
    public void createUser(String userId, User user, OnUserListener listener) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("fullName", user.getFullName());
        userData.put("role", user.getRole().name());
        userData.put("phone", user.getPhone());
        userData.put("avatar", user.getAvatar());
        userData.put("address", user.getAddress());

        if (user.getOrganizationId() != null && !user.getOrganizationId().isEmpty()) {
            userData.put("organizationId", user.getOrganizationId());
            android.util.Log.d(TAG, "Creating user with organizationId: " + user.getOrganizationId());
        } else {
            android.util.Log.w(TAG, "Creating user WITHOUT organizationId");
        }

        userData.put("totalPoints", user.getTotalPoints());
        userData.put("totalDonations", user.getTotalDonations());
        userData.put("totalCampaigns", user.getTotalCampaigns());
        userData.put("isActive", true);
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(USER_COLLECTION)
                .document(userId)
                .set(userData)
                .addOnSuccessListener(a -> {
                    android.util.Log.d(TAG, "User created successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Failed to create user: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }

    // ========== GET USER ROLE ===========
    public void getUserRole(String userId, OnGetRoleListener listener) {
        db.collection(USER_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        listener.onFailure("User not found");
                        return;
                    }
                    String roleString = doc.getString("role");
                    if (roleString == null) {
                        listener.onFailure("Role not found");
                        return;
                    }

                    User.UserRole role = User.UserRole.valueOf(roleString);
                    listener.onSuccess(role);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ========== UPDATE USER FIELDS ===========
    public void updateUser(String userId, Map<String, Object> updates, OnUserListener listener) {
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(USER_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(a -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ========== DELETE USER DOCUMENT ===========
    public void deleteUserDocument(String userId, OnUserListener listener) {
        db.collection(USER_COLLECTION)
                .document(userId)
                .delete()
                .addOnSuccessListener(a -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }


    public void getCurrentUserData(OnUserDataListener listener) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d(TAG, "Getting current user data for userId: " + userId);

        db.collection(USER_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "User document exists");

                        try {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                // Đảm bảo role được set đúng (vì enum không tự map)
                                String roleString = documentSnapshot.getString("role");
                                if (roleString != null) {
                                    user.setRole(User.UserRole.valueOf(roleString));
                                    Log.d(TAG, "User role set to: " + roleString);
                                }

                                // Đảm bảo organizationId được set đúng cho NGO
                                String orgId = documentSnapshot.getString("organizationId");
                                if (orgId != null) {
                                    user.setOrganizationId(orgId);
                                    Log.d(TAG, "User organizationId set to: " + orgId);
                                }

                                // Đảm bảo sponsorId được set đúng cho Sponsor
                                String sponsorId = documentSnapshot.getString("sponsorId");
                                if (sponsorId != null) {
                                    user.setSponsorId(sponsorId);
                                    Log.d(TAG, "User sponsorId set to: " + sponsorId);
                                }

                                listener.onSuccess(user);
                            } else {
                                Log.e(TAG, "Failed to convert document to User object");
                                listener.onFailure("Không thể chuyển đổi dữ liệu người dùng");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting document to User: " + e.getMessage());
                            listener.onFailure("Lỗi chuyển đổi dữ liệu: " + e.getMessage());
                        }
                    } else {
                        Log.e(TAG, "User document does not exist");
                        listener.onFailure("Không tìm thấy thông tin người dùng trong hệ thống");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user document: " + e.getMessage());
                    listener.onFailure("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
                });
    }

    // Callback interface cho việc lấy full user data
    public interface OnUserDataListener {
        void onSuccess(User user);
        void onFailure(String error);
    }


    // ===== CALLBACKS =====
    public interface OnUserListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnGetRoleListener {
        void onSuccess(User.UserRole role);
        void onFailure(String error);
    }
}