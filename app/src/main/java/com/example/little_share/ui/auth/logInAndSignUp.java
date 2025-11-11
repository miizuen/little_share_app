package com.example.little_share.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.example.little_share.spash_screen;

public class logInAndSignUp extends AppCompatActivity {
    Button btnToLoginScreen, btnRoleSeletionScreen;
    ImageView btnBackToSplashScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in_and_sign_up);
        btnBackToSplashScreen = findViewById(R.id.backToSplashScreen);
        btnToLoginScreen = findViewById(R.id.btnToLogInScreen);
        btnRoleSeletionScreen = findViewById(R.id.btnToRoleSelectionScreen);

        btnBackToSplashScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(logInAndSignUp.this, spash_screen.class);
                startActivity(intent1);
            }
        });

        btnToLoginScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(logInAndSignUp.this, login.class);
                startActivity(intent2);
            }
        });

        btnRoleSeletionScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent3 = new Intent(logInAndSignUp.this, role_selection.class);
                startActivity(intent3);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}