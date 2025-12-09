package com.example.little_share.ui.ngo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.CampaignRole;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class activity_ngo_campagin_add_role extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etRoleName, etRoleDescription;
    private SeekBar seekBarPoints;
    private TextView tvPointsValue;
    private MaterialButton btnCancel, btnSave;

    private boolean isEditMode = false;
    private CampaignRole editingRole;
    private int editPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_campagin_add_role);

        checkEditMode();
        initViews();
        setupToolbar();
        setupSeekBar();
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
        if(editingRole != null){
            etRoleName.setText(editingRole.getRoleName());
            etRoleDescription.setText(editingRole.getDescription());

            int points = editingRole.getPointsReward();
            seekBarPoints.setProgress(points - 5);
            tvPointsValue.setText(String.valueOf(points));
        }
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveRole());
    }

    private void saveRole() {
        String roleName = etRoleName.getText().toString();
        String roleDescription = etRoleDescription.getText().toString();
        int points = 5 + seekBarPoints.getProgress();

        if (TextUtils.isEmpty(roleName)) {
            etRoleName.setError("Vui lòng nhập tên vai trò");
            etRoleName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(roleDescription)) {
            etRoleDescription.setError("Vui lòng nhập mô tả vai trò");
            etRoleDescription.requestFocus();
            return;
        }

        if (roleDescription.length() > 150) {
            etRoleDescription.setError("Mô tả không được quá 150 ký tự");
            etRoleDescription.requestFocus();
            return;
        }

        CampaignRole role;
        if (isEditMode && editingRole != null) {
            role = editingRole;
            role.setRoleName(roleName);
            role.setDescription(roleDescription);
            role.setPointsReward(points);
        } else {
            role = new CampaignRole();
            role.setRoleName(roleName);
            role.setDescription(roleDescription);
            role.setPointsReward(points);
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("role", role);
        if(isEditMode){
            resultIntent.putExtra("position", editPosition);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void setupSeekBar() {
        seekBarPoints.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int points = 5 + i;
                tvPointsValue.setText(String.valueOf(points));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        tvPointsValue.setText("5");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if(isEditMode){
            toolbar.setTitle("Sửa vai trò");
        }else{
            toolbar.setTitle("Thêm vai trò");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etRoleName = findViewById(R.id.etRoleName);
        etRoleDescription = findViewById(R.id.etRoleDescription);
        seekBarPoints = findViewById(R.id.seekBarPoints);
        tvPointsValue = findViewById(R.id.tvPointsValue);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEdit", false);
        if (isEditMode) {
            editingRole = (CampaignRole) intent.getSerializableExtra("role");
            editPosition = intent.getIntExtra("position", -1);
        }
    }
    
    
}