package com.example.little_share.ui.ngo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.ui.ngo.dialog.QRScannerDialog;
import com.example.little_share.utils.QRCodeGenerator;
import com.example.little_share.ui.ngo.dialog.QRScannerDialog;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class activity_ngo_attendance extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final String TAG = "NGOAttendance";

    private ImageButton btnBack;
    private MaterialButton btnScanQR;
    private RecyclerView recyclerTodayShifts;

    private List<Shift> shiftList = new ArrayList<>();
    private QRScannerDialog qrDialog;


    private CampaignRepository campaignRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_attendance);

        initViews();
        campaignRepository = new CampaignRepository();
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

            // Thêm các phương thức mới
            @Override
            public void onGiftRedemptionScanned(String redemptionId, String userId, String giftId) {
                // Không xử lý gift redemption trong attendance activity
                Toast.makeText(activity_ngo_attendance.this,
                        "QR code này dành cho đổi quà, không phải điểm danh", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCampaignRegistrationScanned(String registrationId, String userId, String campaignId) {
                // Xử lý điểm danh chiến dịch
                showAttendanceConfirmation(registrationId, userId, campaignId, true);
            }

            @Override
            public void onVolunteerScanned(String volunteerId) {
                // Không xử lý volunteer scan trong attendance
                Toast.makeText(activity_ngo_attendance.this,
                        "QR code volunteer không được hỗ trợ ở đây", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInvalidQRScanned(String error) {
                showErrorDialog("QR code không hợp lệ", error);
            }
        });

        qrDialog.show();
    }


    private void handleAttendanceCode(String code, boolean isScanned) {
        Log.d(TAG, "Processing attendance code: " + code);

        // Parse QR code để kiểm tra loại
        QRCodeGenerator.QRCodeData qrData = QRCodeGenerator.parseQRCode(code);

        if (qrData == null) {
            showErrorDialog("QR code không hợp lệ", "Mã QR không đúng định dạng hoặc đã hỏng.");
            return;
        }

        // Kiểm tra xem có phải QR code chiến dịch không
        if (!"campaign".equals(qrData.type)) {
            showErrorDialog("QR code không phù hợp", "QR code này không phải để điểm danh chiến dịch.");
            return;
        }

        // Lấy thông tin từ QR code
        String registrationId = qrData.getRegistrationId();
        String userId = qrData.getUserId();
        String campaignId = qrData.getReferenceId();

        Log.d(TAG, "Parsed QR - Registration: " + registrationId + ", User: " + userId + ", Campaign: " + campaignId);

        // Hiển thị dialog xác nhận điểm danh
        showAttendanceConfirmation(registrationId, userId, campaignId, isScanned);
    }

    /**
     * Hiển thị dialog xác nhận điểm danh
     */
    private void showAttendanceConfirmation(String registrationId, String userId, String campaignId, boolean isScanned) {
        String scanMethod = isScanned ? "quét QR" : "nhập thủ công";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận điểm danh")
                .setMessage("Bạn có muốn xác nhận điểm danh cho volunteer này?\n\n" +
                        "Phương thức: " + scanMethod + "\n" +
                        "Mã đăng ký: " + registrationId)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Gọi CampaignRepository để xác nhận điểm danh
                    confirmAttendance(registrationId, userId, campaignId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Xác nhận điểm danh và cập nhật database
     */
    private void confirmAttendance(String registrationId, String userId, String campaignId) {
        Toast.makeText(this, "Đang xử lý điểm danh...", Toast.LENGTH_SHORT).show();

        // Gọi CampaignRepository với method mới
        campaignRepository.confirmAttendance(registrationId, userId, campaignId,
                new CampaignRepository.OnAttendanceListener() {
                    @Override
                    public void onSuccess(String message) {
                        showSuccessDialog(registrationId, userId);
                        loadTodayShifts(); // Refresh danh sách
                    }

                    @Override
                    public void onFailure(String error) {
                        showErrorDialog("Lỗi điểm danh", error);
                    }
                });
    }


    /**
     * Hiển thị dialog thành công
     */
    private void showSuccessDialog(String registrationId, String userId) {
        new AlertDialog.Builder(this)
                .setTitle("Điểm danh thành công")
                .setMessage("Đã xác nhận điểm danh cho volunteer.\n\n" +
                        "Mã đăng ký: " + registrationId + "\n" +
                        "Volunteer sẽ nhận được điểm thưởng.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Refresh lại danh sách nếu cần
                    loadTodayShifts();
                })
                .show();
    }

    /**
     * Hiển thị dialog lỗi
     */
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void loadTodayShifts() {
        // TODO: Load ca làm việc hôm nay từ Firebase
        Log.d(TAG, "Loading today's shifts...");

        /*
        // Code này sẽ được implement sau
        campaignRepository.getTodayShifts(new CampaignRepository.OnShiftsListener() {
            @Override
            public void onSuccess(List<Shift> shifts) {
                shiftList.clear();
                shiftList.addAll(shifts);
                // Cập nhật adapter nếu có
                Log.d(TAG, "Loaded " + shifts.size() + " shifts for today");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading shifts: " + error);
                Toast.makeText(activity_ngo_attendance.this,
                    "Không thể tải danh sách ca làm việc", Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data nếu cần
        loadTodayShifts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qrDialog != null && qrDialog.isShowing()) {
            qrDialog.dismiss();
        }
    }
}
