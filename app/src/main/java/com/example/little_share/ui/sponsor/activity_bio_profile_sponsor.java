package com.example.little_share.ui.sponsor;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
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
import androidx.appcompat.widget.AppCompatButton;

import java.util.HashMap;
import java.util.Map;

public class activity_bio_profile_sponsor extends AppCompatActivity {

    private ImageView btnBack, imgAvatar;
    private TextView tvName, tvEmail, tvPhone, tvEmailContact, tvLocation;
    private AppCompatButton btnUpdateProfile;
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
                            .into(imgAvatar);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bio_profile_sponsor);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmailContact = findViewById(R.id.tvEmailContact);
        tvLocation = findViewById(R.id.tvLocation);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

        userRepo = new UserRepository();
        currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadCurrentUserData();

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Avatar click to change image
        imgAvatar.setOnClickListener(v -> openGallery());

        // Update profile button - show edit dialog
        btnUpdateProfile.setOnClickListener(v -> showEditDialog());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void loadCurrentUserData() {
        userRepo.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;

                // Update UI with user data
                tvName.setText(user.getFullName());
                tvEmail.setText(user.getEmail());
                tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa cập nhật");
                tvEmailContact.setText(user.getEmail());
                tvLocation.setText(user.getAddress() != null ? user.getAddress() : "Chưa cập nhật");

                // Load avatar
                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(activity_bio_profile_sponsor.this)
                            .load(user.getAvatar())
                            .placeholder(R.drawable.logo_d_japan)
                            .error(R.drawable.logo_d_japan)
                            .circleCrop()
                            .into(imgAvatar);
                } else {
                    imgAvatar.setImageResource(R.drawable.logo_d_japan);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(activity_bio_profile_sponsor.this, "Lỗi tải dữ liệu: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showEditDialog() {
        // Create simple edit dialog with EditTexts
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        android.widget.EditText etName = new android.widget.EditText(this);
        etName.setHint("Tên đầy đủ");
        etName.setText(tvName.getText().toString());

        android.widget.EditText etPhone = new android.widget.EditText(this);
        etPhone.setHint("Số điện thoại");
        etPhone.setText(tvPhone.getText().toString().equals("Chưa cập nhật") ? "" : tvPhone.getText().toString());

        android.widget.EditText etAddress = new android.widget.EditText(this);
        etAddress.setHint("Địa chỉ");
        etAddress.setText(tvLocation.getText().toString().equals("Chưa cập nhật") ? "" : tvLocation.getText().toString());

        // Create vertical layout for EditTexts
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        layout.addView(etName);
        layout.addView(etPhone);
        layout.addView(etAddress);

        builder.setTitle("Chỉnh sửa thông tin")
                .setView(layout)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    updateProfile(etName.getText().toString().trim(),
                            etPhone.getText().toString().trim(),
                            etAddress.getText().toString().trim());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateProfile(String fullName, String phone, String address) {
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpdateProfile.setEnabled(false);
        btnUpdateProfile.setText("Đang cập nhật...");

        // If there's a new image, upload it first
        if (selectedImageUri != null) {
            uploadImageAndSave(fullName, phone, address);
        } else {
            // No new image, just update text info
            updateProfileWithoutImage(fullName, phone, address);
        }
    }

    private void uploadImageAndSave(String fullName, String phone, String address) {
        ImgBBUploader.uploadImage(this, selectedImageUri, new ImgBBUploader.UploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("fullName", fullName);
                updates.put("phone", phone);
                updates.put("address", address);
                updates.put("avatar", imageUrl);

                userRepo.updateUser(currentUserId, updates, new UserRepository.OnUserListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(activity_bio_profile_sponsor.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            btnUpdateProfile.setEnabled(true);
                            btnUpdateProfile.setText("UPDATE PROFILE");
                            loadCurrentUserData(); // Reload data
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(activity_bio_profile_sponsor.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            btnUpdateProfile.setEnabled(true);
                            btnUpdateProfile.setText("UPDATE PROFILE");
                        });
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_bio_profile_sponsor.this, "Upload ảnh thất bại: " + error, Toast.LENGTH_SHORT).show();
                    btnUpdateProfile.setEnabled(true);
                    btnUpdateProfile.setText("UPDATE PROFILE");
                });
            }

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> btnUpdateProfile.setText("Đang tải ảnh... " + progress + "%"));
            }
        });
    }

    private void updateProfileWithoutImage(String fullName, String phone, String address) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("phone", phone);
        updates.put("address", address);

        userRepo.updateUser(currentUserId, updates, new UserRepository.OnUserListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(activity_bio_profile_sponsor.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    btnUpdateProfile.setEnabled(true);
                    btnUpdateProfile.setText("UPDATE PROFILE");
                    loadCurrentUserData(); // Reload data
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_bio_profile_sponsor.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    btnUpdateProfile.setEnabled(true);
                    btnUpdateProfile.setText("UPDATE PROFILE");
                });
            }
        });
    }
}
