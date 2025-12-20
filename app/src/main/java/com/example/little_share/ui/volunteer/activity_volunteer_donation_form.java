package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.example.little_share.data.models.Donation;
import com.example.little_share.data.models.DonationItem;
import com.example.little_share.data.models.User;
import com.example.little_share.data.repositories.DonationRepository;
import com.example.little_share.data.repositories.UserRepository;
import com.example.little_share.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class activity_volunteer_donation_form extends AppCompatActivity {

    private static final String TAG = "DonationForm";

    private ImageButton btnBack;
    private TextView tvDonationType, tvQuantity, tvPoints;
    private MaterialButton btnSachGiaoKhoa, btnSachKyNang, btnSachThieuNhi, btnTaiLieu;
    private MaterialButton btnMoi, btnTot, btnKha, btnOn;
    private MaterialButton btnRemove, btnPlus, btnConfirm;
    private TextInputEditText edtNote;

    // Repositories
    private DonationRepository donationRepository;
    private UserRepository userRepository;

    // Dữ liệu người dùng chọn
    private String selectedCategory = "Sách giáo khoa";
    private String selectedCondition = "Mới (100%)";
    private int quantity = 5;
    private String donationTypeKey = "BOOK";

    // THÊM BIẾN CAMPAIGN ID
    private String campaignId;

    // User data
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_donation_form);

        // NHẬN CAMPAIGN ID TỪ INTENT
        donationTypeKey = getIntent().getStringExtra("DONATION_TYPE");
        if (donationTypeKey == null) donationTypeKey = "BOOK";

        campaignId = getIntent().getStringExtra("CAMPAIGN_ID");

        // Log để debug
        Log.d(TAG, "Received DONATION_TYPE: " + donationTypeKey);
        Log.d(TAG, "Received CAMPAIGN_ID: " + campaignId);

        // CHO PHÉP CAMPAIGN ID NULL - không bắt buộc nữa
        if (campaignId == null || campaignId.isEmpty()) {
            Log.w(TAG, "Campaign ID is null - proceeding without campaign");
            campaignId = ""; // Gán giá trị rỗng thay vì finish()
        }

        initRepositories();
        initViews();
        setupBackButton();
        setupInitialUI();
        setupCategoryButtons();
        setupConditionButtons();
        setupQuantityButtons();
        setupConfirmButton();

        loadUserData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initRepositories() {
        donationRepository = new DonationRepository();
        userRepository = new UserRepository();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvDonationType = findViewById(R.id.tvDonationType);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvPoints = findViewById(R.id.tv_points);

        btnSachGiaoKhoa = findViewById(R.id.btn_sach_giao_khoa);
        btnSachKyNang = findViewById(R.id.btn_sach_ky_nang);
        btnSachThieuNhi = findViewById(R.id.btn_sach_thieu_nhi);
        btnTaiLieu = findViewById(R.id.btn_tai_lieu_hoc_tap);

        btnMoi = findViewById(R.id.btn_moi);
        btnTot = findViewById(R.id.btn_tot);
        btnKha = findViewById(R.id.btn_kha);
        btnOn = findViewById(R.id.btn_on);

        btnRemove = findViewById(R.id.btn_remove);
        btnPlus = findViewById(R.id.btn_plus);
        btnConfirm = findViewById(R.id.btn_confirm);
        edtNote = findViewById(R.id.edt_note);
    }

    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                finish();
            });
        } else {
            Log.e(TAG, "btnBack is null!");
        }
    }

    private void loadUserData() {
        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                Log.d(TAG, "User loaded: " + user.getFullName());
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading user: " + error);
                Toast.makeText(activity_volunteer_donation_form.this,
                        "Lỗi tải thông tin: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupInitialUI() {
        switch (donationTypeKey) {
            case "BOOKS":
            case "BOOK":
                tvDonationType.setText("SÁCH VỞ");
                btnSachGiaoKhoa.setText("Sách giáo khoa");
                btnSachKyNang.setText("Sách kỹ năng");
                btnSachThieuNhi.setText("Sách thiếu nhi");
                btnTaiLieu.setText("Tài liệu học tập");
                selectedCategory = "Sách giáo khoa";
                break;
            case "CLOTHES":
            case "SHIRT":
                tvDonationType.setText("QUẦN ÁO");
                btnSachGiaoKhoa.setText("Áo thun");
                btnSachKyNang.setText("Áo khoác");
                btnSachThieuNhi.setText("Quần");
                btnTaiLieu.setText("Đồng phục");
                selectedCategory = "Áo thun";
                break;
            case "TOYS":
            case "TOY":
                tvDonationType.setText("ĐỒ CHƠI");
                btnSachGiaoKhoa.setText("Xếp hình");
                btnSachKyNang.setText("Búp bê");
                btnSachThieuNhi.setText("Xe đồ chơi");
                btnTaiLieu.setText("Lego");
                selectedCategory = "Xếp hình";
                break;
            case "ESSENTIALS":
                tvDonationType.setText("NHU YẾU PHẨM");
                btnSachGiaoKhoa.setText("Thực phẩm khô");
                btnSachKyNang.setText("Đồ dùng nhà ở");
                btnSachThieuNhi.setText("Vật dụng vệ sinh");
                btnTaiLieu.setText("Thuốc");
                selectedCategory = "Thực phẩm khô";
                break;
        }

        selectCategoryButton(btnSachGiaoKhoa);
        selectConditionButton(btnMoi);
        updatePoints();
    }

    private void setupCategoryButtons() {
        btnSachGiaoKhoa.setOnClickListener(v -> selectCategoryButton(btnSachGiaoKhoa));
        btnSachKyNang.setOnClickListener(v -> selectCategoryButton(btnSachKyNang));
        btnSachThieuNhi.setOnClickListener(v -> selectCategoryButton(btnSachThieuNhi));
        btnTaiLieu.setOnClickListener(v -> selectCategoryButton(btnTaiLieu));
    }

    private void selectCategoryButton(MaterialButton selectedBtn) {
        resetButtonBackground(btnSachGiaoKhoa);
        resetButtonBackground(btnSachKyNang);
        resetButtonBackground(btnSachThieuNhi);
        resetButtonBackground(btnTaiLieu);

        selectedBtn.setBackgroundResource(R.drawable.background_volunteer);
        selectedCategory = selectedBtn.getText().toString();
        updatePoints();
    }

    private void setupConditionButtons() {
        btnMoi.setOnClickListener(v -> selectConditionButton(btnMoi));
        btnTot.setOnClickListener(v -> selectConditionButton(btnTot));
        btnKha.setOnClickListener(v -> selectConditionButton(btnKha));
        btnOn.setOnClickListener(v -> selectConditionButton(btnOn));
    }

    private void selectConditionButton(MaterialButton selectedBtn) {
        resetButtonBackground(btnMoi);
        resetButtonBackground(btnTot);
        resetButtonBackground(btnKha);
        resetButtonBackground(btnOn);

        selectedBtn.setBackgroundResource(R.drawable.background_volunteer);
        selectedCondition = selectedBtn.getText().toString();
        updatePoints();
    }

    private void resetButtonBackground(MaterialButton btn) {
        btn.setBackgroundResource(R.drawable.bg_default);
    }

    private void setupQuantityButtons() {
        btnPlus.setOnClickListener(v -> {
            quantity++;
            updateQuantityAndPoints();
        });

        btnRemove.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityAndPoints();
            }
        });
    }

    private void updateQuantityAndPoints() {
        tvQuantity.setText(String.valueOf(quantity));
        updatePoints();
    }

    private void updatePoints() {
        int totalPoints = calculatePointsUsingConstants();
        tvPoints.setText(String.valueOf(totalPoints));
    }

    private int calculatePointsUsingConstants() {
        DonationItem.ItemCondition condition = convertConditionToEnum(selectedCondition);

        int basePoints = 0;
        switch (condition) {
            case NEW:
                basePoints = Constants.POINTS_PER_ITEM_NEW;
                break;
            case GOOD:
                basePoints = Constants.POINTS_PER_ITEM_GOOD;
                break;
            case FAIR:
                basePoints = Constants.POINTS_PER_ITEM_FAIR;
                break;
            case ACCEPTABLE:
                basePoints = Constants.POINTS_PER_ITEM_ACCEPTABLE;
                break;
        }

        if ("MONEY".equals(donationTypeKey)) {
            int amount = getMoneyAmount(selectedCategory);
            return (amount / 10000) * Constants.POINTS_PER_10K_VND;
        }

        return basePoints * quantity;
    }

    private DonationItem.ItemCondition convertConditionToEnum(String conditionText) {
        if (conditionText.contains("Mới") || conditionText.contains("100%")) {
            return DonationItem.ItemCondition.NEW;
        } else if (conditionText.contains("Tốt") || conditionText.contains("80")) {
            return DonationItem.ItemCondition.GOOD;
        } else if (conditionText.contains("Khá") || conditionText.contains("60")) {
            return DonationItem.ItemCondition.FAIR;
        } else {
            return DonationItem.ItemCondition.ACCEPTABLE;
        }
    }

    private int getMoneyAmount(String category) {
        switch (category) {
            case "50.000đ": return 50000;
            case "100.000đ": return 100000;
            case "200.000đ": return 200000;
            case "500.000đ": return 500000;
            default: return 50000;
        }
    }

    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this, "Vui lòng đợi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCategory.isEmpty() || selectedCondition.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            showConfirmationDialog();
        });
    }

    private void showConfirmationDialog() {
        String note = edtNote.getText() != null ? edtNote.getText().toString().trim() : "";
        int totalPoints = calculatePointsUsingConstants();

        String message = "Xác nhận quyên góp:\n\n" +
                "Loại: " + tvDonationType.getText() + "\n" +
                "Danh mục: " + selectedCategory + "\n" +
                "Số lượng: " + quantity + "\n" +
                "Tình trạng: " + selectedCondition + "\n" +
                "Điểm dự kiến: " + totalPoints + "\n\n" +
                "Bạn có chắc chắn muốn quyên góp?";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận quyên góp")
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Convert old format to new format
                    String newDonationType = convertToNewFormat(donationTypeKey);

                    Intent intent = new Intent(this, activity_volunteer_donation_confirm.class);
                    intent.putExtra("DONATION_TYPE", newDonationType);
                    intent.putExtra("CATEGORY", selectedCategory);
                    intent.putExtra("QUANTITY", quantity);
                    intent.putExtra("CONDITION", selectedCondition);
                    intent.putExtra("POINTS", totalPoints);
                    intent.putExtra("NOTE", note);
                    intent.putExtra("CAMPAIGN_ID", campaignId);

                    Log.d(TAG, "Passing CAMPAIGN_ID to confirm activity: " + campaignId);

                    startActivity(intent);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String convertToNewFormat(String oldType) {
        switch (oldType) {
            case "BOOK": return "BOOKS";
            case "SHIRT": return "CLOTHES";
            case "TOY": return "TOYS";
            case "MONEY": return "MONEY";
            default: return oldType;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "Hardware back button pressed");
    }
}