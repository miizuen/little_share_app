package com.example.little_share.ui.ngo;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.helper.ImgBBUploader;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;


public class activity_ngo_create_campaign_no_role extends AppCompatActivity {

    private TextInputEditText etVolunteerCount, etRequirements, etContact;
    private SeekBar seekBarPoints;
    private TextView tvPointsValue;
    private Campaign tempCampaign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_create_campaign_no_role);

        tempCampaign = (Campaign) getIntent().getSerializableExtra("temp_campaign");
        if(tempCampaign == null){
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupSeekBar();
        setupCreateButton();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupCreateButton() {
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            if (!validateForm()) return;

            // Cập nhật dữ liệu cơ bản
            int pointReward = seekBarPoints.getProgress() + 5;
            tempCampaign.setMaxVolunteers(Integer.parseInt(etVolunteerCount.getText().toString().trim()));
            tempCampaign.setRequirements(etRequirements.getText().toString().trim());
            tempCampaign.setContactPhone(etContact.getText().toString().trim());
            tempCampaign.setPointsReward(pointReward);

            // XÓA ĐOẠN SET ORGANIZATION NAME Ở ĐÂY

            // Upload ảnh nếu cần
            if (tempCampaign.getImageUrl() != null && tempCampaign.getImageUrl().startsWith("content://")) {
                uploadImageAndCreateCampaign();
            } else {
                createCampaignDirectly();
            }
        });
    }


    private void setupSeekBar() {
        seekBarPoints.setMax(45);
        seekBarPoints.setProgress(0);
        seekBarPoints.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int points = i + 5;
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
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        etVolunteerCount = findViewById(R.id.etVolunteerCount);
        etRequirements = findViewById(R.id.etRequirements);
        etContact = findViewById(R.id.etContact);
        seekBarPoints = findViewById(R.id.seekBarPoints);
        tvPointsValue = findViewById(R.id.tvPointsValue);
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(etVolunteerCount.getText())) {
            etVolunteerCount.setError("Vui lòng nhập số lượng");
            return false;
        }
        if (TextUtils.isEmpty(etRequirements.getText())) {
            etRequirements.setError("Vui lòng nhập yêu cầu");
            return false;
        }
        return true;
    }

    private void uploadImageAndCreateCampaign() {
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

        Uri imageUriToUpload = Uri.parse(tempCampaign.getImageUrl());

        ImgBBUploader.uploadImage(this, imageUriToUpload, new ImgBBUploader.UploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    Log.d("Upload", "Success: " + imageUrl);
                    tempCampaign.setImageUrl(imageUrl);
                    createCampaignDirectly();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Log.e("Upload", "Failed: " + error);
                    Toast.makeText(activity_ngo_create_campaign_no_role.this,
                            "Upload thất bại: " + error, Toast.LENGTH_LONG).show();

                    // Hỏi có muốn tạo campaign không ảnh
                    new androidx.appcompat.app.AlertDialog.Builder(activity_ngo_create_campaign_no_role.this)
                            .setTitle("Upload ảnh thất bại")
                            .setMessage("Bạn có muốn tạo chiến dịch không có ảnh?")
                            .setPositiveButton("Có", (dialog, which) -> {
                                tempCampaign.setImageUrl("");
                                createCampaignDirectly();
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                });
            }

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> {
                    Log.d("Upload", "Progress: " + progress + "%");
                });
            }
        });
    }

    // TẠO CHIẾN DỊCH SAU KHI ĐÃ CÓ LINK ẢNH CHUẨN
    private void createCampaignDirectly() {
        // Dùng method mới để lấy tên tổ chức và tạo campaign
        new CampaignRepository().getOrganizationNameAndCreate(tempCampaign, new CampaignRepository.OnCampaignListener() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(activity_ngo_create_campaign_no_role.this, "Tạo chiến dịch thành công!", Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(activity_ngo_create_campaign_no_role.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}