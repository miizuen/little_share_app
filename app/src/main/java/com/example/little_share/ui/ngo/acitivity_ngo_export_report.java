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
        
        LinearLayoutManager imageLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvImages.setLayoutManager(imageLayoutManager);
        rvImages.setAdapter(imageAdapter);
        
        // Đảm bảo RecyclerView có thể scroll và hiển thị đúng
        rvImages.setHasFixedSize(false);
        rvImages.setNestedScrollingEnabled(true);
        rvImages.setItemAnimator(null); // Tắt animation để tránh conflict
        
        Log.d(TAG, "Image adapter setup completed");
        Log.d(TAG, "Initial images size: " + images.size());
        Log.d(TAG, "Adapter item count: " + imageAdapter.getItemCount());
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
            // Nút "Quay lại" - đóng activity và quay về trang trước
            finish();
        });

        btnExportPdf.setOnClickListener(v -> {
            // Nút "Lưu" - lưu tất cả thay đổi vào Firebase
            saveReportChanges();
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
            
            // Force complete refresh of image adapter
            imageAdapter.notifyDataSetChanged();
            
            // Also try to refresh the RecyclerView layout
            rvImages.post(() -> {
                rvImages.requestLayout();
                imageAdapter.notifyDataSetChanged();
            });
            
            Log.d(TAG, "Loaded " + images.size() + " images from report");
            debugAdapterState();
        } else {
            Log.d(TAG, "No images found in report");
        }

        Log.d(TAG, "Displayed report: " + report.getCampaignName());
        Log.d(TAG, "Expenses: " + expenses.size());
        Log.d(TAG, "Images: " + images.size());
        Log.d(TAG, "Adapter item count: " + imageAdapter.getItemCount());
    }

    private void showAddExpenseDialog() {
        AddSpendingDialog dialog = new AddSpendingDialog();
        dialog.setOnExpenseAddedListener(expense -> {
            // Thêm vào danh sách local
            expenses.add(expense);
            expenseAdapter.notifyItemInserted(expenses.size() - 1);
            updateTotalExpense();
            
            // Lưu ngay vào Firebase
            saveExpenseToFirebase(expense);
            
            Toast.makeText(this, "Đã thêm khoản chi", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "AddSpendingDialog");
    }

    private void showEditExpenseDialog(ReportExpense expense, int position) {
        AddSpendingDialog dialog = AddSpendingDialog.newInstanceForEdit(expense);
        dialog.setOnExpenseAddedListener(updatedExpense -> {
            // Cập nhật trong danh sách local
            expenses.set(position, updatedExpense);
            expenseAdapter.notifyItemChanged(position);
            updateTotalExpense();
            
            // Cập nhật trong Firebase
            updateExpenseInFirebase(updatedExpense);
            
            Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "EditSpendingDialog");
    }

    private void showDeleteExpenseDialog(ReportExpense expense, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa khoản chi này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Xóa khỏi danh sách local
                    expenses.remove(position);
                    expenseAdapter.notifyItemRemoved(position);
                    updateTotalExpense();
                    
                    // Xóa khỏi Firebase
                    deleteExpenseFromFirebase(expense);
                    
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
                    ReportImage imageToDelete = images.get(position);
                    
                    // Xóa khỏi danh sách local
                    images.remove(position);
                    imageAdapter.notifyItemRemoved(position);
                    
                    // Xóa khỏi Firebase
                    deleteImageFromFirebase(imageToDelete);
                    
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
                    
                    // Thêm vào danh sách local
                    images.add(reportImage);
                    
                    // Debug log trước khi notify
                    Log.d(TAG, "About to notify adapter. Images size: " + images.size());
                    Log.d(TAG, "New image URL: " + imageUrl);
                    
                    // Notify adapter về item mới - thử cả hai cách
                    int newPosition = images.size() - 1;
                    imageAdapter.notifyItemInserted(newPosition);
                    
                    // Thêm delay nhỏ rồi force refresh toàn bộ adapter
                    rvImages.postDelayed(() -> {
                        imageAdapter.notifyDataSetChanged();
                        rvImages.scrollToPosition(newPosition);
                        debugAdapterState();
                    }, 100);
                    
                    // Lưu vào Firebase (không cần chờ)
                    saveImageToFirebase(reportImage);
                    
                    Toast.makeText(acitivity_ngo_export_report.this, "Đã thêm ảnh thành công!", Toast.LENGTH_SHORT).show();
                    
                    Log.d(TAG, "Image added successfully. Total images: " + images.size());
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(acitivity_ngo_export_report.this, "Lỗi tải ảnh: " + error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to upload image: " + error);
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

    private void saveReportChanges() {
        if (currentReport == null) {
            Toast.makeText(this, "Không có dữ liệu để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Đang lưu...")
                .setMessage("Vui lòng chờ trong giây lát")
                .setCancelable(false)
                .create();
        progressDialog.show();

        // Cập nhật dữ liệu report
        currentReport.setExpenses(expenses);
        currentReport.setImages(images);
        
        // Tính lại tổng chi tiêu
        double totalExpense = 0;
        for (ReportExpense expense : expenses) {
            totalExpense += expense.getAmount();
        }
        currentReport.setTotalExpense(totalExpense);

        // Lưu vào Firebase
        reportRepository.updateReport(currentReport, new ReportRepository.OnReportListener() {
            @Override
            public void onSuccess(String reportId) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(acitivity_ngo_export_report.this, "Đã lưu thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Có thể quay lại trang trước sau khi lưu
                    // finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(acitivity_ngo_export_report.this, "Lỗi lưu dữ liệu: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Helper methods để thao tác với Firebase
    private void saveExpenseToFirebase(ReportExpense expense) {
        if (currentReport != null) {
            // Cập nhật danh sách expenses trong report
            currentReport.setExpenses(expenses);
            
            // Tính lại tổng chi tiêu
            double totalExpense = 0;
            for (ReportExpense exp : expenses) {
                totalExpense += exp.getAmount();
            }
            currentReport.setTotalExpense(totalExpense);
            
            // Lưu vào Firebase
            reportRepository.updateReport(currentReport, new ReportRepository.OnReportListener() {
                @Override
                public void onSuccess(String reportId) {
                    Log.d(TAG, "Expense saved to Firebase");
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Failed to save expense: " + error);
                }
            });
        }
    }

    private void updateExpenseInFirebase(ReportExpense expense) {
        saveExpenseToFirebase(expense); // Sử dụng cùng logic
    }

    private void deleteExpenseFromFirebase(ReportExpense expense) {
        if (currentReport != null) {
            // Cập nhật danh sách expenses trong report
            currentReport.setExpenses(expenses);
            
            // Tính lại tổng chi tiêu
            double totalExpense = 0;
            for (ReportExpense exp : expenses) {
                totalExpense += exp.getAmount();
            }
            currentReport.setTotalExpense(totalExpense);
            
            // Lưu vào Firebase
            reportRepository.updateReport(currentReport, new ReportRepository.OnReportListener() {
                @Override
                public void onSuccess(String reportId) {
                    Log.d(TAG, "Expense deleted from Firebase");
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Failed to delete expense: " + error);
                }
            });
        }
    }

    private void deleteImageFromFirebase(ReportImage image) {
        if (currentReport != null) {
            // Cập nhật danh sách images trong report
            currentReport.setImages(images);
            
            // Lưu vào Firebase
            reportRepository.updateReport(currentReport, new ReportRepository.OnReportListener() {
                @Override
                public void onSuccess(String reportId) {
                    Log.d(TAG, "Image deleted from Firebase");
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Failed to delete image: " + error);
                }
            });
        }
    }

    private void saveImageToFirebase(ReportImage image) {
        if (currentReport != null) {
            // Cập nhật danh sách images trong report
            currentReport.setImages(images);
            
            // Lưu vào Firebase
            reportRepository.updateReport(currentReport, new ReportRepository.OnReportListener() {
                @Override
                public void onSuccess(String reportId) {
                    Log.d(TAG, "Image saved to Firebase");
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Failed to save image: " + error);
                }
            });
        }
    }

    // Method helper để refresh image adapter
    private void refreshImageAdapter() {
        if (imageAdapter != null) {
            runOnUiThread(() -> {
                Log.d(TAG, "Refreshing image adapter...");
                imageAdapter.notifyDataSetChanged();
                
                // Force layout refresh
                rvImages.post(() -> {
                    rvImages.requestLayout();
                    rvImages.invalidate();
                });
                
                Log.d(TAG, "Image adapter refreshed. Total images: " + images.size());
                debugAdapterState();
            });
        }
    }

    // Method để kiểm tra trạng thái adapter
    private void debugAdapterState() {
        Log.d(TAG, "=== ADAPTER DEBUG ===");
        Log.d(TAG, "Images list size: " + images.size());
        Log.d(TAG, "Adapter item count: " + (imageAdapter != null ? imageAdapter.getItemCount() : "null"));
        Log.d(TAG, "RecyclerView adapter: " + (rvImages.getAdapter() != null ? "attached" : "null"));
        
        if (images.size() > 0) {
            Log.d(TAG, "First image URL: " + images.get(0).getImageUrl());
        }
    }
}