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
import com.example.little_share.utils.QRCodeGenerator;

public class GiftRedemptionSuccessDialog extends Dialog {

    private String giftName;
    private int remainingPoints;
    private String qrCode;
    private OnDialogActionListener listener;

    public GiftRedemptionSuccessDialog(@NonNull Context context, String giftName,
                                       int remainingPoints, String qrCode) {
        super(context, android.R.style.Theme_Material_Dialog); // Thay đổi ở đây
        this.giftName = giftName;
        this.remainingPoints = remainingPoints;
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

        // Set icon tick
        ivSuccessIcon.setImageResource(R.drawable.icon_check);

        tvDialogTitle.setText("Đổi " + giftName + " thành công");
        tvDialogRemainingPoints.setText("Điểm còn lại: " + remainingPoints);

        btnDialogComplete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComplete();
            }
            dismiss();
        });
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
