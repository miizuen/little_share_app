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
import com.example.little_share.data.models.Shift;
import com.example.little_share.ui.ngo.adapter.ShiftAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class acitivity_ngo_campaign_shift_list extends AppCompatActivity {

    private static final int REQUEST_ADD_SHIFT = 200;
    private static final int REQUEST_EDIT_SHIFT = 201;

    private MaterialToolbar toolbar;
    private RecyclerView rvShifts;
    private LinearLayout layoutEmptyState;
    private TextView tvShiftCount;
    private MaterialButton btnAddShift, btnNext;

    private ShiftAdapter adapter;
    private List<Shift> shiftList = new ArrayList<>();
    private Map<String, Map<String, Integer>> shiftRoleAssignments = new HashMap<>();

    // Data từ các bước trước
    private String campaignName, campaignDescription, category, imageUrl;
    private String location, specificLocation, yeuCau, startDate, endDate;
    private boolean needsSponsor;
    private String targetBudget, budgetPurpose;
    private List<CampaignRole> roleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acitivity_ngo_campaign_shift_list);

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
        btnAddShift.setOnClickListener(v -> {
            Intent intent = new Intent(this, activity_ngo_campaign_add_shift.class);
            intent.putExtra("roles", (Serializable)  roleList);
            startActivityForResult(intent, REQUEST_ADD_SHIFT);
        });

        btnNext.setOnClickListener(v -> {
           if(shiftList.isEmpty()){
               Toast.makeText(this, "Vui lòng thêm ít nhất 1 ca làm việc", Toast.LENGTH_SHORT).show();
               return;
           }
           goToReview();
        });
    }

    private void goToReview() {
        Intent intent = new Intent(this, activity_ngo_campagin_create_review.class);

        // Truyền tất cả data
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
        intent.putExtra("shifts", (Serializable) new ArrayList<>(shiftList));
        intent.putExtra("shiftRoleAssignments", (Serializable) shiftRoleAssignments);

        startActivity(intent);
    }

    private void setupRecyclerView() {
        adapter = new ShiftAdapter(this, shiftList, new ShiftAdapter.OnShiftActionListener() {
            @Override
            public void onEditClick(Shift shift, int position) {
                Intent intent = new Intent(acitivity_ngo_campaign_shift_list.this,
                        activity_ngo_campaign_add_shift.class);
                intent.putExtra("shift", shift);
                intent.putExtra("position", position);
                intent.putExtra("isEdit", true);
                intent.putExtra("roles", (Serializable) roleList);

                // Truyền role assignments của shift này
                Map<String, Integer> assignments = shiftRoleAssignments.get(shift.getId());
                if (assignments != null) {
                    intent.putExtra("roleAssignments", (Serializable) new HashMap<>(assignments));
                }

                startActivityForResult(intent, REQUEST_EDIT_SHIFT);
            }

            @Override
            public void onDeleteClick(Shift shift, int position) {
                showDeleteConfirmDialog(position);
            }
        });

        rvShifts.setLayoutManager(new LinearLayoutManager(this));
        rvShifts.setAdapter(adapter);
    }

    private void showDeleteConfirmDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa ca làm")
                .setMessage("Bạn có mốn xóa ca làm này?")
                .setPositiveButton("Xóa", (díalog, which) -> {
                    Shift shift = shiftList.get(position);
                    shiftRoleAssignments.remove(shift.getId());
                    shiftList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateUI();
                    Toast.makeText(this, "Đã xóa ca làm!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateUI() {
        if (shiftList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvShifts.setVisibility(View.GONE);
            btnNext.setEnabled(false);
            tvShiftCount.setText("0 ca làm");
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvShifts.setVisibility(View.VISIBLE);
            btnNext.setEnabled(true);
            tvShiftCount.setText(shiftList.size() + " ca làm");
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvShifts = findViewById(R.id.rvShifts);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvShiftCount = findViewById(R.id.tvShiftCount);
        btnAddShift = findViewById(R.id.btnAddShift);
        btnNext = findViewById(R.id.btnNext);
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
        roleList = (List<CampaignRole>) intent.getSerializableExtra("roles");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null){
            Shift shift = (Shift) data.getSerializableExtra("shift");
            Map<String, Integer> assignments = (Map<String, Integer>) data.getSerializableExtra("roleAssignments");

            if(requestCode == REQUEST_ADD_SHIFT){
                shift.setId("temp_" + System.currentTimeMillis());
                shiftList.add(shift);

                if(assignments != null){
                    shiftRoleAssignments.put(shift.getId(), assignments);
                }
                adapter.notifyItemInserted(shiftList.size() - 1);
                Toast.makeText(this, "Đã thêm ca làm", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_EDIT_SHIFT) {
                int position = data.getIntExtra("position", -1);
                if(position != -1){
                    shiftList.set(position, shift);
                    if(assignments != null){
                        shiftRoleAssignments.put(shift.getId(), assignments);
                    }
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Đã cập nhật ca làm", Toast.LENGTH_SHORT).show();
                }
            }
            updateUI();
        }
    }
}