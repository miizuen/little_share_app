package com.example.little_share.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.little_share.R;

public class activity_about_app extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        initViews();
        setupClickListeners();
        loadAppVersion();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvVersion = findViewById(R.id.tvVersion);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = packageInfo.versionName;
            tvVersion.setText("Phiên bản " + version);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("Phiên bản 1.0.0");
        }
    }
}
