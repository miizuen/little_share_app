package com.example.little_share.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.little_share.data.repositories.SponsorRepository;
import com.example.little_share.data.repositories.UserRepository;
import com.example.little_share.ui.sponsor.activity_sponsor_main;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class activity_signup_sponsor extends AppCompatActivity {
    ImageView btnBackToRoleScreen;
    MaterialButton btnSignUp;
    TextInputEditText edtName, edtEmail, edtPassword, edtAddress, edtPhone;
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private SponsorRepository sponsorRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_sponsor);
        btnBackToRoleScreen = findViewById(R.id.btnBackToRoleScreen);
        btnSignUp = findViewById(R.id.btnSignUp);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtAddress = findViewById(R.id.edtAddress);
        edtPhone = findViewById(R.id.edtPhone);
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        sponsorRepository = new SponsorRepository();


        btnBackToRoleScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_signup_sponsor.this, role_selection.class);
                startActivity(intent);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
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
        String fullname = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if(TextUtils.isEmpty(fullname)){
            edtName.setError("Vui lòng nhập họ tên");
            edtName.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(email)){
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(password) || password.length() < 6){
            edtPassword.setError("Mật khẩu phải >= 6 kí tự");
            edtPassword.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(address)){
            edtAddress.setError("Vui lòng nhập địa chỉ");
            edtAddress.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(phone)){
            edtPhone.setError("Vui lòng nhập số điện thoại");
            edtPhone.requestFocus();
            return;
        }

        btnSignUp.setEnabled(false);
        btnSignUp.setText("Đang tạo tài khoản....");

        // Bước 1: Tạo tài khoản Authentication
        authRepository.register(email, password, new AuthRepository.OnAuthListener() {
            @Override
            public void onSuccess(String userId) {
                // Bước 2: Tạo Sponsor document
                createSponsor(userId, fullname, email, phone, address);
            }

            @Override
            public void onFailure(String error) {
                if(error.contains("email address is already in use")){
                    Toast.makeText(activity_signup_sponsor.this, "Email này đã được sử dụng!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(activity_signup_sponsor.this, "Đăng kí không thành công: " + error, Toast.LENGTH_SHORT).show();
                }
                btnSignUp.setEnabled(true);
                btnSignUp.setText("Đăng kí");
            }
        });
    }

    private void createSponsor(String userId, String fullName, String email, String phone, String address) {
        sponsorRepository.createSponsor(
                userId,      // sponsorId = userId
                fullName,    // Tên nhà tài trợ
                email,       // Email
                phone,       // Số điện thoại
                address,     // Địa chỉ
                new SponsorRepository.OnCreateSponsorListener() {
                    @Override
                    public void onSuccess(String sponsorId) {
                        // Bước 3: Tạo User document với sponsorId
                        createUserDocument(userId, fullName, email, phone, address, sponsorId);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(activity_signup_sponsor.this,
                                "Lỗi tạo thông tin nhà tài trợ: " + error,
                                Toast.LENGTH_SHORT).show();

                        // Rollback: Xóa tài khoản auth đã tạo
                        authRepository.deleteAccount(new AuthRepository.OnSimpleListener() {
                            @Override
                            public void onSuccess() {
                                btnSignUp.setEnabled(true);
                                btnSignUp.setText("Đăng kí");
                            }

                            @Override
                            public void onFailure(String err) {
                                btnSignUp.setEnabled(true);
                                btnSignUp.setText("Đăng kí");
                            }
                        });
                    }
                }
        );
    }

    private void createUserDocument(String userId, String fullName, String email, String phone, String address, String sponsorId) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        user.setRole(User.UserRole.SPONSOR);
        user.setSponsorId(sponsorId);
        user.setAvatar("");
        user.setTotalPoints(0);
        user.setTotalDonations(0);
        user.setTotalCampaigns(0);

        userRepository.createUser(userId, user, new UserRepository.OnUserListener(){
            @Override
            public void onSuccess() {
                Toast.makeText(activity_signup_sponsor.this,
                        "Chào mừng nhà tài trợ mới đã đến với Little Share",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(activity_signup_sponsor.this, activity_sponsor_main.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(activity_signup_sponsor.this,
                        "Lưu thông tin thất bại: " + error,
                        Toast.LENGTH_SHORT).show();
                btnSignUp.setEnabled(true);
                btnSignUp.setText("Đăng kí");
            }
        });
    }
}