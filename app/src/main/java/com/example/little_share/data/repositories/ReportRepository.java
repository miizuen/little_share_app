package com.example.little_share.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.little_share.data.models.FinancialReport;
import com.example.little_share.data.models.ReportExpense;
import com.example.little_share.data.models.ReportImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReportRepository {
    private static final String TAG = "ReportRepository";
    private static final String COLLECTION = "financial_reports";
    private static final String EXPENSES_COLLECTION = "report_expenses";
    private static final String IMAGES_COLLECTION = "report_images";

    private final FirebaseFirestore db;
    private final String currentUserId;

    public ReportRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    // Tạo báo cáo mới
    public void createReport(FinancialReport report, OnReportListener listener) {
        if (currentUserId == null) {
            listener.onFailure("Chưa đăng nhập");
            return;
        }

        report.setOrganizationId(currentUserId);

        db.collection(COLLECTION)
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    String reportId = documentReference.getId();
                    report.setId(reportId);

                    // Lưu expenses nếu có
                    if (report.getExpenses() != null && !report.getExpenses().isEmpty()) {
                        saveExpenses(reportId, report.getExpenses(), new OnExpenseListener() {
                            @Override
                            public void onSuccess() {
                                // Lưu images nếu có
                                if (report.getImages() != null && !report.getImages().isEmpty()) {
                                    saveImages(reportId, report.getImages(), new OnImageListener() {
                                        @Override
                                        public void onSuccess() {
                                            listener.onSuccess(reportId);
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            listener.onSuccess(reportId); // Vẫn thành công dù ảnh lỗi
                                        }
                                    });
                                } else {
                                    listener.onSuccess(reportId);
                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                listener.onSuccess(reportId); // Vẫn thành công dù expense lỗi
                            }
                        });
                    } else {
                        listener.onSuccess(reportId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating report", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Lưu danh sách expenses
    private void saveExpenses(String reportId, List<ReportExpense> expenses, OnExpenseListener listener) {
        int[] count = {0};
        int total = expenses.size();

        for (ReportExpense expense : expenses) {
            expense.setReportId(reportId);

            db.collection(EXPENSES_COLLECTION)
                    .add(expense)
                    .addOnSuccessListener(documentReference -> {
                        expense.setId(documentReference.getId());
                        count[0]++;
                        if (count[0] == total) {
                            listener.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving expense", e);
                        listener.onFailure(e.getMessage());
                    });
        }
    }

    // Lưu danh sách images
    private void saveImages(String reportId, List<ReportImage> images, OnImageListener listener) {
        int[] count = {0};
        int total = images.size();

        for (ReportImage image : images) {
            image.setReportId(reportId);

            db.collection(IMAGES_COLLECTION)
                    .add(image)
                    .addOnSuccessListener(documentReference -> {
                        image.setId(documentReference.getId());
                        count[0]++;
                        if (count[0] == total) {
                            listener.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving image", e);
                        listener.onFailure(e.getMessage());
                    });
        }
    }

    // Lấy tất cả báo cáo của NGO hiện tại
    public LiveData<List<FinancialReport>> getReportsByCurrentNgo() {
        MutableLiveData<List<FinancialReport>> liveData = new MutableLiveData<>();

        if (currentUserId == null) {
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        db.collection(COLLECTION)
                .whereEqualTo("organizationId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting reports", error);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    if (snapshots != null) {
                        List<FinancialReport> reports = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            FinancialReport report = doc.toObject(FinancialReport.class);
                            report.setId(doc.getId());
                            reports.add(report);
                        }
                        liveData.setValue(reports);
                        Log.d(TAG, "Loaded " + reports.size() + " reports");
                    } else {
                        liveData.setValue(new ArrayList<>());
                    }
                });

        return liveData;
    }

    // Lấy báo cáo theo campaignId
    public LiveData<FinancialReport> getReportByCampaignId(String campaignId) {
        MutableLiveData<FinancialReport> liveData = new MutableLiveData<>();

        db.collection(COLLECTION)
                .whereEqualTo("campaignId", campaignId)
                .limit(1)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null || snapshots.isEmpty()) {
                        liveData.setValue(null);
                        return;
                    }

                    FinancialReport report = snapshots.getDocuments().get(0).toObject(FinancialReport.class);
                    if (report != null) {
                        report.setId(snapshots.getDocuments().get(0).getId());

                        // Load expenses và images
                        loadReportDetails(report.getId(), report, liveData);
                    } else {
                        liveData.setValue(null);
                    }
                });

        return liveData;
    }

    // Load chi tiết báo cáo (expenses + images)
    private void loadReportDetails(String reportId, FinancialReport report, MutableLiveData<FinancialReport> liveData) {
        // Load expenses
        db.collection(EXPENSES_COLLECTION)
                .whereEqualTo("reportId", reportId)
                .get()
                .addOnSuccessListener(expenseSnapshots -> {
                    List<ReportExpense> expenses = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : expenseSnapshots) {
                        ReportExpense expense = doc.toObject(ReportExpense.class);
                        expense.setId(doc.getId());
                        expenses.add(expense);
                    }
                    report.setExpenses(expenses);

                    // Load images
                    db.collection(IMAGES_COLLECTION)
                            .whereEqualTo("reportId", reportId)
                            .get()
                            .addOnSuccessListener(imageSnapshots -> {
                                List<ReportImage> images = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : imageSnapshots) {
                                    ReportImage image = doc.toObject(ReportImage.class);
                                    image.setId(doc.getId());
                                    images.add(image);
                                }
                                report.setImages(images);
                                liveData.setValue(report);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading images", e);
                                liveData.setValue(report);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading expenses", e);
                    liveData.setValue(report);
                });
    }

    // Cập nhật báo cáo
    public void updateReport(FinancialReport report, OnReportListener listener) {
        if (report.getId() == null) {
            listener.onFailure("Report ID không hợp lệ");
            return;
        }

        // Cập nhật thông tin báo cáo chính
        db.collection(COLLECTION)
                .document(report.getId())
                .set(report)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật expenses
                    updateExpenses(report.getId(), report.getExpenses(), new OnExpenseListener() {
                        @Override
                        public void onSuccess() {
                            // Cập nhật images
                            updateImages(report.getId(), report.getImages(), new OnImageListener() {
                                @Override
                                public void onSuccess() {
                                    listener.onSuccess(report.getId());
                                }

                                @Override
                                public void onFailure(String error) {
                                    listener.onSuccess(report.getId()); // Vẫn thành công dù ảnh lỗi
                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            listener.onSuccess(report.getId()); // Vẫn thành công dù expense lỗi
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating report", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Cập nhật expenses
    private void updateExpenses(String reportId, List<ReportExpense> expenses, OnExpenseListener listener) {
        // Xóa tất cả expenses cũ trước
        db.collection(EXPENSES_COLLECTION)
                .whereEqualTo("reportId", reportId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    // Xóa expenses cũ
                    for (QueryDocumentSnapshot doc : snapshots) {
                        doc.getReference().delete();
                    }

                    // Thêm expenses mới
                    if (expenses != null && !expenses.isEmpty()) {
                        saveExpenses(reportId, expenses, listener);
                    } else {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting old expenses", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Cập nhật images
    private void updateImages(String reportId, List<ReportImage> images, OnImageListener listener) {
        // Xóa tất cả images cũ trước
        db.collection(IMAGES_COLLECTION)
                .whereEqualTo("reportId", reportId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    // Xóa images cũ
                    for (QueryDocumentSnapshot doc : snapshots) {
                        doc.getReference().delete();
                    }

                    // Thêm images mới
                    if (images != null && !images.isEmpty()) {
                        saveImages(reportId, images, listener);
                    } else {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting old images", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Interfaces
    public interface OnReportListener {
        void onSuccess(String reportId);
        void onFailure(String error);
    }

    public interface OnExpenseListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnImageListener {
        void onSuccess();
        void onFailure(String error);
    }
}