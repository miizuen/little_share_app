package com.example.little_share.ui.ngo;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "activity_ngo_profile_edit";
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
                    Log.d(TAG, "Image selected: " + uri.toString());
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

        try {
            setContentView(R.layout.activity_ngo_profile_edit);

            initViews();
            setupClickListeners();
            loadCurrentOrganizationData();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } catch (Exception e) {
            // Ignore WindowInsets error
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Check for null views
        if (btnBack == null || imgAvatar == null || etFullName == null || btnSaveProfile == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy các view cần thiết", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        orgRepo = new OrganizationRepository();
        Log.d(TAG, "Views initialized successfully");
    }

    private void setupClickListeners() {
        // Nút back
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        // Bấm vào avatar hoặc nút đổi ảnh
        imgAvatar.setOnClickListener(v -> {
            Log.d(TAG, "Avatar clicked");
            openGallery();
        });

        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v -> {
                Log.d(TAG, "Change avatar button clicked");
                openGallery();
            });
        }

        // Nút lưu
        btnSaveProfile.setOnClickListener(v -> {
            Log.d(TAG, "Save profile button clicked");
            saveProfile();
        });
    }

    private void openGallery() {
        try {
            galleryLauncher.launch("image/*");
        } catch (Exception e) {
            Log.e(TAG, "Error opening gallery: " + e.getMessage());
            Toast.makeText(this, "Lỗi mở thư viện ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCurrentOrganizationData() {
        Log.d(TAG, "Loading current organization data");

        orgRepo.getCurrentOrganization(new OrganizationRepository.OnOrganizationListener() {
            @Override
            public void onSuccess(Organization org) {
                try {
                    if (org == null) {
                        Log.e(TAG, "Organization is null");
                        Toast.makeText(activity_ngo_profile_edit.this, "Không tìm thấy thông tin tổ chức", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentOrgId = org.getId();
                    Log.d(TAG, "Organization loaded: " + org.getName());

                    // Fill form with current data
                    if (etFullName != null) etFullName.setText(org.getName());
                    if (etEmail != null) etEmail.setText(org.getEmail());
                    if (etPhone != null) etPhone.setText(org.getPhone());
                    if (etLocation != null) etLocation.setText(org.getAddress());

                    // Load current logo
                    if (imgAvatar != null) {
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
                } catch (Exception e) {
                    Log.e(TAG, "Error in onSuccess: " + e.getMessage());
                    Toast.makeText(activity_ngo_profile_edit.this, "Lỗi hiển thị dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading organization: " + error);
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

        if (currentOrgId == null || currentOrgId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID tổ chức", Toast.LENGTH_SHORT).show();
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
        Log.d(TAG, "Uploading image and saving profile");

        ImgBBUploader.uploadImage(this, selectedImageUri, new ImgBBUploader.UploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d(TAG, "Image uploaded successfully: " + imageUrl);

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
                        Log.d(TAG, "Profile updated successfully");
                        runOnUiThread(() -> {
                            Toast.makeText(activity_ngo_profile_edit.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Error updating profile: " + error);
                        runOnUiThread(() -> {
                            Toast.makeText(activity_ngo_profile_edit.this, "Lỗi lưu dữ liệu: " + error, Toast.LENGTH_SHORT).show();
                            resetSaveButton();
                        });
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Image upload failed: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(activity_ngo_profile_edit.this, "Upload ảnh thất bại: " + error, Toast.LENGTH_SHORT).show();
                    resetSaveButton();
                });
            }

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> btnSaveProfile.setText("Đang tải ảnh... " + progress + "%"));
            }
        });
    }

    private void updateProfileWithoutImage(String name, String email, String phone, String address) {
        Log.d(TAG, "Updating profile without image");

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("address", address);

        orgRepo.updateOrganizationProfile(currentOrgId, updates, new OrganizationRepository.OnUpdateListener() {
            @Override
            public void onSuccess(String orgId) {
                Log.d(TAG, "Profile updated successfully");
                runOnUiThread(() -> {
                    Toast.makeText(activity_ngo_profile_edit.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error updating profile: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(activity_ngo_profile_edit.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    resetSaveButton();
                });
            }
        });
    }

    private void resetSaveButton() {
        btnSaveProfile.setEnabled(true);
        btnSaveProfile.setText("LƯU THÔNG TIN");
    }
}
