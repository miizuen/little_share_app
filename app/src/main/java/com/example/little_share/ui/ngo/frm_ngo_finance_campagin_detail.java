package com.example.little_share.ui.ngo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;

import java.text.NumberFormat;
import java.util.Locale;

public class frm_ngo_finance_campagin_detail extends Fragment {
    private static final String TAG = "FinanceDetail";

    private TextView tvProgressNumber, tvCurrentAmount, tvTargetAmount;
    private ProgressBar progressBar;
    private TextView tvEmptySponsors;
    private CampaignRepository campaignRepository;
    private Campaign currentCampaign;
    private String campaignId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_ngo_finance_campaign_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        
        campaignRepository = new CampaignRepository();
        
        // Lấy campaign data từ arguments
        if (getArguments() != null) {
            campaignId = getArguments().getString("campaignId");
            currentCampaign = (Campaign) getArguments().getSerializable("campaign");
            
            if (currentCampaign != null) {
                loadFinanceData();
            }
        }
    }

    private void initViews(View view) {
        tvProgressNumber = view.findViewById(R.id.tvProgressNumber);
        tvCurrentAmount = view.findViewById(R.id.tvCurrentAmount);
        tvTargetAmount = view.findViewById(R.id.tvTargetAmount);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Tạo TextView empty state nếu chưa có
        tvEmptySponsors = new TextView(getContext());
        tvEmptySponsors.setText("Chưa có nhà tài trợ nào");
        tvEmptySponsors.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvEmptySponsors.setVisibility(View.GONE);
    }


    private void loadFinanceData() {
        if (campaignId == null) {
            Log.e(TAG, "Campaign ID is null");
            return;
        }

        Log.d(TAG, "Loading finance data for campaign: " + campaignId);

        // Hiển thị mục tiêu từ campaign
        if (currentCampaign != null) {
            tvTargetAmount.setText(formatMoney(currentCampaign.getTargetBudget()) + "vnd");
        }

        // Load tổng tiền đã gây được
        campaignRepository.getTotalRaisedForCampaign(campaignId)
                .observe(getViewLifecycleOwner(), totalRaised -> {
                    if (totalRaised != null && isAdded()) {
                        tvCurrentAmount.setText(formatMoney(totalRaised) + "vnd");

                        // Tính phần trăm tiến độ
                        if (currentCampaign != null) {
                            double targetBudget = currentCampaign.getTargetBudget();
                            int progress = targetBudget > 0 ? (int) ((totalRaised / targetBudget) * 100) : 0;
                            
                            // Giới hạn progress không quá 100%
                            progress = Math.min(progress, 100);
                            
                            if (progressBar != null) {
                                progressBar.setProgress(progress);
                            }
                            tvProgressNumber.setText(progress + "%");
                            
                            Log.d(TAG, "Updated progress: " + progress + "% (" + totalRaised + "/" + targetBudget + ")");
                        }
                    }
                });
    }

    private String formatMoney(double amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}