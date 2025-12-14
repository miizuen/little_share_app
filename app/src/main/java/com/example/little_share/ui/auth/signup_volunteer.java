package com.example.little_share.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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
import com.example.little_share.data.repositories.UserRepository;
import com.example.little_share.ui.volunteer.activity_volunteer_main;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class signup_volunteer extends AppCompatActivity {

    ImageView btnBackToRoleScreen;
    MaterialButton btnSignUp;
    TextInputEditText edtName, edtEmail, edtPassword, edtAddress, edtPhone;
    private AuthRepository authRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_volunteer);

        authRepository = new AuthRepository();
        userRepository = new UserRepository();

        btnBackToRoleScreen = findViewById(R.id.btnBackToRoleScreen);
        btnSignUp = findViewById(R.id.btnSignUp);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtAddress = findViewById(R.id.edtAddress);
        edtPhone = findViewById(R.id.edtPhone);

        btnBackToRoleScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signup_volunteer.this, role_selection.class);
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
        String fullName = edtName.getText().toString();
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();
        String address = edtAddress.getText().toString();
        String phone = edtPhone.getText().toString();

        if(TextUtils.isEmpty(fullName)){
            edtName.setError("Vui lòng nhập họ tên");
            edtName.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(email)){
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(password) && password.length() < 6){
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

        authRepository.register(email, password, new AuthRepository.OnAuthListener(){

            @Override
            public void onSuccess(String userId) {
                User user = new User();
                user.setEmail(email);
                user.setFullName(fullName);
                user.setPhone(phone);
                user.setAddress(address);
                user.setRole(User.UserRole.VOLUNTEER);
                userRepository.createUser(userId, user, new UserRepository.OnUserListener(){

                    @Override
                    public void onSuccess() {
                        Toast.makeText(signup_volunteer.this,
                                "Đăng ký thành công! Chào mừng tình nguyện viên mới!", Toast.LENGTH_LONG).show();

                        // Chuyển thẳng vào màn hình chính của Volunteer
                        startActivity(new Intent(signup_volunteer.this, activity_volunteer_main.class));
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(signup_volunteer.this,
                                "Lưu thông tin thất bại: " + error, Toast.LENGTH_SHORT).show();
                        btnSignUp.setEnabled(true);
                        btnSignUp.setText("Đăng kí");
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                if (error.contains("email address is already in use")) {
                    Toast.makeText(signup_volunteer.this,
                            "Email này đã được sử dụng!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(signup_volunteer.this,
                            "Đăng ký thất bại: " + error, Toast.LENGTH_SHORT).show();
                }
                btnSignUp.setEnabled(true);
                btnSignUp.setText("Đăng kí");
            }
        });
    }
}