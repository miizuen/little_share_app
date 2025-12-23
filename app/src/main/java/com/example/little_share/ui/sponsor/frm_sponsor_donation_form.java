package com.example.little_share.ui.sponsor;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;

import java.text.DecimalFormat;

public class frm_sponsor_donation_form extends Fragment {

    private android.widget.Button btnSponsor, btnCancel;
    private Campaign currentCampaign;
    private EditText etAmount, etNote;
    private TextView tvCampaignName, tvOrganizationName, tvRemaining;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_sponsor_donation_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        getCampaignDataFromBundle();
        setupClickListeners();
    }

    public static frm_sponsor_donation_form newInstance(String campaignId, String campaignName,
                                                        String organizationName, double targetBudget,
                                                        double currentBudget) {
        frm_sponsor_donation_form fragment = new frm_sponsor_donation_form();
        Bundle args = new Bundle();

        // Tạo Campaign object
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setName(campaignName);
        campaign.setOrganizationName(organizationName);
        campaign.setTargetBudget(targetBudget);
        campaign.setCurrentBudget(currentBudget);

        args.putSerializable("campaign_data", campaign);
        fragment.setArguments(args);

        return fragment;
    }

    private void initViews(View view) {
        btnSponsor = view.findViewById(R.id.btnSponsor);
        btnCancel = view.findViewById(R.id.btnCancel);

        tvCampaignName = view.findViewById(R.id.tvCampaignName);
        tvOrganizationName = view.findViewById(R.id.tvOrganization);
        tvRemaining = view.findViewById(R.id.tvRemaining);
        etAmount = view.findViewById(R.id.etAmount);
        etNote = view.findViewById(R.id.etNote);
    }

    private void getCampaignDataFromBundle() {
        Bundle args = getArguments();
        if (args != null) {
            currentCampaign = (Campaign) args.getSerializable("campaign_data");
            if (currentCampaign != null) {
                android.util.Log.d("DONATION_FORM", "Received campaign: " + currentCampaign.getName());
                displayCampaignInfo();
            } else {
                android.util.Log.e("DONATION_FORM", "Campaign data is null");
            }
        } else {
            android.util.Log.e("DONATION_FORM", "Arguments is null");
        }
    }

    private void displayCampaignInfo() {
        if (currentCampaign != null) {
            // Hiển thị tên chiến dịch
            if (tvCampaignName != null) {
                tvCampaignName.setText(currentCampaign.getName());
            }

            // Hiển thị tên tổ chức
            if (tvOrganizationName != null) {
                tvOrganizationName.setText(currentCampaign.getOrganizationName());
            }

            // Tính và hiển thị số tiền còn thiếu
            if (tvRemaining != null) {
                double targetBudget = currentCampaign.getTargetBudget();
                double currentBudget = currentCampaign.getCurrentBudget();
                double remaining = targetBudget - currentBudget;

                android.util.Log.d("DONATION_FORM", "Target: " + targetBudget);
                android.util.Log.d("DONATION_FORM", "Current: " + currentBudget);
                android.util.Log.d("DONATION_FORM", "Remaining: " + remaining);

                if (remaining > 0) {
                    String remainingText = "💡 Chiến dịch còn cần: " + formatMoneyWithCommas(remaining) + " VNĐ";
                    tvRemaining.setText(remainingText);
                    tvRemaining.setVisibility(View.VISIBLE);
                } else {
                    tvRemaining.setText("✅ Chiến dịch đã đạt mục tiêu!");
                    tvRemaining.setTextColor(getResources().getColor(R.color.primary_green));
                    tvRemaining.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setupClickListeners() {
        btnSponsor.setOnClickListener(v -> {
            android.util.Log.d("DONATION_FORM", "=== Sponsor button clicked ===");

            // Lấy dữ liệu từ form
            String donationAmount = etAmount.getText().toString().trim();
            String message = etNote.getText().toString().trim();

            android.util.Log.d("DONATION_FORM", "Amount: " + donationAmount);
            android.util.Log.d("DONATION_FORM", "Message: " + message);
            android.util.Log.d("DONATION_FORM", "Campaign: " + (currentCampaign != null ? currentCampaign.getName() : "NULL"));

            // Validate amount
            if (donationAmount.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate campaign data
            if (currentCampaign == null) {
                Toast.makeText(getContext(), "Lỗi: Không có dữ liệu chiến dịch", Toast.LENGTH_SHORT).show();
                android.util.Log.e("DONATION_FORM", "currentCampaign is null");
                return;
            }

            // Kiểm tra context trước khi start activity
            if (getActivity() == null) {
                Toast.makeText(getContext(), "Lỗi: Không thể mở trang thanh toán", Toast.LENGTH_SHORT).show();
                android.util.Log.e("DONATION_FORM", "Activity is null!");
                return;
            }

            // Chuyển sang activity payment confirm
            Intent intent = new Intent(getActivity(), activity_sponsor_payment_confirm.class);

            // Truyền dữ liệu campaign
            intent.putExtra("campaign_id", currentCampaign.getId());
            intent.putExtra("campaign_name", currentCampaign.getName());
            intent.putExtra("organization_name", currentCampaign.getOrganizationName());

            // Truyền dữ liệu từ form
            intent.putExtra("donation_amount", donationAmount);
            intent.putExtra("message", message);

            // Truyền cả object campaign
            intent.putExtra("campaign_data", currentCampaign);

            try {
                android.util.Log.d("DONATION_FORM", "Starting payment confirm activity...");
                startActivity(intent);
                android.util.Log.d("DONATION_FORM", "Payment activity started successfully");
            } catch (Exception e) {
                android.util.Log.e("DONATION_FORM", "Error starting activity", e);
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            android.util.Log.d("DONATION_FORM", "Cancel button clicked");

            // Quay về trang home
            Fragment homeFrm = new frm_sponsor_home();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, homeFrm)
                    .commit();
        });
    }

    private String formatMoney(double amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM VNĐ", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.0fK VNĐ", amount / 1000);
        } else {
            return String.format("%.0f VNĐ", amount);
        }
    }

    private String formatMoneyWithCommas(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }
}