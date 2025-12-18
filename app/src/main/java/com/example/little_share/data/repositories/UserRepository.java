package com.example.little_share.data.repositories;

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

        db.collection(USER_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Đảm bảo role được set đúng (vì enum không tự map)
                            String roleString = documentSnapshot.getString("role");
                            if (roleString != null) {
                                user.setRole(User.UserRole.valueOf(roleString));
                            }

                            // Đảm bảo organizationId được set đúng
                            String orgId = documentSnapshot.getString("organizationId");
                            if (orgId != null) {
                                user.setOrganizationId(orgId);
                            }
                        }
                        listener.onSuccess(user);
                    } else {
                        listener.onFailure("Không tìm thấy thông tin người dùng");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Callback mới cho việc lấy full user data
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