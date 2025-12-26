package com.example.little_share.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.example.little_share.data.models.User;
import com.example.little_share.data.repositories.AuthRepository;
import com.example.little_share.data.repositories.OrganizationRepository;
import com.example.little_share.data.repositories.UserRepository;
import com.example.little_share.ui.ngo.activity_ngo_main;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class signup_ngo extends AppCompatActivity {
    ImageView btnBackToRoleScreen;
    TextInputEditText edtNGOname, edtNGOemail, edtNGOPassword, edtNGOAddress, edtNGOPhoneNumber;
    MaterialButton btnSignUpNGO;
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private OrganizationRepository organizationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_ngo);

        edtNGOname = findViewById(R.id.edtNGOname);
        edtNGOemail = findViewById(R.id.edtNGOemail);
        edtNGOPassword = findViewById(R.id.edtNGOPassword);
        edtNGOAddress = findViewById(R.id.edtNGOAddress);
        edtNGOPhoneNumber = findViewById(R.id.edtNGOPhoneNumber);
        btnSignUpNGO = findViewById(R.id.btnSignUpNGO);

        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        organizationRepository = new OrganizationRepository();

        btnBackToRoleScreen = findViewById(R.id.btnBackToRoleScreen);

        btnBackToRoleScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signup_ngo.this, role_selection.class);
                startActivity(intent);
            }
        });

        btnSignUpNGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void attemptSignUp() {
        String fullName = edtNGOname.getText().toString().trim();
        String email = edtNGOemail.getText().toString().trim();
        String password = edtNGOPassword.getText().toString().trim();
        String address = edtNGOAddress.getText().toString().trim();
        String phone = edtNGOPhoneNumber.getText().toString().trim();

        // Validate input
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSignUpNGO.setEnabled(false);
        btnSignUpNGO.setText("Đang tạo tài khoản....");

        // Bước 1: Tạo tài khoản Authentication
        authRepository.register(email, password, new AuthRepository.OnAuthListener() {
            @Override
            public void onSuccess(String userId) {
                // Bước 2: Tạo Organization document
                createOrganization(userId, fullName, email, phone, address);
            }

            @Override
            public void onFailure(String error) {
                if (error.contains("email address is already in use")) {
                    Toast.makeText(signup_ngo.this, "Email này đã được sử dụng!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(signup_ngo.this, "Đăng ký thất bại: " + error, Toast.LENGTH_SHORT).show();
                }
                btnSignUpNGO.setEnabled(true);
                btnSignUpNGO.setText("Đăng ký");
            }
        });
    }

    private void createOrganization(String userId, String fullName, String email, String phone, String address) {
        organizationRepository.createOrganization(
                userId,      // organizationId = userId
                fullName,    // Tên tổ chức
                email,       // Email
                phone,       // Số điện thoại
                address,     // Địa chỉ
                new OrganizationRepository.OnCreateOrgListener() {
                    @Override
                    public void onSuccess(String organizationId) {
                        // Bước 3: Tạo User document với organizationId
                        createUserDocument(userId, fullName, email, phone, address, organizationId);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(signup_ngo.this,
                                "Lỗi tạo tổ chức: " + error,
                                Toast.LENGTH_SHORT).show();

                        // Rollback: Xóa tài khoản auth đã tạo
                        authRepository.deleteAccount(new AuthRepository.OnSimpleListener() {
                            @Override
                            public void onSuccess() {
                                btnSignUpNGO.setEnabled(true);
                                btnSignUpNGO.setText("Đăng ký");
                            }

                            @Override
                            public void onFailure(String err) {
                                btnSignUpNGO.setEnabled(true);
                                btnSignUpNGO.setText("Đăng ký");
                            }
                        });
                    }
                }
        );
    }

    private void createUserDocument(String userId, String fullName, String email, String phone, String address, String organizationId) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        user.setRole(User.UserRole.ORGANIZATION);
        user.setOrganizationId(organizationId);  // ← QUAN TRỌNG: Set organizationId
        user.setAvatar("");
        user.setTotalPoints(0);
        user.setTotalDonations(0);
        user.setTotalCampaigns(0);

        userRepository.createUser(userId, user, new UserRepository.OnUserListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(signup_ngo.this,
                        "Chào mừng tổ chức thiện nguyện mới đã đến với Little Share",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(signup_ngo.this, activity_ngo_main.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(signup_ngo.this,
                        "Lưu thông tin thất bại: " + error,
                        Toast.LENGTH_SHORT).show();
                btnSignUpNGO.setEnabled(true);
                btnSignUpNGO.setText("Đăng ký");
            }
        });
    }
}