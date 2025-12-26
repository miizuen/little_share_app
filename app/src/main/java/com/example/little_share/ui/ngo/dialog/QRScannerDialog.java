package com.example.little_share.ui.ngo.dialog;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.example.little_share.utils.QRCodeGenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.little_share.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Collections;
import java.util.List;

public class QRScannerDialog extends Dialog {
    private DecoratedBarcodeView barcodeView;
    private FrameLayout cameraPreview;
    private LinearLayout layoutPlaceholder;
    private EditText edtManualCode;
    private AppCompatButton btnConfirm, btnCloseDialog;
    private ImageView btnClose;
    private OnQRScannedListener listener;
    private boolean isScanning = false;

    public interface OnQRScannedListener {
        void onQRScanned(String code);
        void onManualCodeEntered(String code);

        // Thêm phương thức mới để xử lý các loại QR khác nhau
        void onGiftRedemptionScanned(String redemptionId, String userId, String giftId);
        void onCampaignRegistrationScanned(String registrationId, String userId, String campaignId);
        void onVolunteerScanned(String volunteerId);
        void onInvalidQRScanned(String error);
    }


    public QRScannerDialog(@NonNull Context context, OnQRScannedListener listener) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_ngo_scan_qr);

        initViews();
        setupCamera();
        setupListeners();
    }

    private void initViews() {
        cameraPreview = findViewById(R.id.cameraPreview);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        edtManualCode = findViewById(R.id.edtManualCode);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
        btnClose = findViewById(R.id.btnClose);
    }

    private void setupCamera() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Cần cấp quyền camera để quét QR", Toast.LENGTH_LONG).show();
            dismiss();
            return;
        }

        try {
            android.util.Log.d("QRScanner", "Setting up camera..."); // THÊM LOG

            barcodeView = new DecoratedBarcodeView(getContext());
            barcodeView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            barcodeView.getBarcodeView().setDecoderFactory(
                    new DefaultDecoderFactory(Collections.singleton(BarcodeFormat.QR_CODE))
            );

            cameraPreview.addView(barcodeView);
            layoutPlaceholder.setVisibility(View.GONE);

            // THÊM DELAY NHỎ TRƯỚC KHI START SCANNING
            cameraPreview.post(() -> startScanning());

        } catch (Exception e) {
            android.util.Log.e("QRScanner", "Error setting up camera", e);
            Toast.makeText(getContext(), "Không thể khởi động camera: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            layoutPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void startScanning() {
        if (barcodeView != null && !isScanning) {
            try {
                barcodeView.decodeContinuous(new BarcodeCallback() {
                    @Override
                    public void barcodeResult(BarcodeResult result) {
                        if (result != null && result.getText() != null) {
                            android.util.Log.d("QRScanner", "QR scanned: " + result.getText());
                            handleQRScanned(result.getText());
                        }
                    }

                    @Override
                    public void possibleResultPoints(List<ResultPoint> resultPoints) {
                        // Có thể vẽ các điểm tìm thấy QR code
                    }
                });

                barcodeView.resume();
                isScanning = true;
                android.util.Log.d("QRScanner", "Camera started successfully");

            } catch (Exception e) {
                android.util.Log.e("QRScanner", "Error starting camera", e);
                Toast.makeText(getContext(), "Lỗi camera, vui lòng nhập mã thủ công", Toast.LENGTH_LONG).show();
                layoutPlaceholder.setVisibility(View.VISIBLE);
            }
        }
    }



    private void stopScanning() {
        if (barcodeView != null && isScanning) {
            barcodeView.pause();
            isScanning = false;
        }
    }

    private void handleQRScanned(String code) {
        stopScanning();

        // Parse QR code để xác định loại
        QRCodeGenerator.QRCodeData qrData = QRCodeGenerator.parseQRCode(code);

        if (qrData == null) {
            // QR code không hợp lệ
            if (listener != null) {
                listener.onInvalidQRScanned("QR code không hợp lệ hoặc không được hỗ trợ");
            }
            dismiss();
            return;
        }

        // Xử lý theo loại QR code
        switch (qrData.type) {
            case "gift":
                handleGiftRedemptionQR(qrData);
                break;
            case "campaign":
                handleCampaignRegistrationQR(qrData);
                break;
            case "volunteer":
                handleVolunteerQR(qrData);
                break;
            default:
                if (listener != null) {
                    listener.onInvalidQRScanned("Loại QR code không được hỗ trợ: " + qrData.type);
                }
                break;
        }

        dismiss();
    }

    private void handleGiftRedemptionQR(QRCodeGenerator.QRCodeData qrData) {
        String redemptionId = qrData.getRegistrationId(); // redemptionId
        String userId = qrData.getUserId();
        String giftId = qrData.getReferenceId();

        if (listener != null) {
            listener.onGiftRedemptionScanned(redemptionId, userId, giftId);
        }
    }

    private void handleCampaignRegistrationQR(QRCodeGenerator.QRCodeData qrData) {
        String registrationId = qrData.getRegistrationId();
        String userId = qrData.getUserId();
        String campaignId = qrData.getReferenceId();

        if (listener != null) {
            listener.onCampaignRegistrationScanned(registrationId, userId, campaignId);
        }
    }

    private void handleVolunteerQR(QRCodeGenerator.QRCodeData qrData) {
        String volunteerId = qrData.getUserId();

        if (listener != null) {
            listener.onVolunteerScanned(volunteerId);
        }
    }


    private void setupListeners() {
        // Nút X đóng
        btnClose.setOnClickListener(v -> dismiss());

        // Nút đóng dưới
        btnCloseDialog.setOnClickListener(v -> dismiss());

        // Xác nhận mã thủ công
        btnConfirm.setOnClickListener(v -> {
            String code = edtManualCode.getText().toString().trim().toUpperCase();

            if (TextUtils.isEmpty(code)) {
                Toast.makeText(getContext(), "Vui lòng nhập mã", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onManualCodeEntered(code);
            }

            dismiss();
        });
    }

    @Override
    public void show() {
        super.show();
        if (barcodeView != null) {
            startScanning();
        }
    }

    @Override
    public void dismiss() {
        stopScanning();
        super.dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScanning();
    }
}