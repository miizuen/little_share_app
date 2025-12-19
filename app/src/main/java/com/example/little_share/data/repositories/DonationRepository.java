package com.example.little_share.data.repositories;

import android.util.Log;

import com.example.little_share.data.models.Donation;
import com.example.little_share.data.models.DonationItem;
import com.example.little_share.data.models.User;
import com.example.little_share.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonationRepository {
    private static final String TAG = "DonationRepository";
    private static final String DONATIONS_COLLECTION = "donations";
    private static final String DONATION_ITEMS_COLLECTION = "donationItems";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public DonationRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // ========== CREATE VOLUNTEER DONATION ==========
    public void createVolunteerDonation(String organizationId, String organizationName,
                                        Donation.DonationType type, List<DonationItem> items,
                                        OnDonationListener listener) {

        String currentUserId = auth.getCurrentUser().getUid();

        // Get current user info first
        db.collection(USERS_COLLECTION).document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        listener.onFailure("Không tìm thấy thông tin người dùng");
                        return;
                    }

                    User user = userDoc.toObject(User.class);
                    if (user == null || user.getRole() != User.UserRole.VOLUNTEER) {
                        listener.onFailure("Chỉ tình nguyện viên mới có thể quyên góp");
                        return;
                    }

                    // Calculate total points
                    int totalPoints = calculateTotalPoints(items);

                    // Create donation object
                    Donation donation = new Donation();
                    donation.setUserId(currentUserId);
                    donation.setUserName(user.getFullName());
                    donation.setType(type.name());
                    donation.setOrganizationId(organizationId);
                    donation.setOrganizationName(organizationName);
                    donation.setStatus(Donation.DonationStatus.PENDING.name());
                    donation.setPointsEarned(totalPoints);
                    donation.setDonationDate(new Date());

                    // Use batch write for atomic operation
                    WriteBatch batch = db.batch();

                    // Add donation document
                    DocumentReference donationRef = db.collection(DONATIONS_COLLECTION).document();
                    Map<String, Object> donationData = createDonationMap(donation);
                    batch.set(donationRef, donationData);

                    // Add donation items
                    for (DonationItem item : items) {
                        item.setDonationId(donationRef.getId());
                        DocumentReference itemRef = db.collection(DONATION_ITEMS_COLLECTION).document();
                        Map<String, Object> itemData = createDonationItemMap(item);
                        batch.set(itemRef, itemData);
                    }

                    // Update user totalDonations
                    DocumentReference userRef = db.collection(USERS_COLLECTION).document(currentUserId);
                    batch.update(userRef, "totalDonations", FieldValue.increment(1));
                    batch.update(userRef, "updatedAt", FieldValue.serverTimestamp());

                    // Commit batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Donation created successfully: " + donationRef.getId());
                                listener.onSuccess(donationRef.getId());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating donation: " + e.getMessage());
                                listener.onFailure("Lỗi tạo quyên góp: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user info: " + e.getMessage());
                    listener.onFailure("Lỗi lấy thông tin người dùng: " + e.getMessage());
                });
    }

    // ========== GET VOLUNTEER DONATIONS ==========
    public void getVolunteerDonations(OnDonationListListener listener) {
        String currentUserId = auth.getCurrentUser().getUid();

        db.collection(DONATIONS_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .orderBy("donationDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Donation> donations = querySnapshot.toObjects(Donation.class);
                    Log.d(TAG, "Retrieved " + donations.size() + " donations for user: " + currentUserId);
                    listener.onSuccess(donations);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting donations: " + e.getMessage());
                    listener.onFailure("Lỗi lấy danh sách quyên góp: " + e.getMessage());
                });
    }

    // ========== GET DONATION ITEMS ==========
    public void getDonationItems(String donationId, OnDonationItemsListener listener) {
        db.collection(DONATION_ITEMS_COLLECTION)
                .whereEqualTo("donationId", donationId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DonationItem> items = querySnapshot.toObjects(DonationItem.class);
                    Log.d(TAG, "Retrieved " + items.size() + " items for donation: " + donationId);
                    listener.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting donation items: " + e.getMessage());
                    listener.onFailure("Lỗi lấy chi tiết quyên góp: " + e.getMessage());
                });
    }

    // ========== CONFIRM DONATION (NGO ONLY) ==========
    public void confirmDonation(String donationId, int finalPoints, OnDonationListener listener) {
        db.collection(DONATIONS_COLLECTION).document(donationId)
                .get()
                .addOnSuccessListener(donationDoc -> {
                    if (!donationDoc.exists()) {
                        listener.onFailure("Không tìm thấy quyên góp");
                        return;
                    }

                    Donation donation = donationDoc.toObject(Donation.class);
                    if (donation == null) {
                        listener.onFailure("Lỗi đọc dữ liệu quyên góp");
                        return;
                    }

                    // Use batch for atomic update
                    WriteBatch batch = db.batch();

                    // Update donation status and points
                    DocumentReference donationRef = db.collection(DONATIONS_COLLECTION).document(donationId);
                    batch.update(donationRef, "status", Donation.DonationStatus.CONFIRMED.name());
                    batch.update(donationRef, "pointsEarned", finalPoints);

                    // Update user points
                    DocumentReference userRef = db.collection(USERS_COLLECTION).document(donation.getUserId());
                    batch.update(userRef, "totalPoints", FieldValue.increment(finalPoints));
                    batch.update(userRef, "updatedAt", FieldValue.serverTimestamp());

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Donation confirmed successfully: " + donationId);
                                listener.onSuccess(donationId);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error confirming donation: " + e.getMessage());
                                listener.onFailure("Lỗi xác nhận quyên góp: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting donation: " + e.getMessage());
                    listener.onFailure("Lỗi lấy thông tin quyên góp: " + e.getMessage());
                });
    }

    // ========== UPDATE DONATION STATUS ==========
    public void updateDonationStatus(String donationId, Donation.DonationStatus status, OnDonationListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status.name());
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(DONATIONS_COLLECTION).document(donationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Donation status updated: " + donationId + " -> " + status.name());
                    listener.onSuccess(donationId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating donation status: " + e.getMessage());
                    listener.onFailure("Lỗi cập nhật trạng thái: " + e.getMessage());
                });
    }

    // ========== GET DONATIONS FOR NGO ==========
    public void getDonationsForOrganization(String organizationId, OnDonationListListener listener) {
        db.collection(DONATIONS_COLLECTION)
                .whereEqualTo("organizationId", organizationId)
                .orderBy("donationDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Donation> donations = querySnapshot.toObjects(Donation.class);
                    Log.d(TAG, "Retrieved " + donations.size() + " donations for org: " + organizationId);
                    listener.onSuccess(donations);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting org donations: " + e.getMessage());
                    listener.onFailure("Lỗi lấy danh sách quyên góp: " + e.getMessage());
                });
    }

    // ========== HELPER METHODS ==========
    private int calculateTotalPoints(List<DonationItem> items) {
        int totalPoints = 0;
        for (DonationItem item : items) {
            int itemPoints = 0;
            switch (item.getCondition()) {
                case NEW:
                    itemPoints = Constants.POINTS_PER_ITEM_NEW;
                    break;
                case GOOD:
                    itemPoints = Constants.POINTS_PER_ITEM_GOOD;
                    break;
                case FAIR:
                    itemPoints = Constants.POINTS_PER_ITEM_FAIR;
                    break;
                case ACCEPTABLE:
                    itemPoints = Constants.POINTS_PER_ITEM_ACCEPTABLE;
                    break;
            }
            totalPoints += itemPoints * item.getQuantity();
        }
        return totalPoints;
    }

    private Map<String, Object> createDonationMap(Donation donation) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", donation.getUserId());
        data.put("userName", donation.getUserName());
        data.put("type", donation.getType());
        data.put("organizationId", donation.getOrganizationId());
        data.put("organizationName", donation.getOrganizationName());
        data.put("status", donation.getStatus());
        data.put("pointsEarned", donation.getPointsEarned());
        data.put("donationDate", donation.getDonationDate());
        data.put("createdAt", FieldValue.serverTimestamp());
        return data;
    }

    private Map<String, Object> createDonationItemMap(DonationItem item) {
        Map<String, Object> data = new HashMap<>();
        data.put("donationId", item.getDonationId());
        data.put("category", item.getCategory());
        data.put("quantity", item.getQuantity());
        data.put("condition", item.getCondition().name());
        data.put("notes", item.getNotes());
        return data;
    }

    // ========== CALLBACK INTERFACES ==========
    public interface OnDonationListener {
        void onSuccess(String donationId);
        void onFailure(String error);
    }

    public interface OnDonationListListener {
        void onSuccess(List<Donation> donations);
        void onFailure(String error);
    }

    public interface OnDonationItemsListener {
        void onSuccess(List<DonationItem> items);
        void onFailure(String error);
    }
}
