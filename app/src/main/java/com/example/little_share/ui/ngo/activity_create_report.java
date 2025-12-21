package com.example.little_share.ui.ngo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.FinancialReport;
import com.example.little_share.data.models.ReportExpense;
import com.example.little_share.data.models.ReportImage;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.data.repositories.ReportRepository;
import com.example.little_share.ui.ngo.adapter.ReportImageAdapter;
import com.example.little_share.helper.ImgBBUploader;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class activity_create_report extends AppCompatActivity {
    private static final String TAG = "CreateReport";
    private static final int MAX_IMAGES = 10;

    private ImageView btnBack;
    private AutoCompleteTextView spinnerCampaign;
    private LinearLayout layoutCampaignInfo;
    private TextView tvCampaignInfo;
    private TextInputEditText edtReportContent;
    private Button btnAddExpense, btnCancel;
    private MaterialButton btnAddImages, btnCreate;
    private RecyclerView recyclerReportImages;
    private CampaignRepository campaignRepository;
    private ReportRepository reportRepository;
    private List<Campaign> sponsoredCampaigns;
    private Campaign selectedCampaign;
    private List<ReportExpense> expenses;
    private List<ReportImage> reportImages;
    private ReportImageAdapter imageAdapter;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        initViews();
        setupRepositories();
        setupImagePicker();
        setupRecyclerView();
        loadSponsoredCampaigns();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerCampaign = findViewById(R.id.spinnerCampaign);
        layoutCampaignInfo = findViewById(R.id.layoutCampaignInfo);
        tvCampaignInfo = findViewById(R.id.tvCampaignInfo);
        edtReportContent = findViewById(R.id.edtReportContent);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnAddImages = findViewById(R.id.btnAddImages);
        recyclerReportImages = findViewById(R.id.recyclerReportImages);
        btnCancel = findViewById(R.id.btnCancel);
        btnCreate = findViewById(R.id.btnCreate);

        expenses = new ArrayList<>();
        reportImages = new ArrayList<>();
    }

    private void setupRepositories() {
        campaignRepository = new CampaignRepository();
        reportRepository = new ReportRepository();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        handleImageSelection(result.getData());
                    }
                });
    }

    private void setupRecyclerView() {
        imageAdapter = new ReportImageAdapter(reportImages, position -> {
            reportImages.remove(position);
            imageAdapter.notifyItemRemoved(position);
        });

        recyclerReportImages.setLayoutManager(new LinearLayoutManager(this));
        recyclerReportImages.setAdapter(imageAdapter);
    }

    private void loadSponsoredCampaigns() {
        Log.d(TAG, "Loading campaigns with donations...");

        campaignRepository.getCampaignsWithDonations().observe(this, campaigns -> {
            if (campaigns != null && !campaigns.isEmpty()) {
                Log.d(TAG, "Found " + campaigns.size() + " campaigns with donations");
                sponsoredCampaigns = campaigns;
                setupCampaignSpinner(campaigns);
            } else {
                Log.e(TAG, "No campaigns with donations found");
                Toast.makeText(this, "Không có chiến dịch nào đã được tài trợ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupCampaignSpinner(List<Campaign> campaigns) {
        List<String> campaignNames = new ArrayList<>();
        for (Campaign campaign : campaigns) {
            campaignNames.add(campaign.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                campaignNames
        );

        spinnerCampaign.setAdapter(adapter);
        spinnerCampaign.setOnItemClickListener((parent, view, position, id) -> {
            selectedCampaign = campaigns.get(position);
            showCampaignInfo(selectedCampaign);
        });
    }

    private void showCampaignInfo(Campaign campaign) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDate = campaign.getStartDate() != null ? sdf.format(campaign.getStartDate()) : "N/A";
        String endDate = campaign.getEndDate() != null ? sdf.format(campaign.getEndDate()) : "N/A";

        String info = "Tên: " + campaign.getName() + "\n" +
                "Ngân sách: " + String.format("%,.0f VNĐ", campaign.getCurrentBudget()) + "\n" +
                "Thời gian: " + startDate + " - " + endDate;

        tvCampaignInfo.setText(info);
        layoutCampaignInfo.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        btnAddImages.setOnClickListener(v -> {
            if (reportImages.size() >= MAX_IMAGES) {
                Toast.makeText(this, "Chỉ được thêm tối đa " + MAX_IMAGES + " ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker();
        });

        btnCancel.setOnClickListener(v -> finish());

        btnCreate.setOnClickListener(v -> createReport());
    }

    private void showAddExpenseDialog() {
        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ngo_add_spending, null);

        // Find views
        ImageView btnClose = dialogView.findViewById(R.id.btnClose);
        EditText txtHangMuc = dialogView.findViewById(R.id.txtHangMuc);
        EditText txtSoTien = dialogView.findViewById(R.id.txtSoTien);
        LinearLayout btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        TextView txtNgayChi = dialogView.findViewById(R.id.txtNgayChi);
        EditText txtGhiChu = dialogView.findViewById(R.id.txtGhiChu);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnAdd = dialogView.findViewById(R.id.btnAdd);

        final Date[] selectedDate = {new Date()};
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        txtNgayChi.setText(sdf.format(selectedDate[0]));
        txtNgayChi.setTextColor(0xFF000000);

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Date picker
        btnPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        selectedDate[0] = calendar.getTime();
                        txtNgayChi.setText(sdf.format(selectedDate[0]));
                        txtNgayChi.setTextColor(0xFF000000);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Close button
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Add button
        btnAdd.setOnClickListener(v -> {
            String category = txtHangMuc.getText().toString().trim();
            String amountStr = txtSoTien.getText().toString().trim();
            String notes = txtGhiChu.getText().toString().trim();

            if (category.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                ReportExpense expense = new ReportExpense(null, category, amount, selectedDate[0]);
                expense.setNotes(notes);
                expenses.add(expense);

                Toast.makeText(this, "Đã thêm khoản chi tiêu", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelection(Intent data) {
        if (data.getClipData() != null) {
            // Multiple images
            int count = Math.min(data.getClipData().getItemCount(), MAX_IMAGES - reportImages.size());
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                addImage(imageUri);
            }
        } else if (data.getData() != null) {
            // Single image
            Uri imageUri = data.getData();
            addImage(imageUri);
        }
    }

    private void addImage(Uri imageUri) {
        ReportImage reportImage = new ReportImage(null, imageUri.toString());
        reportImages.add(reportImage);
        imageAdapter.notifyItemInserted(reportImages.size() - 1);
    }

    private void createReport() {
        if (selectedCampaign == null) {
            Toast.makeText(this, "Vui lòng chọn chiến dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = edtReportContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung báo cáo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate total expense
        double totalExpense = 0;
        for (ReportExpense expense : expenses) {
            totalExpense += expense.getAmount();
        }

        // Create report
        FinancialReport report = new FinancialReport(selectedCampaign.getId(), null);
        report.setCampaignName(selectedCampaign.getName());
        report.setDescription(content);
        report.setTotalExpense(totalExpense);
        report.setTotalVolunteers(selectedCampaign.getCurrentVolunteers());
        report.setExpenses(expenses);

        // Upload images if any
        if (!reportImages.isEmpty()) {
            uploadImagesAndCreateReport(report);
        } else {
            saveReport(report);
        }
    }

    private void uploadImagesAndCreateReport(FinancialReport report) {
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

        List<ReportImage> uploadedImages = new ArrayList<>();
        int[] uploadCount = {0};
        int[] successCount = {0};
        int totalImages = reportImages.size();

        for (ReportImage image : reportImages) {
            if (image.getImageUrl().startsWith("content://")) {
                // Upload to ImgBB
                Uri imageUri = Uri.parse(image.getImageUrl());

                ImgBBUploader.uploadImage(this, imageUri, new ImgBBUploader.UploadListener() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Image uploaded successfully: " + imageUrl);
                            ReportImage uploadedImage = new ReportImage(null, imageUrl);
                            uploadedImages.add(uploadedImage);
                            successCount[0]++;
                            uploadCount[0]++;

                            // Update progress
                            int progress = (uploadCount[0] * 100) / totalImages;
                            Toast.makeText(activity_create_report.this,
                                    "Đang tải: " + uploadCount[0] + "/" + totalImages,
                                    Toast.LENGTH_SHORT).show();

                            if (uploadCount[0] == totalImages) {
                                report.setImages(uploadedImages);
                                saveReport(report);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Image upload failed: " + error);
                            uploadCount[0]++;

                            if (uploadCount[0] == totalImages) {
                                // Save report even if some images failed
                                report.setImages(uploadedImages);

                                if (successCount[0] > 0) {
                                    Toast.makeText(activity_create_report.this,
                                            "Đã tải " + successCount[0] + "/" + totalImages + " ảnh",
                                            Toast.LENGTH_SHORT).show();
                                }

                                saveReport(report);
                            }
                        });
                    }

                    @Override
                    public void onProgress(int progress) {
                        // Optional: show progress bar
                    }
                });
            } else {
                // Already uploaded (URL format)
                uploadedImages.add(image);
                uploadCount[0]++;
                successCount[0]++;

                if (uploadCount[0] == totalImages) {
                    report.setImages(uploadedImages);
                    saveReport(report);
                }
            }
        }
    }

    private void saveReport(FinancialReport report) {
        reportRepository.createReport(report, new ReportRepository.OnReportListener() {
            @Override
            public void onSuccess(String reportId) {
                Toast.makeText(activity_create_report.this, "Tạo báo cáo thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(activity_create_report.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}