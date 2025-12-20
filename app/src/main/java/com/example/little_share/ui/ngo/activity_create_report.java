package com.example.little_share.ui.ngo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.FinancialReport;
import com.example.little_share.data.models.ReportExpense;
import com.example.little_share.data.models.ReportImage;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.ui.ngo.adapter.ExpenseAdapter;
import com.example.little_share.ui.ngo.adapter.ReportImageAdapter;
import com.example.little_share.ui.ngo.dialog.AddSpendingDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class activity_create_report extends AppCompatActivity implements AddSpendingDialog.OnExpenseAddedListener {

    // UI Components
    private ImageView btnBack;
    private AutoCompleteTextView spinnerCampaign;
    private LinearLayout layoutCampaignInfo;
    private TextView tvCampaignInfo;
    private TextInputEditText edtReportContent;
    private Button btnAddExpense;
    private RecyclerView recyclerExpenses, recyclerReportImages;
    private MaterialButton btnAddImages, btnCreate;
    private Button btnCancel;

    // Data
    private List<Campaign> campaignList = new ArrayList<>();
    private List<ReportExpense> expenseList = new ArrayList<>();
    private List<ReportImage> imageList = new ArrayList<>();
    private Campaign selectedCampaign;

    // Adapters
    private ExpenseAdapter expenseAdapter;
    private ReportImageAdapter imageAdapter;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private CampaignRepository campaignRepository;

    // Image picker
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private List<Uri> selectedImageUris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_report);

        initFirebase();
        initViews();
        setupRecyclerViews();
        setupImagePicker();
        loadCampaigns();
        setupListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        campaignRepository = new CampaignRepository();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerCampaign = findViewById(R.id.spinnerCampaign);
        layoutCampaignInfo = findViewById(R.id.layoutCampaignInfo);
        tvCampaignInfo = findViewById(R.id.tvCampaignInfo);
        edtReportContent = findViewById(R.id.edtReportContent);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        recyclerReportImages = findViewById(R.id.recyclerReportImages);
        btnAddImages = findViewById(R.id.btnAddImages);
        btnCreate = findViewById(R.id.btnCreate);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupRecyclerViews() {
        // Setup expenses RecyclerView (sẽ được thêm vào layout sau khi có chi tiêu)
        expenseAdapter = new ExpenseAdapter(expenseList, new ExpenseAdapter.OnExpenseActionListener() {
            @Override
            public void onEdit(ReportExpense expense, int position) {
                editExpense(expense, position);
            }

            @Override
            public void onDelete(ReportExpense expense, int position) {
                deleteExpense(position);
            }
        });

        // Setup images RecyclerView
        imageAdapter = new ReportImageAdapter(imageList, new ReportImageAdapter.OnImageActionListener() {
            @Override
            public void onRemove(int position) {
                removeImage(position);
            }
        });

        recyclerReportImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerReportImages.setAdapter(imageAdapter);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        if (data.getClipData() != null) {
                            // Multiple images selected
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count && selectedImageUris.size() < 10; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                selectedImageUris.add(imageUri);
                                addImageToList(imageUri);
                            }
                        } else if (data.getData() != null) {
                            // Single image selected
                            Uri imageUri = data.getData();
                            if (selectedImageUris.size() < 10) {
                                selectedImageUris.add(imageUri);
                                addImageToList(imageUri);
                            }
                        }
                    }
                }
        );
    }

    private void addImageToList(Uri imageUri) {
        ReportImage reportImage = new ReportImage();
        reportImage.setImageUrl(imageUri.toString()); // Temporary, will be replaced with Firebase URL
        imageList.add(reportImage);
        imageAdapter.notifyItemInserted(imageList.size() - 1);
    }

    private void removeImage(int position) {
        if (position < selectedImageUris.size()) {
            selectedImageUris.remove(position);
        }
        imageList.remove(position);
        imageAdapter.notifyItemRemoved(position);
    }

    private void loadCampaigns() {
        // Sử dụng method getCampaignsByCurrentNgo() thay vì getCampaignsByOrganization()
        campaignRepository.getCampaignsByCurrentNgo().observe(this, new Observer<List<Campaign>>() {
            @Override
            public void onChanged(List<Campaign> campaigns) {
                if (campaigns != null) {
                    campaignList.clear();
                    campaignList.addAll(campaigns);
                    setupCampaignSpinner();
                } else {
                    Toast.makeText(activity_create_report.this, "Lỗi tải danh sách chiến dịch", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupCampaignSpinner() {
        List<String> campaignNames = new ArrayList<>();
        for (Campaign campaign : campaignList) {
            campaignNames.add(campaign.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, campaignNames);
        spinnerCampaign.setAdapter(adapter);

        spinnerCampaign.setOnItemClickListener((parent, view, position, id) -> {
            selectedCampaign = campaignList.get(position);
            displayCampaignInfo(selectedCampaign);
        });
    }

    private void displayCampaignInfo(Campaign campaign) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        String info = String.format(
                "Tên: %s\nNgân sách: %s\nThời gian: %s - %s\nĐịa điểm: %s",
                campaign.getName(),
                currencyFormat.format(campaign.getTargetBudget()),
                campaign.getStartDate() != null ? dateFormat.format(campaign.getStartDate()) : "Chưa xác định",
                campaign.getEndDate() != null ? dateFormat.format(campaign.getEndDate()) : "Chưa xác định",
                campaign.getLocation() != null ? campaign.getLocation() : "Chưa xác định"
        );

        tvCampaignInfo.setText(info);
        layoutCampaignInfo.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddExpense.setOnClickListener(v -> {
            if (selectedCampaign == null) {
                Toast.makeText(this, "Vui lòng chọn chiến dịch trước", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddExpenseDialog();
        });

        btnAddImages.setOnClickListener(v -> {
            if (selectedImageUris.size() >= 10) {
                Toast.makeText(this, "Chỉ được chọn tối đa 10 ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker();
        });

        btnCancel.setOnClickListener(v -> finish());

        btnCreate.setOnClickListener(v -> createReport());
    }

    private void showAddExpenseDialog() {
        AddSpendingDialog dialog = new AddSpendingDialog();
        dialog.setOnExpenseAddedListener(this);
        dialog.show(getSupportFragmentManager(), "AddSpendingDialog");
    }

    @Override
    public void onExpenseAdded(ReportExpense expense) {
        expenseList.add(expense);

        // Nếu đây là chi tiêu đầu tiên, thêm RecyclerView vào layout
        if (expenseList.size() == 1) {
            addExpenseRecyclerView();
        }

        expenseAdapter.notifyItemInserted(expenseList.size() - 1);
        updateAddExpenseButtonText();
    }

    private void addExpenseRecyclerView() {
        // Tìm vị trí để thêm RecyclerView (sau button Add Expense)
        LinearLayout parentLayout = findViewById(R.id.main);

        // Tạo RecyclerView cho expenses
        RecyclerView recyclerExpenses = new RecyclerView(this);
        recyclerExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerExpenses.setAdapter(expenseAdapter);

        // Thêm vào layout (cần implement logic để thêm đúng vị trí)
        // Hoặc có thể thêm RecyclerView vào XML và ẩn/hiện
    }

    private void editExpense(ReportExpense expense, int position) {
        AddSpendingDialog dialog = AddSpendingDialog.newInstanceForEdit(expense);
        dialog.setOnExpenseAddedListener(new AddSpendingDialog.OnExpenseAddedListener() {
            @Override
            public void onExpenseAdded(ReportExpense updatedExpense) {
                expenseList.set(position, updatedExpense);
                expenseAdapter.notifyItemChanged(position);
            }
        });
        dialog.show(getSupportFragmentManager(), "EditSpendingDialog");
    }

    private void deleteExpense(int position) {
        expenseList.remove(position);
        expenseAdapter.notifyItemRemoved(position);
        updateAddExpenseButtonText();
    }

    private void updateAddExpenseButtonText() {
        if (expenseList.isEmpty()) {
            btnAddExpense.setText("+ Thêm khoản chi tiêu");
        } else {
            btnAddExpense.setText(String.format("+ Thêm chi tiêu (%d)", expenseList.size()));
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    private void createReport() {
        if (!validateInput()) return;

        btnCreate.setEnabled(false);
        btnCreate.setText("Đang tạo...");

        // Upload images first, then create report
        if (!selectedImageUris.isEmpty()) {
            uploadImagesAndCreateReport();
        } else {
            createReportInFirestore(new ArrayList<>());
        }
    }

    private boolean validateInput() {
        if (selectedCampaign == null) {
            Toast.makeText(this, "Vui lòng chọn chiến dịch", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(edtReportContent.getText())) {
            Toast.makeText(this, "Vui lòng nhập nội dung báo cáo", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void uploadImagesAndCreateReport() {
        List<String> uploadedImageUrls = new ArrayList<>();

        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri imageUri = selectedImageUris.get(i);
            String fileName = "report_images/" + UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storage.getReference().child(fileName);

            final int index = i;
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            uploadedImageUrls.add(downloadUri.toString());

                            // Nếu đã upload hết ảnh
                            if (uploadedImageUrls.size() == selectedImageUris.size()) {
                                createReportInFirestore(uploadedImageUrls);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnCreate.setEnabled(true);
                        btnCreate.setText("Tạo báo cáo");
                    });
        }
    }

    private void createReportInFirestore(List<String> imageUrls) {
        FinancialReport report = new FinancialReport();
        report.setCampaignId(selectedCampaign.getId());
        report.setCampaignName(selectedCampaign.getName());
        report.setOrganizationId(auth.getCurrentUser().getUid());
        report.setDescription(edtReportContent.getText().toString().trim());

        // Calculate total expense
        double totalExpense = 0;
        for (ReportExpense expense : expenseList) {
            totalExpense += expense.getAmount();
        }
        report.setTotalExpense(totalExpense);

        // Create report images
        List<ReportImage> reportImages = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            ReportImage reportImage = new ReportImage();
            reportImage.setImageUrl(imageUrl);
            reportImages.add(reportImage);
        }
        report.setImages(reportImages);
        report.setExpenses(expenseList);

        db.collection("financial_reports")
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Tạo báo cáo thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tạo báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnCreate.setEnabled(true);
                    btnCreate.setText("Tạo báo cáo");
                });
    }
}
