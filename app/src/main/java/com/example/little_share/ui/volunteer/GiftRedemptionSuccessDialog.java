package com.example.little_share.ui.volunteer;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.little_share.R;
import com.example.little_share.helper.ImageSaver;
import com.example.little_share.utils.QRCodeGenerator;

public class GiftRedemptionSuccessDialog extends Dialog {

    private String giftName;
    private String qrCode;
    private OnDialogActionListener listener;

    // Constructor mới - bỏ remainingPoints vì chưa trừ điểm
    public GiftRedemptionSuccessDialog(@NonNull Context context, String giftName, String qrCode) {
        super(context, android.R.style.Theme_Material_Dialog);
        this.giftName = giftName;
        this.qrCode = qrCode;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_gift_redemption_success);

        // Không cho phép cancel bằng cách touch outside
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        initViews();
        generateAndDisplayQR();
    }

    private void initViews() {
        ImageView ivSuccessIcon = findViewById(R.id.ivSuccessIcon);
        TextView tvDialogTitle = findViewById(R.id.tvDialogTitle);
        TextView tvDialogRemainingPoints = findViewById(R.id.tvDialogRemainingPoints);
        Button btnDialogComplete = findViewById(R.id.btnDialogComplete);
        Button btnSaveQR = findViewById(R.id.btnSaveQR); // Nếu có trong layout

        // Set icon tick
        ivSuccessIcon.setImageResource(R.drawable.icon_check);

        tvDialogTitle.setText("Yêu cầu đổi " + giftName + " thành công!");
        tvDialogRemainingPoints.setText("Vui lòng mang QR code này đến địa điểm nhận quà.\nĐiểm sẽ được trừ khi tổ chức quét QR code.");

        btnDialogComplete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComplete();
            }
            dismiss();
        });

        // Nếu có nút lưu QR
        if (btnSaveQR != null) {
            btnSaveQR.setOnClickListener(v -> saveQRCode());
        }
    }

    // Phương thức lưu QR code sử dụng ImageSaver helper
    private void saveQRCode() {
        try {
            // Tạo lại bitmap QR code để đảm bảo chất lượng tốt nhất
            Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrCode, 512);
            
            if (qrBitmap != null) {
                // Tạo tên file dựa trên tên quà
                String fileName = "QR_" + giftName.replaceAll("[^a-zA-Z0-9]", "_");
                
                // Sử dụng ImageSaver helper để lưu ảnh
                ImageSaver.saveQRCodeToGallery(getContext(), qrBitmap, fileName, new ImageSaver.OnImageSavedListener() {
                    @Override
                    public void onSuccess(String filePath) {
                        android.widget.Toast.makeText(getContext(), 
                            "Đã lưu QR code vào thư viện ảnh thành công!", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        android.widget.Toast.makeText(getContext(), 
                            "Lỗi khi lưu QR code: " + error, 
                            android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                android.widget.Toast.makeText(getContext(), 
                    "Không thể tạo QR code để lưu", 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(getContext(), 
                "Lỗi không mong muốn khi lưu QR code", 
                android.widget.Toast.LENGTH_SHORT).show();
        }
    }




    private void generateAndDisplayQR() {
        ImageView ivDialogQRCode = findViewById(R.id.ivDialogQRCode);

        // Sử dụng QRCodeGenerator hiện có
        Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrCode, 400);
        if (qrBitmap != null) {
            ivDialogQRCode.setImageBitmap(qrBitmap);
        }
    }

    public void setOnDialogActionListener(OnDialogActionListener listener) {
        this.listener = listener;
    }

    public interface OnDialogActionListener {
        void onComplete();
    }
}
