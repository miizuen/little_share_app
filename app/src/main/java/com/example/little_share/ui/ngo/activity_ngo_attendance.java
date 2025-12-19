package com.example.little_share.ui.ngo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Shift;
import com.example.little_share.ui.ngo.dialog.QRScannerDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class activity_ngo_attendance extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private ImageButton btnBack;
    private MaterialButton btnScanQR;
    private RecyclerView recyclerTodayShifts;

    private List<Shift> shiftList = new ArrayList<>();
    private QRScannerDialog qrDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_attendance);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadTodayShifts();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnScanQR = findViewById(R.id.btnScanQR);
        recyclerTodayShifts = findViewById(R.id.recyclerTodayShifts);
    }

    private void setupRecyclerView() {
        recyclerTodayShifts.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Setup adapter
        // AttendanceShiftAdapter adapter = new AttendanceShiftAdapter(this, shiftList);
        // recyclerTodayShifts.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnScanQR.setOnClickListener(v -> {
            // Kiểm tra quyền camera
            if (checkCameraPermission()) {
                showQRScanner();
            } else {
                requestCameraPermission();
            }
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showQRScanner();
            } else {
                Toast.makeText(this, "Cần cấp quyền camera để quét QR", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showQRScanner() {
        qrDialog = new QRScannerDialog(this, new QRScannerDialog.OnQRScannedListener() {
            @Override
            public void onQRScanned(String code) {
                handleAttendanceCode(code, true);
            }

            @Override
            public void onManualCodeEntered(String code) {
                handleAttendanceCode(code, false);
            }
        });

        qrDialog.show();
    }

    private void handleAttendanceCode(String code, boolean isScanned) {
        // Xử lý mã điểm danh
        String message = isScanned ? "Đã quét QR: " : "Mã thủ công: ";
        Toast.makeText(this, message + code, Toast.LENGTH_LONG).show();

        // TODO: Gọi API điểm danh với mã này
        // checkAttendance(code);
    }

    private void loadTodayShifts() {
        // TODO: Load ca làm việc hôm nay từ Firebase
        // CampaignRepository hoặc ShiftRepository
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data nếu cần
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qrDialog != null && qrDialog.isShowing()) {
            qrDialog.dismiss();
        }
    }
}