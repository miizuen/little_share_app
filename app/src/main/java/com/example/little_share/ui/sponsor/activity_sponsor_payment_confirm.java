package com.example.little_share.ui.sponsor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.SponsorDonation;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.data.repositories.NotificationRepository;
import com.example.little_share.helper.helperZaloPay.Api.CreateOrder;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.text.DecimalFormat;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class activity_sponsor_payment_confirm extends AppCompatActivity {

    private TextView tvCampaignName, tvDonationAmount;
    private Button btnPayWithZaloPay;

    private Campaign currentCampaign;
    private String etAmount, etNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsor_payment_confirm);

        initViews();
        getDataFromIntent();
        displayPaymentInfo();
        setupClickListeners();

        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder().permitAll().build()
        );
        // Init ZaloPay SDK
        ZaloPaySDK.init(2553, Environment.SANDBOX);
    }

    private void initViews() {
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvDonationAmount = findViewById(R.id.tvAmount);
        btnPayWithZaloPay = findViewById(R.id.btnPayWithZaloPay);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();

        currentCampaign = (Campaign) intent.getSerializableExtra("campaign_data");
        etAmount = intent.getStringExtra("donation_amount");
        etNote = intent.getStringExtra("message");

        Log.d("PAYMENT_CONFIRM", "Campaign: " +
                (currentCampaign != null ? currentCampaign.getName() : "NULL"));
        Log.d("PAYMENT_CONFIRM", "Amount: " + etAmount);
        Log.d("PAYMENT_CONFIRM", "Message: " + etNote);
    }

    private void displayPaymentInfo() {
        if (currentCampaign != null) {
            tvCampaignName.setText(currentCampaign.getName());
        }

        if (etAmount != null) {
            tvDonationAmount.setText(formatMoney(Double.parseDouble(etAmount)) + " VNĐ");
        }
    }

    private String formatMoney(double amount) {
        return new DecimalFormat("#,###").format(amount);
    }

    private void setupClickListeners() {
        // Trong setupClickListeners(), thêm try-catch chi tiết hơn:
        btnPayWithZaloPay.setOnClickListener(v -> {
            try {
                Log.d("PAYMENT_CONFIRM", "=== Starting ZaloPay payment ===");
                Log.d("PAYMENT_CONFIRM", "Amount: " + etAmount);

                CreateOrder orderApi = new CreateOrder();
                JSONObject data = orderApi.createOrder(etAmount);

                Log.d("PAYMENT_CONFIRM", "Order response: " + data.toString());

                String returnCode = data.getString("return_code");
                Log.d("PAYMENT_CONFIRM", "Return code: " + returnCode);

                if ("1".equals(returnCode)) {
                    String token = data.getString("zp_trans_token");
                    Log.d("PAYMENT_CONFIRM", "Token: " + token);

                    ZaloPaySDK.getInstance().payOrder(
                            activity_sponsor_payment_confirm.this,
                            token,
                            "demozpdk://app",
                            new PayOrderListener() {
                                @Override
                                public void onPaymentSucceeded(String s, String s1, String s2) {
                                    Log.d("ZALOPAY", "✅ Payment SUCCESS - waiting for callback");
                                    // KHÔNG chuyển trang ở đây, chờ callback từ ZaloPay app
                                }

                                @Override
                                public void onPaymentCanceled(String s, String s1) {
                                    Log.d("ZALOPAY", "⚠️ Payment CANCELED");
                                    Toast.makeText(activity_sponsor_payment_confirm.this,
                                            "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPaymentError(ZaloPayError error, String s, String s1) {
                                    Log.e("ZALOPAY", "❌ Payment ERROR: " + error.toString());
                                    Toast.makeText(activity_sponsor_payment_confirm.this,
                                            "Lỗi thanh toán: " + error.toString(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                } else {
                    String returnMessage = data.optString("return_message", "Không rõ lỗi");
                    Log.e("PAYMENT_CONFIRM", "❌ Create order failed: " + returnMessage);
                    Toast.makeText(this, "Không tạo được đơn hàng: " + returnMessage,
                            Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Log.e("PAYMENT_CONFIRM", "❌ Exception: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);

        // Kiểm tra callback từ ZaloPay
        Uri data = intent.getData();
        if (data != null && "demozpdk".equals(data.getScheme())) {
            Log.d("ZALOPAY", "✅ Received callback from ZaloPay - processing payment success");
            
            // Lưu donation vào Firebase trước khi chuyển trang
            onPaymentSuccess("zalopay_" + System.currentTimeMillis());
        }
    }

    private void onPaymentSuccess(String transactionId) {
        // Tạo SponsorDonation object
        SponsorDonation donation = new SponsorDonation();
        donation.setSponsorId(getCurrentUserId());
        donation.setSponsorName(getCurrentUserName());
        donation.setCampaignId(currentCampaign.getId());
        donation.setCampaignName(currentCampaign.getName());
        donation.setOrganizationName(currentCampaign.getOrganizationName());
        donation.setAmount(Double.parseDouble(etAmount));
        donation.setMessage(etNote);
        donation.setTransactionId(transactionId);
        donation.setStatus("COMPLETED");
        donation.setPaymentMethod("ZaloPay");
        
        // Lưu vào Firebase
        CampaignRepository campaignRepository = new CampaignRepository();
        campaignRepository.saveDonation(donation, new CampaignRepository.OnDonationSaveListener() {
            @Override
            public void onSuccess() {
                Log.d("PAYMENT_CONFIRM", "Donation saved successfully");
                
                // Tạo notification cho donation thành công
                NotificationRepository notificationRepository = new NotificationRepository();
                notificationRepository.createDonationSuccessNotification(
                    currentCampaign.getName(), 
                    Double.parseDouble(etAmount)
                );
                
                // Chuyển đến trang thành công
                showSuccessDialog();
            }
            
            @Override
            public void onFailure(String error) {
                Log.e("PAYMENT_CONFIRM", "Failed to save donation: " + error);
                Toast.makeText(activity_sponsor_payment_confirm.this, 
                    "Lỗi lưu thông tin: " + error, Toast.LENGTH_SHORT).show();
                // Vẫn chuyển đến trang thành công vì đã thanh toán
                showSuccessDialog();
            }
        });
    }
    private void showSuccessDialog() {
        Log.d("PAYMENT_CONFIRM", "=== SHOWING SUCCESS DIALOG ===");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_donation_success_sponsor, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Setup dialog content
        TextView tvAmount = dialogView.findViewById(R.id.tvAmount);
        TextView tvCampaignName = dialogView.findViewById(R.id.tvCampaignName);
        Button btnBackToHome = dialogView.findViewById(R.id.btnBackToHome);

        if (tvAmount != null) {
            tvAmount.setText(formatMoney(Double.parseDouble(etAmount)) + " VNĐ");
            Log.d("PAYMENT_CONFIRM", "Set amount: " + etAmount);
        }
        if (tvCampaignName != null) {
            tvCampaignName.setText(currentCampaign.getName());
            Log.d("PAYMENT_CONFIRM", "Set campaign: " + currentCampaign.getName());
        }

        if (btnBackToHome != null) {
            btnBackToHome.setOnClickListener(v -> {
                Log.d("PAYMENT_CONFIRM", "Back to home clicked");
                dialog.dismiss();
                navigateToHomeWithRefresh();
            });
        }

        dialog.show();
        Log.d("PAYMENT_CONFIRM", "Dialog shown successfully");
    }

    private void navigateToHomeWithRefresh() {
        Log.d("PAYMENT_CONFIRM", "=== NAVIGATING TO HOME WITH REFRESH ===");

        Intent intent = new Intent(this, activity_sponsor_main.class);

        // QUAN TRỌNG: Sử dụng FLAG này để không tạo activity mới
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Set flags để refresh
        intent.putExtra("refresh_sponsored", true);

        Log.d("PAYMENT_CONFIRM", "Starting main activity with refresh_sponsored=true");
        startActivity(intent);
        finish();
    }


    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private String getCurrentUserName() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getDisplayName() : "Nhà tài trợ";
    }


}
