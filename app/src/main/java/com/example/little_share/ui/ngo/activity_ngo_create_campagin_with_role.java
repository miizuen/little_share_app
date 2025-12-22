package com.example.little_share.ui.ngo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.CampaignRole;
import com.example.little_share.ui.ngo.adapter.CampaignRoleAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class activity_ngo_create_campagin_with_role extends AppCompatActivity {

    private static final int REQUEST_ADD_ROLE = 100;
    private static final int REQUEST_EDIT_ROLE = 101;

    private MaterialToolbar toolbar;
    private RecyclerView rvRoles;
    private LinearLayout layoutEmptyState;
    private TextView tvRoleCount;
    private MaterialButton btnAddRole, btnNext;

    private CampaignRoleAdapter adapter;
    private List<CampaignRole> roleList = new ArrayList<>();

    // Data từ bước trước
    private String campaignName;
    private String campaignDescription;
    private String category;
    private String imageUrl;
    private String location;
    private String specificLocation;
    private String yeuCau;
    private String startDate;
    private String endDate;
    private boolean needsSponsor;
    private String targetBudget;
    private String budgetPurpose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_create_campagin_with_role);

        receiveDataFromPreviousStep();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        updateUI();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupButtons() {
        btnAddRole.setOnClickListener(v -> {
            Intent intent = new Intent(this, activity_ngo_campagin_add_role.class);
            startActivityForResult(intent, REQUEST_ADD_ROLE);
        });

        btnNext.setOnClickListener( v -> {
           if(roleList.isEmpty()){
               Toast.makeText(this, "Vui lòng thêm ít nhất một vai trò", Toast.LENGTH_SHORT).show();
                return;
           }
           goToShiftManager();
        });
    }

    private void goToShiftManager() {
        Intent intent = new Intent(this, acitivity_ngo_campaign_shift_list.class);

        intent.putExtra("campaignName", campaignName);
        intent.putExtra("campaignDescription", campaignDescription);
        intent.putExtra("category", category);
        intent.putExtra("imageUrl", imageUrl);
        intent.putExtra("location", location);
        intent.putExtra("specificLocation", specificLocation);
        intent.putExtra("yeuCau", yeuCau);
        intent.putExtra("startDate", startDate);
        intent.putExtra("endDate", endDate);
        intent.putExtra("needsSponsor", needsSponsor);
        intent.putExtra("targetBudget", targetBudget);
        intent.putExtra("budgetPurpose", budgetPurpose);

        intent.putExtra("roles", (Serializable) new ArrayList<>(roleList));
        startActivity(intent);
    }

    private void setupRecyclerView() {
        adapter = new CampaignRoleAdapter(this, roleList, new CampaignRoleAdapter.OnRoleActionListener() {
            @Override
            public void onEditClick(CampaignRole role, int position) {
                Intent intent = new Intent(activity_ngo_create_campagin_with_role. this, activity_ngo_campagin_add_role.class);
                intent.putExtra("role", role);
                intent.putExtra("position", position);
                startActivityForResult(intent, REQUEST_EDIT_ROLE);
            }

            @Override
            public void onDeleteClick(CampaignRole role, int position) {
                showDeleteConfirmationDialog(role, position);
            }
        });
        rvRoles.setLayoutManager(new LinearLayoutManager(this));
        rvRoles.setAdapter(adapter);
    }

    private void showDeleteConfirmationDialog(CampaignRole role, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa vai trò")
                .setMessage("Bạn có muốn xóa vai trò này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    roleList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateUI();
                    Toast.makeText(this, "Đã xóa vai trò", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateUI() {
        if(roleList.isEmpty()){
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvRoles.setVisibility(View.GONE);
            btnNext.setEnabled(false);
            tvRoleCount.setText("0 vai trò");
        }else {
            layoutEmptyState.setVisibility(View.GONE);
            rvRoles.setVisibility(View.VISIBLE);
            btnNext.setEnabled(true);
            tvRoleCount.setText(roleList.size() + " vai trò");
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void receiveDataFromPreviousStep() {
        Intent intent = getIntent();
        campaignName = intent.getStringExtra("campaignName");
        campaignDescription = intent.getStringExtra("campaignDescription");
        category = intent.getStringExtra("category");
        imageUrl = intent.getStringExtra("imageUrl");
        location = intent.getStringExtra("location");
        specificLocation = intent.getStringExtra("specificLocation");
        yeuCau = intent.getStringExtra("yeuCau");
        startDate = intent.getStringExtra("startDate");
        endDate = intent.getStringExtra("endDate");
        needsSponsor = intent.getBooleanExtra("needsSponsor", false);
        targetBudget = intent.getStringExtra("targetBudget");
        budgetPurpose = intent.getStringExtra("budgetPurpose");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvRoles = findViewById(R.id.rvRoles);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvRoleCount = findViewById(R.id.tvRoleCount);
        btnAddRole = findViewById(R.id.btnAddRole);
        btnNext = findViewById(R.id.btnNext);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null){
            CampaignRole role = (CampaignRole) data.getSerializableExtra("role");

            if(requestCode == REQUEST_ADD_ROLE){
                roleList.add(role);
                adapter.notifyItemInserted(roleList.size() - 1);
                Toast.makeText(this, "Đã thêm vai trò", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_EDIT_ROLE) {
                int position = data.getIntExtra("position", -1);
                if(position != -1){
                    roleList.set(position, role);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Đã cập nhật vai trò", Toast.LENGTH_SHORT).show();
                }
            }
            updateUI();
        }
    }
}