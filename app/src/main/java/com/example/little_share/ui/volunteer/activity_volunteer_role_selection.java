package com.example.little_share.ui.volunteer;

import android.content.Intent;
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
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.Campain.CampaignRole;
import com.example.little_share.ui.volunteer.adapter.VolunteerRoleAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class activity_volunteer_role_selection extends AppCompatActivity {

    private static final String TAG = "RoleSelection";

    private RecyclerView recyclerRoles;
    private TextView tvCampaignName;
    private ImageButton btnBack;

    private VolunteerRoleAdapter adapter;
    private List<CampaignRole> roleList = new ArrayList<>();
    private FirebaseFirestore db;

    private Campaign campaign;
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
        campaign = (Campaign) getIntent().getSerializableExtra("campaign");

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
    if (campaignId == null || campaignId.isEmpty()) {
        Toast.makeText(this, "Không tìm thấy thông tin chiến dịch", Toast.LENGTH_SHORT).show();
        return;
    }

    Log.d(TAG, "Querying roles for campaignId: " + campaignId);

    // LUÔN query từ Firebase để lấy dữ liệu mới nhất (có maxVolunteers)
    db.collection("campaign_roles")
            .whereEqualTo("campaignId", campaignId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                roleList.clear();
                Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " roles from Firebase");
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    CampaignRole role = doc.toObject(CampaignRole.class);
                    role.setId(doc.getId());
                    roleList.add(role);
                    Log.d(TAG, "Role: " + role.getRoleName() + ", maxVol: " + role.getMaxVolunteers());
                }
                adapter.notifyDataSetChanged();

                if (roleList.isEmpty()) {
                    Toast.makeText(this, "Chiến dịch này chưa có vai trò", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error: " + e.getMessage());
                Toast.makeText(this, "Lỗi tải vai trò", Toast.LENGTH_SHORT).show();
            });
}

    private void registerForRole(CampaignRole role) {
        // Kiểm tra slot trước khi chuyển sang đăng ký
        checkRoleSlotAndProceed(role);
    }

    // THÊM METHOD: Kiểm tra slot trước khi đăng ký
    private void checkRoleSlotAndProceed(CampaignRole role) {
        android.util.Log.d("ROLE_SLOT", "Checking slot for role: " + role.getRoleName());

        // Query số lượng đã đăng ký cho vai trò này
        db.collection("volunteer_registrations")
                .whereEqualTo("roleId", role.getId())
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(snapshots -> {
                    int currentCount = snapshots.size();
                    int maxCount = role.getMaxVolunteers();

                    android.util.Log.d("ROLE_SLOT", "Current: " + currentCount + ", Max: " + maxCount);

                    if (currentCount >= maxCount && maxCount > 0) {
                        // HẾT SLOT
                        showRoleFullDialog(role, currentCount, maxCount);
                    } else {
                        // CÒN SLOT → Chuyển sang đăng ký
                        proceedToRegistration(role);
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ROLE_SLOT", "Error checking role slot: " + e.getMessage());
                    Toast.makeText(this, "Lỗi kiểm tra thông tin vai trò", Toast.LENGTH_SHORT).show();
                });
    }

    // THÊM METHOD: Hiển thị dialog khi vai trò đã đầy
    private void showRoleFullDialog(CampaignRole role, int current, int max) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Vai trò đã đầy")
                .setMessage("Rất tiếc! Vai trò \"" + role.getRoleName() + "\" đã đủ số lượng tình nguyện viên.\n\n" +
                        "Số lượng hiện tại: " + current + "/" + max + " người\n\n" +
                        "Bạn có thể chọn vai trò khác hoặc theo dõi để đăng ký khi có slot trống.")
                .setPositiveButton("Chọn vai trò khác", (dialog, which) -> {
                    dialog.dismiss();
                    // Ở lại màn hình để chọn vai trò khác
                })
                .setNegativeButton("Quay lại", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }

    // THÊM METHOD: Chuyển sang đăng ký
    private void proceedToRegistration(CampaignRole role) {
        Intent intent = new Intent(this, activity_volunteer_role_registration.class);
        intent.putExtra("role", role);
        intent.putExtra("campaignId", campaignId);
        intent.putExtra("campaignName", campaignName);
        intent.putExtra("campaign", campaign);
        startActivity(intent);
    }

}
