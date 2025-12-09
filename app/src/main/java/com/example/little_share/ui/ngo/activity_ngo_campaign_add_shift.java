package com.example.little_share.ui.ngo;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.LinearLayout;
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
import com.example.little_share.data.models.Shift;
import com.example.little_share.ui.ngo.adapter.RoleAssignmentAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class activity_ngo_campaign_add_shift extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextInputEditText etShiftName, etStartTime, etEndTime;
    private RecyclerView rvRoleAssignment;
    private MaterialButton btnCancel, btnSave;
    private RoleAssignmentAdapter adapter;
    private List<CampaignRole> roleList;
    private Map<String, Integer> roleAssignments = new HashMap<>();
    private boolean isEditMode = false;
    private Shift editingShift;
    private int editPosition = -1;
    private int selectedHour = 8, selectedMinute = 0;
    private boolean selectingStartTime = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_campaign_add_shift);

        receiveData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupTimeInputs();
        setupButtons();

        if (isEditMode) {
            fillDataForEdit();
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void fillDataForEdit() {
        if (editingShift != null) {
            etShiftName.setText(editingShift.getShiftName());
            etStartTime.setText(editingShift.getStartTime());
            etEndTime.setText(editingShift.getEndTime());
        }
    }
    private void setupButtons() {
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveShift());
    }



    private void setupTimeInputs() {
        etStartTime.setOnClickListener(v -> {
            selectingStartTime = true;
            showTimePickerDialog();
        });

        etEndTime.setOnClickListener(v -> {
            selectingStartTime = false;
            showTimePickerDialog();
        });
    }

    private void showTimePickerDialog() {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    if (selectingStartTime) {
                        etStartTime.setText(time);
                    } else {
                        etEndTime.setText(time);
                    }
                },

                selectedHour,
                selectedMinute,
                true
        );
        dialog.show();
    }

    private void setupRecyclerView() {
        adapter = new RoleAssignmentAdapter(this, roleList, roleAssignments);
        rvRoleAssignment.setLayoutManager(new LinearLayoutManager(this));
        rvRoleAssignment.setAdapter(adapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if(isEditMode){
            toolbar.setTitle("Sửa ca làm viêc");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etShiftName = findViewById(R.id.etShiftName);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        rvRoleAssignment = findViewById(R.id.rvRoleAssignment);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
    }

    private void receiveData() {
        Intent intent = getIntent();
        roleList = (List<CampaignRole>) intent.getSerializableExtra("roles");
        isEditMode = intent.getBooleanExtra("isEdit", false);

        if (isEditMode) {
            editingShift = (Shift) intent.getSerializableExtra("shift");
            editPosition = intent.getIntExtra("position", -1);
            Map<String, Integer> savedAssignments = (Map<String, Integer>)
                    intent.getSerializableExtra("roleAssignments");
            if (savedAssignments != null) {
                roleAssignments.putAll(savedAssignments);
            }
        }
    }

    private void saveShift() {
        String shiftName = etShiftName.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(shiftName)) {
            etShiftName.setError("Vui lòng nhập tên ca làm");
            etShiftName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(startTime)) {
            etStartTime.setError("Vui lòng chọn thời gian bắt đầu");
            etStartTime.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(endTime)) {
            etEndTime.setError("Vui lòng chọn thời gian kết thúc");
            etEndTime.requestFocus();
            return;
        }

        // Kiểm tra có phân bổ vai trò chưa
        int totalVolunteers = 0;
        for (Integer count : roleAssignments.values()) {
            totalVolunteers += count;
        }

        if (totalVolunteers == 0) {
            Toast.makeText(this, "Vui lòng phân bổ ít nhất 1 tình nguyện viên", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo hoặc cập nhật shift
        Shift shift;
        if (isEditMode && editingShift != null) {
            shift = editingShift;
            shift.setShiftName(shiftName);
            shift.setStartTime(startTime);
            shift.setEndTime(endTime);
            shift.setMaxVolunteers(totalVolunteers);
        } else {
            shift = new Shift();
            shift.setShiftName(shiftName);
            shift.setStartTime(startTime);
            shift.setEndTime(endTime);
            shift.setMaxVolunteers(totalVolunteers);
            shift.setCurrentVolunteers(0);
        }

        // Trả kết quả về
        Intent resultIntent = new Intent();
        resultIntent.putExtra("shift", shift);
        resultIntent.putExtra("roleAssignments", (Serializable) new HashMap<>(roleAssignments));
        if (isEditMode) {
            resultIntent.putExtra("position", editPosition);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}