package com.example.little_share.ui.volunteer;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
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
import com.example.little_share.data.models.User;
import com.example.little_share.data.repositories.UserRepository;
import com.example.little_share.helper.ImgBBUploader;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class activity_volunteer_edit_profile extends AppCompatActivity {

    private ImageView btnBack, ivAvatar, btnEditAvatar;
    private EditText etName, etEmail, etPhone, etAddress;
    private MaterialButton btnUpdateProfile;
    private UserRepository userRepo;
    private String currentUserId;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this)
                            .load(uri)
                            .circleCrop()
                            .into(ivAvatar);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        try {
            setContentView(R.layout.activity_volunteer_edit_profile);

            // Initialize views
            btnBack = findViewById(R.id.btnBack);
            ivAvatar = findViewById(R.id.ivAvatar);
            btnEditAvatar = findViewById(R.id.btnEditAvatar);
            etName = findViewById(R.id.etName);
            etEmail = findViewById(R.id.etEmail);
            etPhone = findViewById(R.id.etPhone);
            etAddress = findViewById(R.id.etAddress);
            btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

            // Check for null views
            if (btnBack == null) {
                Toast.makeText(this, "Lỗi: btnBack not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (ivAvatar == null) {
                Toast.makeText(this, "Lỗi: ivAvatar not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (etName == null) {
                Toast.makeText(this, "Lỗi: etName not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            userRepo = new UserRepository();

            // Check current user
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Lỗi: Chưa đăng nhập", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Nút back
            btnBack.setOnClickListener(v -> finish());

            // Bấm vào avatar hoặc nút đổi ảnh
            ivAvatar.setOnClickListener(v -> openGallery());
            if (btnEditAvatar != null) {
                btnEditAvatar.setOnClickListener(v -> openGallery());
            }

            // Nút lưu
            if (btnUpdateProfile != null) {
                btnUpdateProfile.setOnClickListener(v -> saveProfile());
            }

            loadCurrentUserData();

        } catch (Exception e) {
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

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void loadCurrentUserData() {
        userRepo.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;

                try {
                    if (etName != null) {
                        etName.setText(user.getFullName());
                    }
                    if (etEmail != null) {
                        etEmail.setText(user.getEmail());
                    }
                    if (etPhone != null) {
                        etPhone.setText(user.getPhone());
                    }
                    if (etAddress != null) {
                        etAddress.setText(user.getAddress());
                    }

                    // Load avatar hiện tại
                    if (ivAvatar != null) {
                        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                            Glide.with(activity_volunteer_edit_profile.this)
                                    .load(user.getAvatar())
                                    .placeholder(R.drawable.img_profile_volunteer)
                                    .error(R.drawable.img_profile_volunteer)
                                    .circleCrop()
                                    .into(ivAvatar);
                        } else {
                            ivAvatar.setImageResource(R.drawable.img_profile_volunteer);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(activity_volunteer_edit_profile.this, "Lỗi hiển thị dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(activity_volunteer_edit_profile.this, "Lỗi tải dữ liệu: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveProfile() {
        String fullName = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpdateProfile.setEnabled(false);
        btnUpdateProfile.setText("Đang lưu...");

        // Nếu có ảnh mới → upload trước
        if (selectedImageUri != null) {
            uploadImageAndSave(fullName, email, phone, address);
        } else {
            // Không đổi ảnh → chỉ cập nhật thông tin
            updateProfileWithoutImage(fullName, email, phone, address);
        }
    }

    private void uploadImageAndSave(String fullName, String email, String phone, String address) {
        ImgBBUploader.uploadImage(this, selectedImageUri, new ImgBBUploader.UploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                // Upload thành công → lưu toàn bộ thông tin + avatar
                Map<String, Object> updates = new HashMap<>();
                updates.put("fullName", fullName);
                updates.put("email", email);
                updates.put("phone", phone);
                updates.put("address", address);
                updates.put("avatar", imageUrl);

                userRepo.updateUser(currentUserId, updates, new UserRepository.OnUserListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(activity_volunteer_edit_profile.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(activity_volunteer_edit_profile.this, "Lỗi lưu dữ liệu: " + error, Toast.LENGTH_SHORT).show();
                            btnUpdateProfile.setEnabled(true);
                            btnUpdateProfile.setText("CẬP NHẬT PROFILE");
                        });
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_volunteer_edit_profile.this, "Upload ảnh thất bại: " + error, Toast.LENGTH_SHORT).show();
                    btnUpdateProfile.setEnabled(true);
                    btnUpdateProfile.setText("CẬP NHẬT PROFILE");
                });
            }

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> btnUpdateProfile.setText("Đang tải ảnh... " + progress + "%"));
            }
        });
    }

    private void updateProfileWithoutImage(String fullName, String email, String phone, String address) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("address", address);

        userRepo.updateUser(currentUserId, updates, new UserRepository.OnUserListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(activity_volunteer_edit_profile.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_volunteer_edit_profile.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    btnUpdateProfile.setEnabled(true);
                    btnUpdateProfile.setText("CẬP NHẬT PROFILE");
                });
            }
        });
    }
}
