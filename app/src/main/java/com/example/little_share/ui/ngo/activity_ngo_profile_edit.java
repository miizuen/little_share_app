package com.example.little_share.ui.ngo;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Organization;
import com.example.little_share.data.repositories.OrganizationRepository;
import com.example.little_share.helper.ImgBBUploader;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class activity_ngo_profile_edit extends AppCompatActivity {

    private ImageView btnBack, imgAvatar, btnChangeAvatar;
    private TextInputEditText etFullName, etEmail, etPhone, etLocation;
    private MaterialButton btnSaveProfile;
    private OrganizationRepository orgRepo;
    private String currentOrgId;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this)
                            .load(uri)
                            .circleCrop()
                            .into(imgAvatar);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_profile_edit);

        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        orgRepo = new OrganizationRepository();
        loadCurrentOrganizationData();

        // Nút back
        btnBack.setOnClickListener(v -> finish());

        // Bấm vào avatar hoặc nút đổi ảnh
        imgAvatar.setOnClickListener(v -> openGallery());
        btnChangeAvatar.setOnClickListener(v -> openGallery());

        // Nút lưu
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void loadCurrentOrganizationData() {
        orgRepo.getCurrentOrganization(new OrganizationRepository.OnOrganizationListener() {
            @Override
            public void onSuccess(Organization org) {
                if (org == null) return;

                currentOrgId = org.getId();

                etFullName.setText(org.getName());
                etEmail.setText(org.getEmail());
                etPhone.setText(org.getPhone());
                etLocation.setText(org.getAddress());

                // Load logo hiện tại
                if (org.getLogo() != null && !org.getLogo().isEmpty()) {
                    Glide.with(activity_ngo_profile_edit.this)
                            .load(org.getLogo())
                            .placeholder(R.drawable.logo_thelight)
                            .error(R.drawable.logo_thelight)
                            .circleCrop()
                            .into(imgAvatar);
                } else {
                    imgAvatar.setImageResource(R.drawable.logo_thelight);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(activity_ngo_profile_edit.this, "Lỗi tải dữ liệu: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveProfile() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etLocation.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Đang lưu...");

        // Nếu có ảnh mới → upload trước
        if (selectedImageUri != null) {
            uploadImageAndSave(name, email, phone, address);
        } else {
            // Không đổi ảnh → chỉ cập nhật thông tin
            updateProfileWithoutImage(name, email, phone, address);
        }
    }

    private void uploadImageAndSave(String name, String email, String phone, String address) {
        ImgBBUploader.uploadImage(this, selectedImageUri, new ImgBBUploader.UploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                // Upload thành công → lưu toàn bộ thông tin + logo
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", name);
                updates.put("email", email);
                updates.put("phone", phone);
                updates.put("address", address);
                updates.put("logo", imageUrl);

                orgRepo.updateOrganizationProfile(currentOrgId, updates, new OrganizationRepository.OnUpdateListener() {
                    @Override
                    public void onSuccess(String orgId) {
                        runOnUiThread(() -> {
                            Toast.makeText(activity_ngo_profile_edit.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(activity_ngo_profile_edit.this, "Lỗi lưu dữ liệu: " + error, Toast.LENGTH_SHORT).show();
                            btnSaveProfile.setEnabled(true);
                            btnSaveProfile.setText("LƯU THÔNG TIN");
                        });
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_ngo_profile_edit.this, "Upload ảnh thất bại: " + error, Toast.LENGTH_SHORT).show();
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("LƯU THÔNG TIN");
                });
            }

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> btnSaveProfile.setText("Đang tải ảnh... " + progress + "%"));
            }
        });
    }

    private void updateProfileWithoutImage(String name, String email, String phone, String address) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("address", address);

        orgRepo.updateOrganizationProfile(currentOrgId, updates, new OrganizationRepository.OnUpdateListener() {
            @Override
            public void onSuccess(String orgId) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_ngo_profile_edit.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_ngo_profile_edit.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("LƯU THÔNG TIN");
                });
            }
        });
    }
}