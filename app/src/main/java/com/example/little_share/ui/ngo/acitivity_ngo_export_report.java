package com.example.little_share.ui.ngo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.little_share.data.models.FinancialReport;
import com.example.little_share.data.models.ReportExpense;
import com.example.little_share.data.models.ReportImage;
import com.example.little_share.data.repositories.ReportRepository;
import com.example.little_share.helper.ImgBBUploader;
import com.example.little_share.ui.ngo.adapter.ExpenseAdapter;
import com.example.little_share.ui.ngo.adapter.ReportImageAdapter;
import com.example.little_share.ui.ngo.dialog.AddSpendingDialog;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class acitivity_ngo_export_report extends AppCompatActivity {
    private static final String TAG = "ExportReport";
    private static final int MAX_IMAGES = 10;

    private ImageView btnBack;
    private TextView tvCampaignName, tvDate, tvLocation, tvTotalSpending, tvVolunteerCount, tvExpenseTotal;
    private TextView btnAddExpense, btnAddImage;
    private LinearLayout btnCamera;
    private RecyclerView rvExpenses, rvImages;
    private MaterialButton btnEdit, btnExportPdf;

    private ReportRepository reportRepository;
    private String reportId, campaignId, campaignName;
    private FinancialReport currentReport;

    private ExpenseAdapter expenseAdapter;
    private ReportImageAdapter imageAdapter;
    private List<ReportExpense> expenses;
    private List<ReportImage> images;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_ngo_export_report);

        initViews();
        getIntentData();
        setupRepositories();
        setupImagePicker();
        setupRecyclerViews();
        setupListeners();
        loadReportData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvDate = findViewById(R.id.tvDate);
        tvLocation = findViewById(R.id.tvLocation);
        tvTotalSpending = findViewById(R.id.tvTotalSpending);
        tvVolunteerCount = findViewById(R.id.tvVolunteerCount);
        tvExpenseTotal = findViewById(R.id.tvExpenseTotal);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnCamera = findViewById(R.id.btnCamera);
        rvExpenses = findViewById(R.id.rvExpenses);
        rvImages = findViewById(R.id.rvImages);
        btnEdit = findViewById(R.id.btnEdit);
        btnExportPdf = findViewById(R.id.btnExportPdf);

        expenses = new ArrayList<>();
        images = new ArrayList<>();
    }

    private void getIntentData() {
        reportId = getIntent().getStringExtra("REPORT_ID");
        campaignId = getIntent().getStringExtra("CAMPAIGN_ID");
        campaignName = getIntent().getStringExtra("CAMPAIGN_NAME");

        Log.d(TAG, "reportId: " + reportId);
        Log.d(TAG, "campaignId: " + campaignId);
    }

    private void setupRepositories() {
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

    private void setupRecyclerViews() {
        // Expenses RecyclerView
        expenseAdapter = new ExpenseAdapter(expenses, new ExpenseAdapter.OnExpenseActionListener() {
            @Override
            public void onEdit(ReportExpense expense, int position) {
                showEditExpenseDialog(expense, position);
            }

            @Override
            public void onDelete(ReportExpense expense, int position) {
                showDeleteExpenseDialog(expense, position);
            }
        });
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(expenseAdapter);

        // Images RecyclerView
        imageAdapter = new ReportImageAdapter(images, position -> {
            showDeleteImageDialog(position);
        });
        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        btnAddImage.setOnClickListener(v -> {
            if (images.size() >= MAX_IMAGES) {
                Toast.makeText(this, "Chỉ được thêm tối đa " + MAX_IMAGES + " ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker();
        });

        btnCamera.setOnClickListener(v -> {
            if (images.size() >= MAX_IMAGES) {
                Toast.makeText(this, "Chỉ được thêm tối đa " + MAX_IMAGES + " ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker();
        });

        btnEdit.setOnClickListener(v -> {
            // TODO: Implement edit functionality
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        btnExportPdf.setOnClickListener(v -> {
            // TODO: Implement PDF export
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadReportData() {
        if (campaignId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin báo cáo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        reportRepository.getReportByCampaignId(campaignId).observe(this, report -> {
            if (report != null) {
                currentReport = report;
                displayReportData(report);
            } else {
                Toast.makeText(this, "Không tìm thấy báo cáo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayReportData(FinancialReport report) {
        // Campaign name
        tvCampaignName.setText(report.getCampaignName());

        // Date (you might want to get this from Campaign object)
        tvDate.setText("N/A");

        // Location (you might want to get this from Campaign object)
        tvLocation.setText("N/A");

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

    private void showAddExpenseDialog() {
        AddSpendingDialog dialog = new AddSpendingDialog();
        dialog.setOnExpenseAddedListener(expense -> {
            // TODO: Save to Firebase
            expenses.add(expense);
            expenseAdapter.notifyItemInserted(expenses.size() - 1);
            updateTotalExpense();
            Toast.makeText(this, "Đã thêm khoản chi", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "AddSpendingDialog");
    }

    private void showEditExpenseDialog(ReportExpense expense, int position) {
        AddSpendingDialog dialog = AddSpendingDialog.newInstanceForEdit(expense);
        dialog.setOnExpenseAddedListener(updatedExpense -> {
            // TODO: Update in Firebase
            expenses.set(position, updatedExpense);
            expenseAdapter.notifyItemChanged(position);
            updateTotalExpense();
            Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "EditSpendingDialog");
    }

    private void showDeleteExpenseDialog(ReportExpense expense, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa khoản chi này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // TODO: Delete from Firebase
                    expenses.remove(position);
                    expenseAdapter.notifyItemRemoved(position);
                    updateTotalExpense();
                    Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteImageDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa ảnh này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // TODO: Delete from Firebase
                    images.remove(position);
                    imageAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
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
            int count = Math.min(data.getClipData().getItemCount(), MAX_IMAGES - images.size());
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                uploadAndAddImage(imageUri);
            }
        } else if (data.getData() != null) {
            // Single image
            Uri imageUri = data.getData();
            uploadAndAddImage(imageUri);
        }
    }

    private void uploadAndAddImage(Uri imageUri) {
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

        ImgBBUploader.uploadImage(this, imageUri, new ImgBBUploader.UploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    ReportImage reportImage = new ReportImage(reportId, imageUrl);
                    // TODO: Save to Firebase
                    images.add(reportImage);
                    imageAdapter.notifyItemInserted(images.size() - 1);
                    Toast.makeText(acitivity_ngo_export_report.this, "Đã tải ảnh lên", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(acitivity_ngo_export_report.this, "Lỗi tải ảnh: " + error, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onProgress(int progress) {
                // Optional: show progress
            }
        });
    }

    private void updateTotalExpense() {
        double total = 0;
        for (ReportExpense expense : expenses) {
            total += expense.getAmount();
        }
        tvExpenseTotal.setText(formatMoney(total) + " đ");
        tvTotalSpending.setText(formatMoney(total) + " VND");
    }

    private String formatMoney(double amount) {
        if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000);
        }
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }
}