package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class activity_volunteer_donation_form extends AppCompatActivity {

    // Views
    private TextView tvDonationType, tvQuantity, tvPoints;
    private MaterialButton btnSachGiaoKhoa, btnSachKyNang, btnSachThieuNhi, btnTaiLieu;
    private MaterialButton btnMoi, btnTot, btnKha, btnOn;
    private MaterialButton btnRemove, btnPlus, btnConfirm;
    private TextInputEditText edtNote;

    // Dữ liệu người dùng chọn
    private String selectedCategory = "Sách giáo khoa";
    private String selectedCondition = "Mới (100%)";
    private int quantity = 5;
    private String donationTypeKey = "BOOK"; // mặc định

    // Màu sắc
    private final int COLOR_SELECTED = 0xFFFF8866;   // màu cam nổi bật (có thể lấy từ @drawable/background_volunteer)
    private final int COLOR_DEFAULT = 0xFFFFFFFF;     // trắng hoặc màu mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_donation_form);

        // Nhận loại quyên góp từ Intent
        donationTypeKey = getIntent().getStringExtra("DONATION_TYPE");
        if (donationTypeKey == null) donationTypeKey = "BOOK";

        initViews();
        setupInitialUI();
        setupCategoryButtons();
        setupConditionButtons();
        setupQuantityButtons();
        setupConfirmButton();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        tvDonationType = findViewById(R.id.tvDonationType);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvPoints = findViewById(R.id.tv_points);

        // Danh mục
        btnSachGiaoKhoa = findViewById(R.id.btn_sach_giao_khoa);
        btnSachKyNang = findViewById(R.id.btn_sach_ky_nang);
        btnSachThieuNhi = findViewById(R.id.btn_sach_thieu_nhi);
        btnTaiLieu = findViewById(R.id.btn_tai_lieu_hoc_tap);

        // Tình trạng
        btnMoi = findViewById(R.id.btn_moi);
        btnTot = findViewById(R.id.btn_tot);
        btnKha = findViewById(R.id.btn_kha);
        btnOn = findViewById(R.id.btn_on);

        // Số lượng
        btnRemove = findViewById(R.id.btn_remove);
        btnPlus = findViewById(R.id.btn_plus);

        btnConfirm = findViewById(R.id.btn_confirm);
        edtNote = findViewById(R.id.edt_note);
    }

    private void setupInitialUI() {
        // Cập nhật giao diện theo loại quyên góp
        switch (donationTypeKey) {
            case "BOOK":
                tvDonationType.setText("SÁCH VỞ");
                btnSachGiaoKhoa.setText("Sách giáo khoa");
                btnSachKyNang.setText("Sách kỹ năng");
                btnSachThieuNhi.setText("Sách thiếu nhi");
                btnTaiLieu.setText("Tài liệu học tập");
                break;
            case "SHIRT":
                tvDonationType.setText("QUẦN ÁO");
                btnSachGiaoKhoa.setText("Áo thun");
                btnSachKyNang.setText("Áo khoác");
                btnSachThieuNhi.setText("Quần");
                btnTaiLieu.setText("Đồng phục");
                break;
            case "TOY":
                tvDonationType.setText("ĐỒ CHƠI");
                btnSachGiaoKhoa.setText("Xếp hình");
                btnSachKyNang.setText("Búp bê");
                btnSachThieuNhi.setText("Xe đồ chơi");
                btnTaiLieu.setText("Lego");
                break;
            case "MONEY":
                tvDonationType.setText("TIỀN MẶT");
                btnSachGiaoKhoa.setText("50.000đ");
                btnSachKyNang.setText("100.000đ");
                btnSachThieuNhi.setText("200.000đ");
                btnTaiLieu.setText("500.000đ");
                break;
        }

        // Mặc định chọn nút đầu tiên
        selectCategoryButton(btnSachGiaoKhoa);
        selectConditionButton(btnMoi);
        updatePoints();
    }

    // === XỬ LÝ DANH MỤC ===
    private void setupCategoryButtons() {
        btnSachGiaoKhoa.setOnClickListener(v -> selectCategoryButton(btnSachGiaoKhoa));
        btnSachKyNang.setOnClickListener(v -> selectCategoryButton(btnSachKyNang));
        btnSachThieuNhi.setOnClickListener(v -> selectCategoryButton(btnSachThieuNhi));
        btnTaiLieu.setOnClickListener(v -> selectCategoryButton(btnTaiLieu));
    }

    private void selectCategoryButton(MaterialButton selectedBtn) {
        // Reset tất cả
        resetButtonBackground(btnSachGiaoKhoa);
        resetButtonBackground(btnSachKyNang);
        resetButtonBackground(btnSachThieuNhi);
        resetButtonBackground(btnTaiLieu);

        // Chọn cái được click
        selectedBtn.setBackgroundResource(R.drawable.background_volunteer);
        selectedCategory = selectedBtn.getText().toString();

        updatePoints();
    }

    // === XỬ LÝ TÌNH TRẠNG ===
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

    // === XỬ LÝ SỐ LƯỢNG ===
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

    // === TÍNH ĐIỂM ƯỚC TÍNH ===
    private void updatePoints() {
        int basePoint = getBasePointFromCategory();
        int conditionMultiplier = getConditionMultiplier();
        int total = basePoint * quantity * conditionMultiplier;

        tvPoints.setText(String.valueOf(total));
    }

    private int getBasePointFromCategory() {
        switch (selectedCategory) {
            case "Sách giáo khoa": case "Áo thun": case "Xếp hình": case "50.000đ":
                return 15;
            case "Sách kỹ năng": case "Áo khoác": case "Búp bê": case "100.000đ":
                return 20;
            case "Sách thiếu nhi": case "Quần": case "Xe đồ chơi": case "200.000đ":
                return 18;
            case "Tài liệu học tập": case "Đồng phục": case "Lego": case "500.000đ":
                return 25;
            default:
                return 15;
        }
    }

    private int getConditionMultiplier() {
        switch (selectedCondition) {
            case "Mới (100%)": return 10;      // x1.0
            case "Tốt (80%-90%)": return 9;    // x0.9
            case "Khá (60%-70%)": return 7;    // x0.7
            case "Ổn (50%)": return 5;         // x0.5
            default: return 10;
        }
    }

    // === XÁC NHẬN QUYÊN GÓP ===
    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> {
            String note = edtNote.getText() != null ? edtNote.getText().toString().trim() : "";

            Intent intent = new Intent(this, activity_volunteer_donation_confirm.class);
            intent.putExtra("DONATION_TYPE", donationTypeKey);
            intent.putExtra("CATEGORY", selectedCategory);
            intent.putExtra("QUANTITY", quantity);
            intent.putExtra("CONDITION", selectedCondition);
            intent.putExtra("POINTS", Integer.parseInt(tvPoints.getText().toString()));
            intent.putExtra("NOTE", note);

            startActivity(intent);
        });
    }
}