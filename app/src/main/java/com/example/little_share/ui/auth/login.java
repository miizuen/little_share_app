package com.example.little_share.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.example.little_share.ui.ngo.activity_ngo_main;
import com.example.little_share.ui.sponsor.activity_sponsor_main;
import com.example.little_share.ui.volunteer.activity_volunteer_main;
import com.google.android.material.button.MaterialButton;

public class login extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    ImageView btnBackToLogInAndSignUpScreen;
    private AuthRepository authRepository;
    private UserRepository userRepository;

    EditText edtEmail, edtPassword;
    TextView tvForgotPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        btnBackToLogInAndSignUpScreen = findViewById(R.id.btnBackToLoginAndSignUpScreen);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        edtEmail = findViewById(R.id.etEmail);
        edtPassword = findViewById(R.id.etPassword);
        authRepository = new AuthRepository();
        userRepository = new UserRepository();

        btnBackToLogInAndSignUpScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(login.this, logInAndSignUp.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void attemptLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        Log.d(TAG, "Attempting login for email: " + email);

        authRepository.login(email, password, new AuthRepository.OnAuthListener() {
            @Override
            public void onSuccess(String userId) {
                Log.d(TAG, "Firebase Auth successful for userId: " + userId);

                // Lấy thông tin user đầy đủ thay vì chỉ lấy role
                getUserDataAndRedirect(userId);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Firebase Auth failed: " + error);

                if(error.contains("password is invalid") || error.contains("no user record")) {
                    Toast.makeText(login.this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                } else if (error.contains("network")) {
                    Toast.makeText(login.this, "Không có kết nối mạng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(login.this, "Đăng nhập thất bại: " + error, Toast.LENGTH_SHORT).show();
                }
                resetLoginButton();
            }
        });
    }

    private void getUserDataAndRedirect(String userId) {
        Log.d(TAG, "Getting user data for userId: " + userId);

        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    Log.e(TAG, "User object is null");
                    Toast.makeText(login.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    resetLoginButton();
                    return;
                }

                Log.d(TAG, "User data loaded successfully:");
                Log.d(TAG, "- Role: " + user.getRole());
                Log.d(TAG, "- FullName: " + user.getFullName());
                Log.d(TAG, "- Email: " + user.getEmail());

                if (user.getRole() == User.UserRole.SPONSOR) {
                    Log.d(TAG, "- SponsorId: " + user.getSponsorId());
                } else if (user.getRole() == User.UserRole.ORGANIZATION) {
                    Log.d(TAG, "- OrganizationId: " + user.getOrganizationId());
                }

                redirectByRole(user.getRole(), user);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to get user data: " + error);
                Toast.makeText(login.this, "Lỗi lấy thông tin người dùng: " + error, Toast.LENGTH_SHORT).show();
                resetLoginButton();
            }
        });
    }

    private void resetLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText("ĐĂNG NHẬP");
    }

    private void redirectByRole(User.UserRole role, User user) {
        Intent intent;
        String welcomeMsg;

        Log.d(TAG, "Redirecting user with role: " + role);

        switch (role) {
            case VOLUNTEER:
                intent = new Intent(this, activity_volunteer_main.class);
                welcomeMsg = "Chào mừng Tình nguyện viên " + user.getFullName() + "!";
                break;

            case SPONSOR:
                intent = new Intent(this, activity_sponsor_main.class);
                welcomeMsg = "Chào mừng Nhà tài trợ " + user.getFullName() + "!";
                Log.d(TAG, "Redirecting to sponsor main activity");
                break;

            case ORGANIZATION:
                intent = new Intent(this, activity_ngo_main.class);
                welcomeMsg = "Chào mừng Tổ chức " + user.getFullName() + "!";
                break;

            default:
                Log.e(TAG, "Invalid role: " + role);
                Toast.makeText(this, "Role không hợp lệ: " + role, Toast.LENGTH_LONG).show();
                resetLoginButton();
                return;
        }

        Toast.makeText(this, welcomeMsg, Toast.LENGTH_LONG).show();

        // Clear all previous activities and start new one
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
