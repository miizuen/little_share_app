package com.example.little_share.ui.volunteer;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.CampaignRegistration;
import com.example.little_share.ui.volunteer.adapter.HistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class activity_volunteer_campagin_history extends AppCompatActivity
        implements HistoryAdapter.OnHistoryItemClickListener {

    private static final String TAG = "CampaignHistory";
    private ImageView btnBack;
    private TextView tvTotalEvents, tvTotalPoints;
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_campagin_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initFirebase();
        setupRecyclerView();
        setupClickListeners();
        
        // Debug: Kiểm tra tất cả collections có thể có
        debugAllCollections();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalEvents = findViewById(R.id.tv_total_events);
        tvTotalPoints = findViewById(R.id.tv_total_points);
        rvHistory = findViewById(R.id.rv_history);
        Log.d(TAG, "Views initialized");
    }
    
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null 
            ? FirebaseAuth.getInstance().getCurrentUser().getUid() 
            : null;
            
        Log.d(TAG, "=== FIREBASE INIT ===");
        Log.d(TAG, "Current User ID: " + currentUserId);
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter(this, new ArrayList<>());
        historyAdapter.setOnHistoryItemClickListener(this);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
        Log.d(TAG, "RecyclerView setup completed");
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void debugAllCollections() {
        if (currentUserId == null) {
            Log.e(TAG, "USER NOT LOGGED IN!");
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
            return;
        }
        
        Log.d(TAG, "=== DEBUGGING ALL COLLECTIONS ===");
        
        // Danh sách tất cả collections có thể có
        String[] collections = {
            "volunteer_registrations",
            "campaign_registrations", 
            "registrations",
            "user_campaigns",
            "campaign_volunteers",
            "volunteer_campaigns"
        };
        
        for (String collection : collections) {
            checkCollection(collection);
        }
    }
    
    private void checkCollection(String collectionName) {
        Log.d(TAG, "=== CHECKING COLLECTION: " + collectionName + " ===");
        
        db.collection(collectionName)
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    Log.d(TAG, "Collection '" + collectionName + "': " + snapshots.size() + " documents");
                    
                    if (!snapshots.isEmpty()) {
                        Log.d(TAG, "*** FOUND DATA IN: " + collectionName + " ***");
                        
                        // Log chi tiết 3 documents đầu
                        int count = 0;
                        for (QueryDocumentSnapshot doc : snapshots) {
                            if (count >= 3) break;
                            
                            Log.d(TAG, "Document " + count + " ID: " + doc.getId());
                            Log.d(TAG, "All fields:");
                            for (String field : doc.getData().keySet()) {
                                Object value = doc.get(field);
                                Log.d(TAG, "  " + field + " = " + value + " (" + (value != null ? value.getClass().getSimpleName() : "null") + ")");
                            }
                            Log.d(TAG, "---");
                            count++;
                        }
                        
                        // Nếu tìm thấy data, load ngay
                        loadDataFromCollection(collectionName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking collection '" + collectionName + "'", e);
                });
    }
    
    private void loadDataFromCollection(String collectionName) {
        Log.d(TAG, "=== LOADING DATA FROM: " + collectionName + " ===");
        
        db.collection(collectionName)
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading from " + collectionName, error);
                        return;
                    }
                    
                    if (snapshots == null || snapshots.isEmpty()) {
                        Log.w(TAG, "No data in " + collectionName);
                        return;
                    }
                    
                    Log.d(TAG, "Processing " + snapshots.size() + " documents from " + collectionName);
                    
                    List<CampaignRegistration> registrations = new ArrayList<>();
                    int totalPoints = 0;
                    
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            CampaignRegistration registration = new CampaignRegistration();
                            registration.setId(doc.getId());
                            
                            // Lấy tất cả fields có thể có
                            registration.setUserId(getStringField(doc, "userId", "user_id"));
                            registration.setCampaignId(getStringField(doc, "campaignId", "campaign_id"));
                            registration.setRoleId(getStringField(doc, "roleId", "role_id"));
                            registration.setStatus(getStringField(doc, "status"));
                            registration.setQrCode(getStringField(doc, "qrCode", "qr_code"));
                            registration.setShiftTime(getStringField(doc, "shiftTime", "shift_time"));
                            registration.setNotes(getStringField(doc, "notes"));
                            
                            // Campaign name
                            String campaignName = getStringField(doc, "campaignName", "campaign_name", "name", "title");
                            if (campaignName == null || campaignName.isEmpty()) {
                                campaignName = "Chiến dịch #" + (registrations.size() + 1);
                            }
                            registration.setCampaignName(campaignName);
                            
                            // Role name
                            String roleName = getStringField(doc, "roleName", "role_name", "role", "position");
                            if (roleName == null || roleName.isEmpty()) {
                                roleName = "Tình nguyện viên";
                            }
                            registration.setRoleName(roleName);
                            
                            // User name
                            registration.setUserName(getStringField(doc, "userName", "user_name"));
                            
                            // Dates
                            if (doc.getTimestamp("createdAt") != null) {
                                registration.setCreatedAt(doc.getTimestamp("createdAt").toDate());
                            }
                            if (doc.getTimestamp("registrationDate") != null) {
                                registration.setRegistrationDate(doc.getTimestamp("registrationDate").toDate());
                            }
                            if (doc.getTimestamp("workDate") != null) {
                                registration.setWorkDate(doc.getTimestamp("workDate").toDate());
                            }
                            
                            // Points
                            Long points = getLongField(doc, "pointsEarned", "points", "pointsReward", "points_earned", "reward_points");
                            if (points != null) {
                                registration.setPointsEarned(points.intValue());
                                totalPoints += points.intValue();
                            }
                            
                            registrations.add(registration);
                            
                            Log.d(TAG, "Created registration: " + campaignName + " | " + roleName + " | " + points + " points");
                            
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing document: " + doc.getId(), e);
                        }
                    }
                    
                    // Update UI
                    Log.d(TAG, "=== UPDATING UI ===");
                    Log.d(TAG, "Total campaigns: " + registrations.size());
                    Log.d(TAG, "Total points: " + totalPoints);
                    
                    tvTotalEvents.setText(String.valueOf(registrations.size()));
                    tvTotalPoints.setText(String.valueOf(totalPoints));
                    
                    if (!registrations.isEmpty()) {
                        historyAdapter.updateData(registrations);
                        Log.d(TAG, "Adapter updated with " + registrations.size() + " items");
                    } else {
                        Log.w(TAG, "No registrations to display");
                        Toast.makeText(this, "Không có dữ liệu để hiển thị", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    // Helper methods để lấy field với nhiều tên khác nhau
    private String getStringField(QueryDocumentSnapshot doc, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = doc.getString(fieldName);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }
    
    private Long getLongField(QueryDocumentSnapshot doc, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Long value = doc.getLong(fieldName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    public void onItemClick(CampaignRegistration registration) {
        Log.d(TAG, "Item clicked: " + registration.getCampaignName());
        Toast.makeText(this, "Chi tiết: " + registration.getCampaignName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onQRCodeClick(CampaignRegistration registration) {
        Log.d(TAG, "QR clicked: " + registration.getCampaignName());
        if (registration.getQrCode() != null && !registration.getQrCode().isEmpty()) {
            Toast.makeText(this, "QR: " + registration.getQrCode(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Chưa có mã QR", Toast.LENGTH_SHORT).show();
        }
    }
}