package com.example.little_share.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.little_share.data.models.Campain.Campaign;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CampaignRepository {
    private static final String TAG = "CampaignRepository";
    private static final String COLLECTION = "campaigns";
    private final FirebaseFirestore db;
    private final String currentUserId;


    public CampaignRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
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
        db.collection("users").document(currentUserId)
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
                                listener.onSuccess(ref.getId());  // <-- Trả về campaignId thực
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
                        campaign.setId(doc.getId());  // Set document ID
                        campaigns.add(campaign);
                    }
                    liveData.setValue(campaigns);
                    Log.d(TAG, "Loaded " + campaigns.size() + " campaigns");
                } else {
                    liveData.setValue(new ArrayList<>());
                }
            });

    return liveData;
}

public LiveData<List<Campaign>> getCampaignsByCategory(String category) {
    MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();

    db.collection(COLLECTION)
            .whereEqualTo("category", category)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error getting campaigns by category", error);
                    liveData.setValue(new ArrayList<>());
                    return;
                }

                if (snapshots != null) {
                    List<Campaign> campaigns = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Campaign campaign = doc.toObject(Campaign.class);
                        campaign.setId(doc.getId());  // Set document ID
                        campaigns.add(campaign);
                    }
                    liveData.setValue(campaigns);
                } else {
                    liveData.setValue(new ArrayList<>());
                }
            });

    return liveData;
}


    public LiveData<List<Campaign>> getCampaignsNeedingSponsor(){
        MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();

        db.collection(COLLECTION)
                .whereEqualTo("needsSponsor", true)
                .addSnapshotListener((snapshot, error) -> {
                    if(error != null){
                        Log.e(TAG, "Error getting campaigns needing sponsor", error);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    if(snapshot != null){
                        List<Campaign> campaigns = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshot){
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

    public void insertMockCampaigns(OnCampaignListener listener) {
        List<Campaign> mockCampaigns = createMockData();

        int[] successCount = {0};
        int totalCampaigns = mockCampaigns.size();

        for (Campaign campaign : mockCampaigns) {
            db.collection(COLLECTION)
                    .add(campaign)
                    .addOnSuccessListener(docRef -> {
                        successCount[0]++;
                        Log.d(TAG, "Mock campaign inserted: " + docRef.getId());

                        if (successCount[0] == totalCampaigns) {
                            listener.onSuccess("Inserted " + totalCampaigns + " campaigns");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error inserting mock campaign", e);
                    });
        }
    }

    private List<Campaign> createMockData() {
        List<Campaign> campaigns = new ArrayList<>();

        // Campaign 1: Nấu ăn cho em
        Campaign c1 = new Campaign();
        c1.setName("Nấu ăn cho em");
        c1.setCategory(Campaign.CampaignCategory.FOOD.name());
        c1.setOrganizationName("Nhóm tình nguyện niềm tin");
        c1.setLocation("Cao Bằng");
        c1.setSpecificLocation("Trường Tiểu học Mường Nhé");
        c1.setStartDate(new Date(2025 - 1900, 9, 12)); // Oct 12, 2025
        c1.setEndDate(new Date(2025 - 1900, 11, 30)); // Dec 30, 2025
        c1.setMaxVolunteers(60);
        c1.setCurrentVolunteers(44);
        c1.setPointsReward(50);
        c1.setStatus(Campaign.CampaignStatus.ONGOING.name());
        c1.setDescription("Cung cấp bữa ăn dinh dưỡng cho trẻ em vùng cao");
        c1.setActivities("Nấu 200 suất cơm mỗi ngày");
        campaigns.add(c1);

        // Campaign 2: Mùa đông ấm áp
        Campaign c2 = new Campaign();
        c2.setName("Mùa đông ấm áp");
        c2.setCategory(Campaign.CampaignCategory.EDUCATION.name());
        c2.setOrganizationName("Tổ chức từ thiện ABC");
        c2.setLocation("Hà Giang");
        c2.setSpecificLocation("Xã Lũng Cú");
        c2.setStartDate(new Date(2025 - 1900, 10, 15)); // Nov 15, 2025
        c2.setEndDate(new Date(2025 - 1900, 11, 20)); // Dec 20, 2025
        c2.setMaxVolunteers(50);
        c2.setCurrentVolunteers(23);
        c2.setPointsReward(80);
        c2.setStatus(Campaign.CampaignStatus.UPCOMING.name());
        c2.setDescription("Trao quần áo ấm và sách vở cho học sinh");
        campaigns.add(c2);

        // Campaign 3: Trồng cây xanh
        Campaign c3 = new Campaign();
        c3.setName("Trồng cây xanh");
        c3.setCategory(Campaign.CampaignCategory.ENVIRONMENT.name());
        c3.setOrganizationName("Nhóm môi trường xanh");
        c3.setLocation("Đà Nẵng");
        c3.setSpecificLocation("Bãi biển Mỹ Khê");
        c3.setStartDate(new Date(2025 - 1900, 10, 1)); // Nov 1, 2025
        c3.setEndDate(new Date(2025 - 1900, 10, 30)); // Nov 30, 2025
        c3.setMaxVolunteers(100);
        c3.setCurrentVolunteers(85);
        c3.setPointsReward(60);
        c3.setStatus(Campaign.CampaignStatus.ONGOING.name());
        c3.setDescription("Trồng 500 cây xanh ven biển");
        campaigns.add(c3);

        // Campaign 4: Khám bệnh miễn phí
        Campaign c4 = new Campaign();
        c4.setName("Khám bệnh miễn phí");
        c4.setCategory(Campaign.CampaignCategory.HEALTH.name());
        c4.setOrganizationName("Bệnh viện Từ Dũ");
        c4.setLocation("TP. Hồ Chí Minh");
        c4.setSpecificLocation("Quận 1");
        c4.setStartDate(new Date(2025 - 1900, 10, 10)); // Nov 10, 2025
        c4.setEndDate(new Date(2025 - 1900, 10, 12)); // Nov 12, 2025
        c4.setMaxVolunteers(30);
        c4.setCurrentVolunteers(15);
        c4.setPointsReward(100);
        c4.setStatus(Campaign.CampaignStatus.UPCOMING.name());
        c4.setDescription("Khám và phát thuốc miễn phí cho người nghèo");
        campaigns.add(c4);

        // Campaign 5: Cứu trợ lũ lụt
        Campaign c5 = new Campaign();
        c5.setName("Cứu trợ lũ lụt khẩn cấp");
        c5.setCategory(Campaign.CampaignCategory.URGENT.name());
        c5.setOrganizationName("Hội Chữ thập đỏ");
        c5.setLocation("Quảng Bình");
        c5.setSpecificLocation("Huyện Lệ Thủy");
        c5.setStartDate(new Date(2025 - 1900, 9, 20)); // Oct 20, 2025
        c5.setEndDate(new Date(2025 - 1900, 9, 25)); // Oct 25, 2025
        c5.setMaxVolunteers(80);
        c5.setCurrentVolunteers(32);
        c5.setPointsReward(120);
        c5.setStatus(Campaign.CampaignStatus.ONGOING.name());
        c5.setDescription("Hỗ trợ người dân vùng lũ lụt");
        campaigns.add(c5);

        return campaigns;
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

        // Lấy tên tổ chức từ collection "organization"
        db.collection("organization")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    String orgName = "Tổ chức từ thiện";

                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            orgName = name;
                        }
                    }

                    // Set tên tổ chức
                    campaign.setOrganizationName(orgName);
                    campaign.setOrganizationId(currentUserId);

                    // Tạo campaign
                    db.collection(COLLECTION)
                            .add(campaign)
                            .addOnSuccessListener(ref -> {
                                campaign.setId(ref.getId());
                                listener.onSuccess("Tạo thành công!");
                            })
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting org name: " + e.getMessage());
                    // Nếu lỗi vẫn tạo với tên mặc định
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
        int[] completedQueries = {0};
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

}
