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
        // Kiểm tra quyền camera
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Cần cấp quyền camera để quét QR", Toast.LENGTH_LONG).show();
            dismiss();
            return;
        }

        // Tạo BarcodeView
        barcodeView = new DecoratedBarcodeView(getContext());
        barcodeView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // Cấu hình decoder chỉ quét QR code
        barcodeView.getBarcodeView().setDecoderFactory(
                new DefaultDecoderFactory(Collections.singleton(BarcodeFormat.QR_CODE))
        );

        // Thêm vào container
        cameraPreview.addView(barcodeView);

        // Ẩn placeholder khi camera khởi động
        layoutPlaceholder.setVisibility(View.GONE);

        // Bắt đầu quét
        startScanning();
    }

    private void startScanning() {
        if (barcodeView != null && !isScanning) {
            barcodeView.decodeContinuous(new BarcodeCallback() {
                @Override
                public void barcodeResult(BarcodeResult result) {
                    if (result != null && result.getText() != null) {
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

        if (listener != null) {
            listener.onQRScanned(code);
        }

        dismiss();
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