package com.example.little_share.ui.ngo;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.FinancialReport;
import com.example.little_share.data.repositories.ReportRepository;
import com.example.little_share.ui.ngo.adapter.ReportAdapter;

import java.util.ArrayList;
import java.util.List;

public class activity_ngo_finance_report extends AppCompatActivity {
    private static final String TAG = "FinanceReport";

    private EditText edtSearch;
    private Button btnCreateReport;
    private RecyclerView rvDonationsPending;

    private ReportRepository reportRepository;
    private ReportAdapter reportAdapter;
    private List<FinancialReport> allReports;
    private List<FinancialReport> filteredReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngo_finance_report);

        initViews();
        setupRepository();
        setupRecyclerView();
        setupListeners();
        loadReports();
    }

    private void initViews() {
        edtSearch = findViewById(R.id.edtSearch);
        btnCreateReport = findViewById(R.id.btnCreateReport);
        rvDonationsPending = findViewById(R.id.rvDonationsPending);

        allReports = new ArrayList<>();
        filteredReports = new ArrayList<>();
    }

    private void setupRepository() {
        reportRepository = new ReportRepository();
    }

    private void setupRecyclerView() {
        reportAdapter = new ReportAdapter(this, filteredReports, new ReportAdapter.OnReportClickListener() {
            @Override
            public void onReportClick(FinancialReport report) {
                // Xử lý click vào item
                Toast.makeText(activity_ngo_finance_report.this,
                        "Clicked: " + report.getCampaignName(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDetailClick(FinancialReport report) {
                // Mở màn hình chi tiết báo cáo
                openReportDetail(report);
            }
        });

        rvDonationsPending.setLayoutManager(new LinearLayoutManager(this));
        rvDonationsPending.setAdapter(reportAdapter);

        Log.d(TAG, "RecyclerView setup completed");
    }

    private void setupListeners() {
        // Nút tạo báo cáo
        btnCreateReport.setOnClickListener(v -> {
            Intent intent = new Intent(this, activity_create_report.class);
            startActivity(intent);
        });

        // Tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReports(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadReports() {
        Log.d(TAG, "Loading reports...");

        reportRepository.getReportsByCurrentNgo().observe(this, reports -> {
            if (reports != null) {
                Log.d(TAG, "Received " + reports.size() + " reports");

                allReports.clear();
                allReports.addAll(reports);

                filteredReports.clear();
                filteredReports.addAll(reports);

                reportAdapter.notifyDataSetChanged();

                if (reports.isEmpty()) {
                    Toast.makeText(this, "Chưa có báo cáo nào", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "First report: " + reports.get(0).getCampaignName());
                }
            } else {
                Log.e(TAG, "Reports is null");
                Toast.makeText(this, "Lỗi khi tải báo cáo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterReports(String query) {
        filteredReports.clear();

        if (query.isEmpty()) {
            filteredReports.addAll(allReports);
        } else {
            String lowerQuery = query.toLowerCase();
            for (FinancialReport report : allReports) {
                // Tìm theo tên chiến dịch hoặc ID báo cáo
                if ((report.getCampaignName() != null &&
                        report.getCampaignName().toLowerCase().contains(lowerQuery)) ||
                        (report.getId() != null &&
                                report.getId().toLowerCase().contains(lowerQuery))) {
                    filteredReports.add(report);
                }
            }
        }

        reportAdapter.notifyDataSetChanged();
        Log.d(TAG, "Filtered: " + filteredReports.size() + " reports");
    }

    private void openReportDetail(FinancialReport report) {
        // TODO: Tạo màn hình chi tiết báo cáo
        Intent intent = new Intent(this, acitivity_ngo_export_report.class);
        intent.putExtra("REPORT_ID", report.getId());
        intent.putExtra("CAMPAIGN_ID", report.getCampaignId());
        intent.putExtra("CAMPAIGN_NAME", report.getCampaignName());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data khi quay lại màn hình
        Log.d(TAG, "onResume - refreshing data");
    }
}