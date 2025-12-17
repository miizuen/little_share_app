package com.example.little_share.ui.sponsor;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.helper.helperZaloPay.Api.CreateOrder;

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
                                    Log.d("ZALOPAY", "✅ Payment SUCCESS");
                                    Intent intent1 = new Intent(activity_sponsor_payment_confirm.this,
                                            dialog_donation_success_sponsor.class);
                                    intent1.putExtra("result","Thanh toán thành công");
                                    startActivity(intent1);
                                    finish();
                                }

                                @Override
                                public void onPaymentCanceled(String s, String s1) {
                                    Log.d("ZALOPAY", "⚠️ Payment CANCELED");
                                    Intent intent1 = new Intent(activity_sponsor_payment_confirm.this,
                                            dialog_donation_success_sponsor.class);
                                    intent1.putExtra("result","Hủy thanh toán");
                                    startActivity(intent1);
                                    finish();
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
    }

}
