package com.example.little_share.ui.sponsor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.FinancialReport;
import com.example.little_share.data.models.ReportExpense;
import com.example.little_share.data.models.ReportImage;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.data.repositories.ReportRepository;
import com.example.little_share.ui.ngo.adapter.ExpenseAdapter;
import com.example.little_share.ui.ngo.adapter.ReportImageAdapter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class frm_sponsor_campaign_detail_report extends Fragment {
    private static final String TAG = "SponsorReportDetail";

    private ImageView btnBack;
    private TextView tvCampaignName, tvDate, tvLocation, tvTotalSpending, tvVolunteerCount, tvExpenseTotal;
    private RecyclerView rvExpenses, rvImages;

    private ReportRepository reportRepository;
    private CampaignRepository campaignRepository;
    private String campaignId, campaignName;
    private Campaign campaign;
    private FinancialReport currentReport;

    private ExpenseAdapter expenseAdapter;
    private ReportImageAdapter imageAdapter;
    private List<ReportExpense> expenses;
    private List<ReportImage> images;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sponsor_campaign_detail_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        getBundleArguments();
        setupRepositories();
        setupRecyclerViews();
        setupListeners();
        loadCampaignInfo();
        loadReportData();
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        tvCampaignName = view.findViewById(R.id.tvCampaignName);
        tvDate = view.findViewById(R.id.tvDate);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvTotalSpending = view.findViewById(R.id.tvTotalSpending);
        tvVolunteerCount = view.findViewById(R.id.tvVolunteerCount);
        tvExpenseTotal = view.findViewById(R.id.tvExpenseTotal);
        rvExpenses = view.findViewById(R.id.rvExpenses);
        rvImages = view.findViewById(R.id.rvImages);

        expenses = new ArrayList<>();
        images = new ArrayList<>();
    }

    private void getBundleArguments() {
        if (getArguments() != null) {
            campaignId = getArguments().getString("campaign_id");
            campaignName = getArguments().getString("campaign_name");
            campaign = (Campaign) getArguments().getSerializable("campaign_data");

            Log.d(TAG, "campaignId: " + campaignId);
            Log.d(TAG, "campaignName: " + campaignName);
        }
    }

    private void setupRepositories() {
        reportRepository = new ReportRepository();
        campaignRepository = new CampaignRepository();
    }

    private void setupRecyclerViews() {
        // Expenses RecyclerView (READ-ONLY - pass null as listener)
        expenseAdapter = new ExpenseAdapter(expenses, null);
        rvExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExpenses.setAdapter(expenseAdapter);

        // Images RecyclerView (READ-ONLY - pass null as listener)
        imageAdapter = new ReportImageAdapter(images, null);
        rvImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void loadCampaignInfo() {
        // If campaign object is provided, use it
        if (campaign != null) {
            displayCampaignInfo(campaign);
        }
        // Otherwise load from Firebase
        else if (campaignId != null) {
            campaignRepository.getCampaignById(campaignId).observe(getViewLifecycleOwner(), loadedCampaign -> {
                if (loadedCampaign != null && isAdded()) {
                    campaign = loadedCampaign;
                    displayCampaignInfo(campaign);
                }
            });
        }
        // Fallback: use campaign name if available
        else if (campaignName != null) {
            tvCampaignName.setText(campaignName);
        }
    }

    private void displayCampaignInfo(Campaign campaign) {
        // Campaign name
        tvCampaignName.setText(campaign.getName());

        // Date
        if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateRange = sdf.format(campaign.getStartDate()) + " - " + sdf.format(campaign.getEndDate());
            tvDate.setText(dateRange);
        }

        // Location
        String location = campaign.getLocation();
        if (campaign.getSpecificLocation() != null && !campaign.getSpecificLocation().isEmpty()) {
            location += " - " + campaign.getSpecificLocation();
        }
        tvLocation.setText(location);
    }

    private void loadReportData() {
        if (campaignId == null) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin báo cáo", Toast.LENGTH_SHORT).show();
            return;
        }

        reportRepository.getReportByCampaignId(campaignId).observe(getViewLifecycleOwner(), report -> {
            if (report != null && isAdded()) {
                currentReport = report;
                displayReportData(report);
            } else {
                Log.w(TAG, "No report found for campaign: " + campaignId);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Chưa có báo cáo cho chiến dịch này", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayReportData(FinancialReport report) {
        // Total spending
        DecimalFormat formatter = new DecimalFormat("#,###");
        tvTotalSpending.setText(formatter.format(report.getTotalExpense()) + " VND");

        // Volunteers
        tvVolunteerCount.setText(report.getTotalVolunteers() + " người");

        // Expenses
        if (report.getExpenses() != null && !report.getExpenses().isEmpty()) {
            expenses.clear();
            expenses.addAll(report.getExpenses());
            expenseAdapter.notifyDataSetChanged();

            // Calculate total
            double total = 0;
            for (ReportExpense expense : expenses) {
                total += expense.getAmount();
            }
            tvExpenseTotal.setText(formatMoney(total) + " đ");
        } else {
            tvExpenseTotal.setText("0 đ");
        }

        // Images
        if (report.getImages() != null && !report.getImages().isEmpty()) {
            images.clear();
            images.addAll(report.getImages());
            imageAdapter.notifyDataSetChanged();
        }

        Log.d(TAG, "Displayed report: " + report.getCampaignName());
        Log.d(TAG, "Expenses: " + expenses.size());
        Log.d(TAG, "Images: " + images.size());
    }

    private String formatMoney(double amount) {
        if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000);
        }
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }
}