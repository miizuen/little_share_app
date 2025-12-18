package com.example.little_share.ui.volunteer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
import com.example.little_share.data.models.Campain.CampaignRole;
import com.example.little_share.ui.volunteer.adapter.VolunteerRoleAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class activity_volunteer_role_selection extends AppCompatActivity {

    private static final String TAG = "RoleSelection";

    private RecyclerView recyclerRoles;
    private TextView tvCampaignName;
    private ImageButton btnBack;

    private VolunteerRoleAdapter adapter;
    private List<CampaignRole> roleList = new ArrayList<>();
    private FirebaseFirestore db;

    private String campaignId;
    private String campaignName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_role_selection);

        db = FirebaseFirestore.getInstance();

        initViews();
        getDataFromIntent();
        setupRecyclerView();
        loadRoles();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        recyclerRoles = findViewById(R.id.recyclerRoles);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void getDataFromIntent() {
    campaignId = getIntent().getStringExtra("campaignId");
    campaignName = getIntent().getStringExtra("campaignName");
    
    // Lấy roles từ Intent
    if (getIntent().hasExtra("roles")) {
        List<CampaignRole> roles = (List<CampaignRole>) getIntent().getSerializableExtra("roles");
        if (roles != null) {
            roleList.clear();
            roleList.addAll(roles);
        }
    }

    if (campaignName != null) {
        tvCampaignName.setText(campaignName.toUpperCase());
    }
}

    private void setupRecyclerView() {
        adapter = new VolunteerRoleAdapter(this, roleList, role -> {
            // Xử lý khi click đăng ký vai trò
            registerForRole(role);
        });

        recyclerRoles.setLayoutManager(new LinearLayoutManager(this));
        recyclerRoles.setAdapter(adapter);
    }

private void loadRoles() {
    // Nếu đã có roles từ Intent thì hiển thị luôn
    if (!roleList.isEmpty()) {
        adapter.notifyDataSetChanged();
        Log.d(TAG, "Loaded " + roleList.size() + " roles from Intent");
        return;
    }
    
    if (campaignId == null || campaignId.isEmpty()) {
        Toast.makeText(this, "Không tìm thấy thông tin chiến dịch", Toast.LENGTH_SHORT).show();
        return;
    }

    Log.d(TAG, "Querying roles for campaignId: " + campaignId);

    // Query từ collection campaign_roles
    db.collection("campaign_roles")
            .whereEqualTo("campaignId", campaignId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                roleList.clear();
                Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " roles");
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    CampaignRole role = doc.toObject(CampaignRole.class);
                    role.setId(doc.getId());
                    roleList.add(role);
                    Log.d(TAG, "Role: " + role.getRoleName());
                }
                adapter.notifyDataSetChanged();

                if (roleList.isEmpty()) {
                    // Nếu không có trong campaign_roles, thử lấy từ campaign document
                    loadRolesFromCampaignDocument();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error: " + e.getMessage());
                Toast.makeText(this, "Lỗi tải vai trò", Toast.LENGTH_SHORT).show();
            });
}

private void loadRolesFromCampaignDocument() {
    db.collection("campaigns")
            .document(campaignId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    List<Map<String, Object>> rolesData = (List<Map<String, Object>>) doc.get("roles");
                    if (rolesData != null && !rolesData.isEmpty()) {
                        roleList.clear();
                        for (Map<String, Object> roleMap : rolesData) {
                            CampaignRole role = new CampaignRole();
                            role.setRoleName((String) roleMap.get("roleName"));
                            role.setDescription((String) roleMap.get("description"));
                            role.setMaxVolunteers(((Long) roleMap.getOrDefault("maxVolunteers", 0L)).intValue());
                            role.setCurrentVolunteers(((Long) roleMap.getOrDefault("currentVolunteers", 0L)).intValue());
                            role.setPointsReward(((Long) roleMap.getOrDefault("pointsReward", 0L)).intValue());
                            roleList.add(role);
                        }
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Loaded " + roleList.size() + " roles from campaign document");
                    } else {
                        Toast.makeText(this, "Chiến dịch này chưa có vai trò", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading from campaign: " + e.getMessage());
            });
}

    private void registerForRole(CampaignRole role) {
        // TODO: Xử lý đăng ký vai trò cho tình nguyện viên
        // Ví dụ: Lưu vào collection volunteer_registrations
        Toast.makeText(this, "Đăng ký vai trò: " + role.getRoleName(), Toast.LENGTH_SHORT).show();
    }
}
