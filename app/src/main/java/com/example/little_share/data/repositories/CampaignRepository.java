package com.example.little_share.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.little_share.data.models.Campain.CampaignRegistration;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.SponsorDonation;
import com.example.little_share.data.repositories.NotificationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.little_share.data.models.Campain.CampaignRegistration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CampaignRepository {
    private static final String TAG = "CampaignRepository";
    private static final String COLLECTION = "campaigns";
    private final FirebaseFirestore db;
    private final String currentUserId;

    public LiveData<List<CampaignRegistration>> getUserRegistrationHistory() {
        MutableLiveData<List<CampaignRegistration>> liveData = new MutableLiveData<>();

        if (currentUserId == null) {
            Log.w(TAG, "Current user ID is null");
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        Log.d(TAG, "Setting up registration history listener for user: " + currentUserId);

        // FIX: Đổi từ "campaign_registrations" sang "volunteer_registrations"
        db.collection("volunteer_registrations")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", "completed") // CHỈ LẤY COMPLETED
                .orderBy("attendedAt", Query.Direction.DESCENDING) // Sort theo thời gian điểm danh
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting registrations", error);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        List<CampaignRegistration> registrations = new ArrayList<>();
                        Log.d(TAG, "Processing " + snapshots.size() + " completed registration documents");

                        for (QueryDocumentSnapshot doc : snapshots) {
                            try {
                                CampaignRegistration registration = doc.toObject(CampaignRegistration.class);
                                if (registration != null) {
                                    registration.setId(doc.getId());
                                    registrations.add(registration);

                                    Log.d(TAG, "Loaded registration: " + registration.getCampaignName() +
                                            " | Status: " + registration.getStatus() +
                                            " | Points: " + registration.getPointsEarned());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing registration document: " + doc.getId(), e);
                            }
                        }

                        Log.d(TAG, "Setting " + registrations.size() + " completed registrations to LiveData");
                        liveData.setValue(registrations);
                    } else {
                        Log.w(TAG, "No completed registration documents found");
                        liveData.setValue(new ArrayList<>());
                    }
                });

        return liveData;
    }

    public void getRegistrationStats(OnRegistrationStatsListener listener) {
        if (currentUserId == null) {
            Log.w(TAG, "Current user ID is null for stats");
            listener.onSuccess(0, 0);
            return;
        }

        Log.d(TAG, "Loading registration stats for user: " + currentUserId);

        // FIX: Đổi collection name
        db.collection("volunteer_registrations")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", "completed") // CHỈ ĐẾM COMPLETED
                .get()
                .addOnSuccessListener(snapshots -> {
                    int totalCampaigns = snapshots.size();
                    int totalPoints = 0;

                    Log.d(TAG, "Found " + totalCampaigns + " completed campaigns");

                    // Đếm unique campaigns
                    java.util.Set<String> uniqueCampaignIds = new java.util.HashSet<>();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            CampaignRegistration registration = doc.toObject(CampaignRegistration.class);
                            if (registration != null) {
                                totalPoints += registration.getPointsEarned();

                                // Thêm campaignId vào Set để đếm unique
                                if (registration.getCampaignId() != null) {
                                    uniqueCampaignIds.add(registration.getCampaignId());
                                }

                                Log.d(TAG, "Campaign: " + registration.getCampaignName() +
                                        " | Points: " + registration.getPointsEarned());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing registration for stats: " + doc.getId(), e);
                        }
                    }

                    int uniqueCampaignCount = uniqueCampaignIds.size();

                    Log.d(TAG, "Stats calculated: " + uniqueCampaignCount + " unique campaigns, " +
                            totalPoints + " points (from " + totalCampaigns + " registrations)");

                    listener.onSuccess(uniqueCampaignCount, totalPoints);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting registration stats", e);
                    listener.onSuccess(0, 0);
                });
    }

    public LiveData<List<Campaign>> getDonationCampaigns() {
        MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();

        Log.d(TAG, "Setting up getDonationCampaigns listener");

        db.collection(COLLECTION)
                .whereEqualTo("campaignType", "DONATION")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting donation campaigns", error);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    if (snapshots != null) {
                        Log.d(TAG, "Firestore returned " + snapshots.size() + " documents");

                        List<Campaign> campaigns = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Campaign campaign = doc.toObject(Campaign.class);
                            campaign.setId(doc.getId());
                            campaigns.add(campaign);

                            Log.d(TAG, "Document: " + doc.getId());
                            Log.d(TAG, "  campaignType: " + doc.getString("campaignType"));
                            Log.d(TAG, "  name: " + doc.getString("name"));
                        }

                        liveData.setValue(campaigns);
                        Log.d(TAG, "Loaded " + campaigns.size() + " donation campaigns");
                    } else {
                        Log.w(TAG, "Snapshots is null");
                        liveData.setValue(new ArrayList<>());
                    }
                });

        return liveData;
    }

    // Thêm interface mới
    public interface OnRegistrationStatsListener {
        void onSuccess(int totalCampaigns, int totalPoints);
    }

    public CampaignRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }

    public LiveData<List<Campaign>> getCampaignsByCurrentNgo() {
        MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();
        if (currentUserId == null) {
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        db.collection(COLLECTION)
                .whereEqualTo("organizationId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<Campaign> campaigns = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Campaign campaign = doc.toObject(Campaign.class);
                        campaign.setId(doc.getId());
                        campaigns.add(campaign);
                    }
                    liveData.setValue(campaigns);
                });

        return liveData;
    }

    // Tạo chiến dịch mới
    public void createCampaign(Campaign campaign, OnCampaignListener listener) {
        campaign.setOrganizationId(currentUserId);

        // Lấy tên tổ chức từ Firestore trước khi tạo
        db.collection("organization").document(currentUserId)


                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String orgName = doc.getString("name");
                        if (orgName != null && !orgName.isEmpty()) {
                            campaign.setOrganizationName(orgName);
                        }
                    }

                    // Sau đó mới tạo campaign
                    db.collection(COLLECTION)
                            .add(campaign)
                            .addOnSuccessListener(ref -> {
                                campaign.setId(ref.getId());
                                listener.onSuccess(ref.getId());
                            })
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    // Nếu lỗi vẫn tạo campaign với tên mặc định
                    db.collection(COLLECTION)
                            .add(campaign)
                            .addOnSuccessListener(ref -> {
                                campaign.setId(ref.getId());
                                listener.onSuccess(ref.getId()); // <-- Trả về campaignId thực
                            })
                            .addOnFailureListener(err -> listener.onFailure(err.getMessage()));
                });
    }

    public LiveData<List<Campaign>> getAllCampaigns() {
        MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();

        db.collection(COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting campaigns", error);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    if (snapshots != null) {
                        List<Campaign> campaigns = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Campaign campaign = doc.toObject(Campaign.class);
                            campaign.setId(doc.getId()); // Set document ID

                            // KIỂM TRA CHIẾN DỊCH ĐÃ KẾT THÚC
                            Object endDate = doc.get("endDate");
                            if (!isCampaignExpired(endDate)) {
                                // Chỉ thêm chiến dịch chưa kết thúc
                                campaigns.add(campaign);
                            } else {
                                Log.d(TAG, "Campaign expired, not showing: " + campaign.getName());
                            }
                        }
                        liveData.setValue(campaigns);
                        Log.d(TAG, "Loaded " + campaigns.size() + " active campaigns");
                    } else {
                        liveData.setValue(new ArrayList<>());
                    }
                });

        return liveData;
    }


    public LiveData<List<Campaign>> getCampaignsByCategory(String category) {
        MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();

        android.util.Log.d("REPO_FILTER", "Querying campaigns with category: " + category);

        db.collection(COLLECTION)
                .whereEqualTo("category", category)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting campaigns by category: " + category, error);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    if (snapshots != null) {
                        android.util.Log.d("REPO_FILTER", "Found " + snapshots.size() + " documents for category: " + category);

                        List<Campaign> campaigns = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Campaign campaign = doc.toObject(Campaign.class);
                            campaign.setId(doc.getId());

                            // Debug thông tin campaign
                            android.util.Log.d("REPO_FILTER", "Campaign: " + campaign.getName() + ", Category: " + campaign.getCategory());

                            // KIỂM TRA CHIẾN DỊCH ĐÃ KẾT THÚC (nếu đã implement)
                            Object endDate = doc.get("endDate");
                            if (!isCampaignExpired(endDate)) {
                                campaigns.add(campaign);
                            } else {
                                android.util.Log.d("REPO_FILTER", "Campaign expired: " + campaign.getName());
                            }
                        }
                        liveData.setValue(campaigns);
                        android.util.Log.d("REPO_FILTER", "Returning " + campaigns.size() + " active campaigns for category: " + category);
                    } else {
                        android.util.Log.w("REPO_FILTER", "No snapshots for category: " + category);
                        liveData.setValue(new ArrayList<>());
                    }
                });

        return liveData;
    }




    public LiveData<List<Campaign>> getCampaignsWithDonations() {
        MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();

        if (currentUserId == null) {
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        // Bước 1: Lấy tất cả donations của các campaigns thuộc NGO này
        db.collection("sponsorDonations")
                .whereEqualTo("status", "COMPLETED")
                .get()
                .addOnSuccessListener(donationSnapshots -> {
                    // Lấy unique campaign IDs từ donations
                    List<String> campaignIdsWithDonations = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : donationSnapshots) {
                        String campaignId = doc.getString("campaignId");
                        if (campaignId != null && !campaignIdsWithDonations.contains(campaignId)) {
                            campaignIdsWithDonations.add(campaignId);
                        }
                    }

                    if (campaignIdsWithDonations.isEmpty()) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    // Bước 2: Lấy campaigns của NGO hiện tại có trong danh sách đã được donate
                    db.collection(COLLECTION)
                            .whereEqualTo("organizationId", currentUserId)
                            .get()
                            .addOnSuccessListener(campaignSnapshots -> {
                                List<Campaign> campaigns = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : campaignSnapshots) {
                                    String campaignId = doc.getId();
                                    if (campaignIdsWithDonations.contains(campaignId)) {
                                        Campaign campaign = doc.toObject(Campaign.class);
                                        campaign.setId(campaignId);
                                        campaigns.add(campaign);
                                    }
                                }
                                liveData.setValue(campaigns);
                                Log.d(TAG, "Found " + campaigns.size() + " campaigns with donations");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting campaigns", e);
                                liveData.setValue(new ArrayList<>());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting donations", e);
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    public LiveData<List<Campaign>> getCampaignsNeedingSponsor() {
        MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();

        db.collection(COLLECTION)
                .whereEqualTo("needsSponsor", true)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting campaigns needing sponsor", error);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    if (snapshot != null) {
                        List<Campaign> campaigns = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshot) {
                            Campaign campaign = doc.toObject(Campaign.class);
                            campaign.setId(doc.getId());

                            String status = campaign.getStatus();
                            boolean isActive = "UPCOMING".equals(status) || "ONGOING".equals(status);
                            boolean needsBudget = campaign.getCurrentBudget() < campaign.getTargetBudget();

                            if (isActive && needsBudget) {
                                campaigns.add(campaign);
                                Log.d(TAG, "Added campaign: " + campaign.getName() +
                                        " - Budget: " + campaign.getCurrentBudget() + "/" + campaign.getTargetBudget());
                            } else {
                                Log.d(TAG, "Skipped campaign: " + campaign.getName() +
                                        " - Active: " + isActive + ", NeedsBudget: " + needsBudget);
                            }
                        }

                        // Sắp xếp theo độ ưu tiên (campaign thiếu budget nhiều nhất lên đầu)
                        campaigns.sort((c1, c2) -> {
                            double remaining1 = c1.getTargetBudget() - c1.getCurrentBudget();
                            double remaining2 = c2.getTargetBudget() - c2.getCurrentBudget();
                            return Double.compare(remaining2, remaining1);
                        });

                        liveData.setValue(campaigns);
                        Log.d(TAG, "Loaded " + campaigns.size() + " campaigns needing sponsor");
                    } else {
                        liveData.setValue(new ArrayList<>());
                    }
                });
        return liveData;
    }

    public LiveData<Campaign> getCampaignById(String campaignId) {
        MutableLiveData<Campaign> liveData = new MutableLiveData<>();

        db.collection(COLLECTION)
                .document(campaignId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        liveData.setValue(null);
                        return;
                    }

                    Campaign campaign = snapshot.toObject(Campaign.class);
                    liveData.setValue(campaign);
                });

        return liveData;
    }

    public LiveData<List<Campaign>> searchCampaigns(String query) {
        MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();

        String queryLower = query.toLowerCase();

        db.collection(COLLECTION)
                .orderBy("name")
                .startAt(queryLower)
                .endAt(queryLower + "\uf8ff")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<Campaign> campaigns = snapshots.toObjects(Campaign.class);
                    liveData.setValue(campaigns);
                });

        return liveData;
    }

    public void updateCampaign(String campaignId, Campaign campaign, OnCampaignListener listener) {
        db.collection(COLLECTION)
                .document(campaignId)
                .set(campaign)
                .addOnSuccessListener(aVoid -> listener.onSuccess(campaignId))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void deleteCampaign(String campaignId, OnCampaignListener listener) {
        db.collection(COLLECTION)
                .document(campaignId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess(campaignId))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void incrementVolunteers(String campaignId) {
        db.collection(COLLECTION)
                .document(campaignId)
                .update("currentVolunteers", FieldValue.increment(1));
    }

    public void addBudget(String campaignId, double amount) {
        db.collection(COLLECTION)
                .document(campaignId)
                .update("currentBudget", FieldValue.increment(amount));
    }



    public interface OnCampaignListener {
        void onSuccess(String result);

        void onFailure(String error);
    }

    public LiveData<List<Campaign>> getCampaignsBySponsor(String sponsorId) {
        MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();

        Log.d(TAG, "Getting campaigns for sponsor: " + sponsorId);

        db.collection(COLLECTION)
                .whereArrayContains("sponsorIds", sponsorId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting sponsored campaigns: " + error.getMessage());
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<Campaign> campaigns = new ArrayList<>();
                    if (snapshots != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            try {
                                Campaign campaign = doc.toObject(Campaign.class);
                                if (campaign != null) {
                                    campaign.setId(doc.getId());
                                    campaigns.add(campaign);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to Campaign: " + e.getMessage());
                            }
                        }
                    }

                    Log.d(TAG, "Found " + campaigns.size() + " sponsored campaigns");
                    liveData.setValue(campaigns);
                });

        return liveData;
    }

    public void getOrganizationNameAndCreate(Campaign campaign, OnCampaignListener listener) {
        if (currentUserId == null) {
            listener.onFailure("Chưa đăng nhập");
            return;
        }

        db.collection("organization")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    String orgName;

                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            orgName = name;
                        } else {
                            orgName = "Tổ chức từ thiện";
                        }
                    } else {
                        orgName = "Tổ chức từ thiện";
                    }

                    campaign.setOrganizationName(orgName);
                    campaign.setOrganizationId(currentUserId);

                    db.collection(COLLECTION)
                            .add(campaign)
                            .addOnSuccessListener(ref -> {
                                campaign.setId(ref.getId());

                                NotificationRepository notificationRepo = new NotificationRepository();

                                // ===== KIỂM TRA LOẠI CAMPAIGN =====
                                if ("VOLUNTEER".equals(campaign.getCampaignType())) {
                                    // Campaign tình nguyện - gửi thông báo cho volunteers
                                    notificationRepo.notifyVolunteersAboutNewCampaign(
                                            campaign.getId(),
                                            campaign.getName(),
                                            orgName,
                                            new NotificationRepository.OnNotificationListener() {
                                                @Override
                                                public void onSuccess(String message) {
                                                    Log.d(TAG, "Notifications sent to volunteers: " + message);
                                                }

                                                @Override
                                                public void onFailure(String error) {
                                                    Log.e(TAG, "Failed to send notifications: " + error);
                                                }
                                            });
                                } else if ("DONATION".equals(campaign.getCampaignType())) {
                                    // Campaign quyên góp - gửi thông báo quyên góp
                                    String donationType = campaign.getDonationTypeEnum() != null
                                            ? campaign.getDonationTypeEnum().getDisplayName()
                                            : "vật phẩm";

                                    notificationRepo.notifyVolunteersAboutNewDonationCampaign(
                                            campaign.getId(),
                                            campaign.getName(),
                                            orgName,
                                            donationType,
                                            new NotificationRepository.OnNotificationListener() {
                                                @Override
                                                public void onSuccess(String message) {
                                                    Log.d(TAG, "Donation campaign notifications sent: " + message);
                                                }

                                                @Override
                                                public void onFailure(String error) {
                                                    Log.e(TAG, "Failed to send donation notifications: " + error);
                                                }
                                            });
                                }

                                listener.onSuccess("Tạo thành công!");
                            })
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting org name: " + e.getMessage());
                    campaign.setOrganizationName("Tổ chức từ thiện");
                    campaign.setOrganizationId(currentUserId);

                    db.collection(COLLECTION)
                            .add(campaign)
                            .addOnSuccessListener(ref -> {
                                campaign.setId(ref.getId());
                                listener.onSuccess("Tạo thành công!");
                            })
                            .addOnFailureListener(err -> listener.onFailure(err.getMessage()));
                });
    }

    public LiveData<List<Campaign>> getSponsoredCampaigns() {
        MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();

        if (currentUserId == null) {
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        // Query donations của user hiện tại
        db.collection("sponsorDonations")
                .whereEqualTo("sponsorId", currentUserId)
                .orderBy("donationDate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting sponsor donations", error);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    // Lấy unique campaign IDs
                    List<String> uniqueCampaignIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String campaignId = doc.getString("campaignId");
                        if (campaignId != null && !uniqueCampaignIds.contains(campaignId)) {
                            uniqueCampaignIds.add(campaignId);
                        }
                    }

                    if (uniqueCampaignIds.isEmpty()) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    // Lấy campaign details
                    fetchCampaignDetails(uniqueCampaignIds, liveData);
                });

        return liveData;
    }

    private void fetchCampaignDetails(List<String> campaignIds, MutableLiveData<List<Campaign>> liveData) {
        List<Campaign> campaigns = new ArrayList<>();
        int[] completedQueries = { 0 };
        int totalQueries = campaignIds.size();

        for (String campaignId : campaignIds) {
            db.collection(COLLECTION)
                    .document(campaignId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Campaign campaign = doc.toObject(Campaign.class);
                            if (campaign != null) {
                                campaign.setId(doc.getId());
                                campaigns.add(campaign);
                            }
                        }

                        completedQueries[0]++;
                        if (completedQueries[0] == totalQueries) {
                            liveData.setValue(campaigns);
                            Log.d(TAG, "Loaded " + campaigns.size() + " sponsored campaigns");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting campaign: " + campaignId, e);
                        completedQueries[0]++;
                        if (completedQueries[0] == totalQueries) {
                            liveData.setValue(campaigns);
                        }
                    });
        }
    }

    // Lưu donation vào Firebase
    public void saveDonation(SponsorDonation donation, OnDonationSaveListener listener) {
        // Lưu donation vào collection "sponsorDonations"
        db.collection("sponsorDonations")
                .add(donation)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Donation saved with ID: " + documentReference.getId());

                    // Cập nhật currentBudget của campaign
                    updateCampaignBudget(donation.getCampaignId(), donation.getAmount(), listener);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving donation", e);
                    listener.onFailure("Lỗi lưu thông tin donation: " + e.getMessage());
                });
    }

    private void updateCampaignBudget(String campaignId, double donationAmount, OnDonationSaveListener listener) {
        DocumentReference campaignRef = db.collection("campaigns").document(campaignId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(campaignRef);
            double currentBudget = snapshot.getDouble("currentBudget");
            double newBudget = currentBudget + donationAmount;

            transaction.update(campaignRef, "currentBudget", newBudget);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Campaign budget updated successfully");
            listener.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error updating campaign budget", e);
            listener.onFailure("Lỗi cập nhật budget: " + e.getMessage());
        });
    }





    public interface OnDonationSaveListener {
        void onSuccess();

        void onFailure(String error);
    }

    // Lấy tổng số tiền đã donate cho một campaign cụ thể
    public void getTotalDonationForCampaign(String campaignId, OnDonationAmountListener listener) {
        if (currentUserId == null) {
            listener.onResult(0.0);
            return;
        }

        db.collection("sponsorDonations")
                .whereEqualTo("sponsorId", currentUserId)
                .whereEqualTo("campaignId", campaignId)
                .whereEqualTo("status", "COMPLETED")
                .get()
                .addOnSuccessListener(snapshots -> {
                    double totalAmount = 0.0;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Double amount = doc.getDouble("amount");
                        if (amount != null) {
                            totalAmount += amount;
                        }
                    }
                    listener.onResult(totalAmount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting donation amount", e);
                    listener.onResult(0.0);
                });
    }

    public interface OnDonationAmountListener {
        void onResult(double amount);
    }

    // THÊM METHOD MỚI CHO ĐIỂM DANH
    public void confirmAttendance(String registrationId, String userId, String campaignId,
                                  OnAttendanceListener listener) {

        android.util.Log.d("ATTENDANCE", "=== CONFIRMING ATTENDANCE ===");

        // Bước 1: Kiểm tra registration
        db.collection("volunteer_registrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        listener.onFailure("Không tìm thấy thông tin đăng ký");
                        return;
                    }

                    String status = doc.getString("status");
                    Long attendedAt = doc.getLong("attendedAt");
                    String regUserId = doc.getString("userId");
                    String regCampaignId = doc.getString("campaignId");
                    String shiftId = doc.getString("shiftId"); // LẤY SHIFT ID

                    // Validate
                    if (!"approved".equals(status)) {
                        listener.onFailure("Đăng ký chưa được duyệt");
                        return;
                    }

                    if (attendedAt != null) {
                        listener.onFailure("Đã điểm danh trước đó");
                        return;
                    }

                    if (!userId.equals(regUserId) || !campaignId.equals(regCampaignId)) {
                        listener.onFailure("QR code không hợp lệ");
                        return;
                    }

                    // Bước 2: Lấy điểm từ campaign
                    getCampaignPoints(campaignId, points -> {
                        // Bước 3: Cập nhật attendance + cộng điểm + INCREMENT SHIFT
                        updateAttendanceAndPoints(registrationId, userId, shiftId, points, listener);
                    });
                })
                .addOnFailureListener(e -> {
                    listener.onFailure("Lỗi truy vấn: " + e.getMessage());
                });
    }


    // Method phụ: Lấy điểm từ campaign
    private void getCampaignPoints(String campaignId, OnPointsListener pointsListener) {
        db.collection("campaigns")
                .document(campaignId)
                .get()
                .addOnSuccessListener(doc -> {
                    int points = 0;
                    if (doc.exists()) {
                        Long pointsReward = doc.getLong("pointsReward");
                        if (pointsReward != null) {
                            points = pointsReward.intValue();
                        }
                    }
                    android.util.Log.d("ATTENDANCE", "Campaign points: " + points);
                    pointsListener.onPointsReceived(points);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ATTENDANCE", "Error getting points: " + e.getMessage());
                    pointsListener.onPointsReceived(0); // Fallback: 0 điểm
                });
    }

    // Method phụ: Cập nhật attendance và cộng điểm
    private void updateAttendanceAndPoints(String registrationId, String userId, String shiftId,
                                           int points, OnAttendanceListener listener) {

        // Cập nhật registration status
        java.util.Map<String, Object> regUpdates = new java.util.HashMap<>();
        regUpdates.put("status", "completed");
        regUpdates.put("attendedAt", System.currentTimeMillis());
        regUpdates.put("isAttended", true);
        regUpdates.put("pointsEarned", points);

        db.collection("volunteer_registrations")
                .document(registrationId)
                .update(regUpdates)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("ATTENDANCE", "✓ Registration updated to completed");

                    // Cộng điểm vào user
                    if (points > 0) {
                        addPointsToUser(userId, points, listener);
                    }

                    // ===== THÊM: Tăng currentVolunteers của shift =====
                    if (shiftId != null && !shiftId.isEmpty()) {
                        incrementShiftVolunteers(shiftId);
                    }

                    listener.onSuccess("Điểm danh thành công! Đã cộng " + points + " điểm");
                })
                .addOnFailureListener(e -> {
                    listener.onFailure("Lỗi cập nhật đăng ký: " + e.getMessage());
                });
    }

    private void incrementShiftVolunteers(String shiftId) {
        android.util.Log.d("SHIFT_UPDATE", "Incrementing volunteers for shift: " + shiftId);

        db.collection("shifts")
                .document(shiftId)
                .update("currentVolunteers", com.google.firebase.firestore.FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("SHIFT_UPDATE", "✓ Shift currentVolunteers incremented");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("SHIFT_UPDATE", "✗ Failed to increment shift: " + e.getMessage());
                });
    }

    public void decrementShiftVolunteers(String shiftId, OnSimpleCallback callback) {
        if (shiftId == null || shiftId.isEmpty()) {
            if (callback != null) callback.onComplete();
            return;
        }

        android.util.Log.d("SHIFT_UPDATE", "Decrementing volunteers for shift: " + shiftId);

        db.collection("shifts")
                .document(shiftId)
                .update("currentVolunteers", com.google.firebase.firestore.FieldValue.increment(-1))
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("SHIFT_UPDATE", "✓ Shift currentVolunteers decremented");
                    if (callback != null) callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("SHIFT_UPDATE", "✗ Failed to decrement shift: " + e.getMessage());
                    if (callback != null) callback.onComplete();
                });
    }

    // Method phụ: Cộng điểm vào user
    private void addPointsToUser(String userId, int points, OnAttendanceListener listener) {
        android.util.Log.d("ATTENDANCE", "Adding " + points + " points to user: " + userId);

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    int currentPoints = 0;
                    if (doc.exists()) {
                        Long existingPoints = doc.getLong("totalPoints");
                        if (existingPoints != null) {
                            currentPoints = existingPoints.intValue();
                        }
                    }

                    int newPoints = currentPoints + points;
                    android.util.Log.d("ATTENDANCE",
                            "Current points: " + currentPoints + " → New points: " + newPoints);

                    // Cập nhật điểm
                    db.collection("users")
                            .document(userId)
                            .update("totalPoints", newPoints)
                            .addOnSuccessListener(aVoid -> {
                                android.util.Log.d("ATTENDANCE", "✓ Points added successfully");
                                listener.onSuccess("Điểm danh thành công! Đã cộng " + points + " điểm");
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("ATTENDANCE", "✗ Failed to add points: " + e.getMessage());
                                listener.onSuccess("Điểm danh thành công nhưng lỗi cộng điểm");
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ATTENDANCE", "✗ Failed to get user points: " + e.getMessage());
                    listener.onSuccess("Điểm danh thành công nhưng lỗi cộng điểm");
                });
    }

    // Interface phụ
    private interface OnPointsListener {
        void onPointsReceived(int points);
    }

    public interface OnAttendanceListener {
        void onSuccess(String message);

        void onFailure(String error);
    }

    // Lấy tổng số tiền đã tài trợ
    public LiveData<Double> getTotalDonationAmount() {
        MutableLiveData<Double> liveData = new MutableLiveData<>();

        if (currentUserId == null) {
            liveData.setValue(0.0);
            return liveData;
        }

        db.collection("sponsorDonations")
                .whereEqualTo("sponsorId", currentUserId)
                .whereEqualTo("status", "COMPLETED")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting total donation amount", error);
                        liveData.setValue(0.0);
                        return;
                    }

                    double totalAmount = 0.0;
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Double amount = doc.getDouble("amount");
                            if (amount != null) {
                                totalAmount += amount;
                            }
                        }
                    }

                    liveData.setValue(totalAmount);
                    Log.d(TAG, "Total donation amount: " + totalAmount);
                });

        return liveData;
    }

    // Lấy số lượng chiến dịch đã tài trợ (unique campaigns)
    public LiveData<Integer> getTotalSponsoredCampaignsCount() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();

        if (currentUserId == null) {
            liveData.setValue(0);
            return liveData;
        }

        db.collection("sponsorDonations")
                .whereEqualTo("sponsorId", currentUserId)
                .whereEqualTo("status", "COMPLETED")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting campaigns count", error);
                        liveData.setValue(0);
                        return;
                    }

                    Set<String> uniqueCampaignIds = new HashSet<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String campaignId = doc.getString("campaignId");
                            if (campaignId != null) {
                                uniqueCampaignIds.add(campaignId);
                            }
                        }
                    }

                    int count = uniqueCampaignIds.size();
                    liveData.setValue(count);
                    Log.d(TAG, "Total sponsored campaigns count: " + count);
                });

        return liveData;
    }

    // Lấy danh sách donations cho campaign cụ thể
    public LiveData<List<SponsorDonation>> getDonationsForCampaign(String campaignId) {
        MutableLiveData<List<SponsorDonation>> liveData = new MutableLiveData<>();

        db.collection("sponsorDonations")
                .whereEqualTo("campaignId", campaignId)
                .whereEqualTo("status", "COMPLETED")
                .orderBy("donationDate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting donations for campaign", error);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<SponsorDonation> donations = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            SponsorDonation donation = doc.toObject(SponsorDonation.class);
                            donations.add(donation);
                        }
                    }
                    liveData.setValue(donations);
                    Log.d(TAG, "Loaded " + donations.size() + " donations for campaign: " + campaignId);
                });

        return liveData;
    }

    // Lấy tổng tiền đã gây được cho campaign
    public LiveData<Double> getTotalRaisedForCampaign(String campaignId) {
        MutableLiveData<Double> liveData = new MutableLiveData<>();

        db.collection("sponsorDonations")
                .whereEqualTo("campaignId", campaignId)
                .whereEqualTo("status", "COMPLETED")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting total raised for campaign", error);
                        liveData.setValue(0.0);
                        return;
                    }

                    double total = 0.0;
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Double amount = doc.getDouble("amount");
                            if (amount != null) {
                                total += amount;
                            }
                        }
                    }
                    liveData.setValue(total);
                    Log.d(TAG, "Total raised for campaign " + campaignId + ": " + total);
                });

        return liveData;
    }
    private boolean isCampaignExpired(Object endDateObj) {
        if (endDateObj == null) {
            return false; // Nếu không có ngày kết thúc, coi như chưa hết hạn
        }

        try {
            Date campaignEndDate = null;
            Date currentDate = new Date();

            // Xử lý các kiểu dữ liệu khác nhau
            if (endDateObj instanceof String) {
                // Nếu là String, parse theo format
                String endDateStr = (String) endDateObj;
                if (endDateStr.isEmpty()) {
                    return false;
                }
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                campaignEndDate = sdf.parse(endDateStr);
            } else if (endDateObj instanceof com.google.firebase.Timestamp) {
                // Nếu là Firestore Timestamp
                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) endDateObj;
                campaignEndDate = timestamp.toDate();
            } else if (endDateObj instanceof Date) {
                // Nếu là Date
                campaignEndDate = (Date) endDateObj;
            } else {
                // Kiểu không hỗ trợ, coi như chưa hết hạn
                Log.w(TAG, "Unsupported endDate type: " + endDateObj.getClass().getSimpleName());
                return false;
            }

            if (campaignEndDate != null) {
                // So sánh ngày hiện tại với ngày kết thúc
                boolean expired = currentDate.after(campaignEndDate);
                Log.d(TAG, "Campaign end date: " + campaignEndDate + ", current: " + currentDate + ", expired: " + expired);
                return expired;
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing endDate: " + e.getMessage());
            e.printStackTrace();
            return false; // Nếu có lỗi parse, coi như chưa hết hạn
        }
    }

    public interface OnSimpleCallback {
        void onComplete();
    }

}