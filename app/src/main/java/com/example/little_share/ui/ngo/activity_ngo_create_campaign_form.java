package com.example.little_share.ui.ngo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class activity_ngo_create_campaign_form extends AppCompatActivity {
    private TextInputEditText etCampaignName, etDescription, etStartDate, etEndDate;
    private TextInputEditText etLocation, etSpecificLocation, etBudget, etPurpose, etYeuCau;
    private AutoCompleteTextView actvCategory;
    private ImageView ivCampaignImage;
    private View layoutImagePlaceholder;
    private SwitchMaterial switchNeedSponsor;
    private LinearLayout layoutSponsorDetails;
    private Uri imageUri;

    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();

    private final ActivityResultLauncher<Intent> imagePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).into(ivCampaignImage);
                    ivCampaignImage.setVisibility(View.VISIBLE);
                    layoutImagePlaceholder.setVisibility(View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_create_campaign_form);

        initViews();
        setupToolbar();
        setupCategoryDropdown();
        setupImagePicker();
        setupDatePickers();
        setupSponsorSwitch();
        setupNextButton();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupSponsorSwitch() {
        switchNeedSponsor.setOnCheckedChangeListener((buttonView, isChecked) ->
                layoutSponsorDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    private void setupNextButton() {
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            if (!validateBasicInfo()) return;

            // Lấy dữ liệu từ form
            String name = etCampaignName.getText() != null ? etCampaignName.getText().toString().trim() : "";
            String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String category = actvCategory.getText() != null ? actvCategory.getText().toString().trim() : "";
            String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
            String specificLoc = etSpecificLocation.getText() != null ? etSpecificLocation.getText().toString().trim() : "";
            String yeuCau = etYeuCau.getText() != null ? etYeuCau.getText().toString().trim() : "";

            // Format dates thành String
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String startDateStr = sdf.format(startCalendar.getTime());
            String endDateStr = sdf.format(endCalendar.getTime());

            // Xử lý sponsor
            String budgetStr = "";
            String purposeStr = "";
            if (switchNeedSponsor.isChecked()) {
                budgetStr = etBudget.getText() != null ? etBudget.getText().toString().replace(".", "").trim() : "0";
                purposeStr = etPurpose.getText() != null ? etPurpose.getText().toString().trim() : "";
            }

            // Chỉ đi màn hình thêm role (with role)
            Intent intent = new Intent(this, activity_ngo_create_campagin_with_role.class);

            // Truyền dữ liệu
            intent.putExtra("campaignName", name);
            intent.putExtra("campaignDescription", desc);
            intent.putExtra("category", category);
            intent.putExtra("imageUrl", imageUri != null ? imageUri.toString() : "");
            intent.putExtra("location", location);
            intent.putExtra("specificLocation", specificLoc);
            intent.putExtra("yeuCau", yeuCau);
            intent.putExtra("startDate", startDateStr);
            intent.putExtra("endDate", endDateStr);
            intent.putExtra("needsSponsor", switchNeedSponsor.isChecked());
            intent.putExtra("targetBudget", budgetStr);
            intent.putExtra("budgetPurpose", purposeStr);

            startActivity(intent);
        });
    }

    private void setupDatePickers() {
        DatePickerDialog.OnDateSetListener startListener = (view, year, month, dayOfMonth) -> {
            startCalendar.set(Calendar.YEAR, year);
            startCalendar.set(Calendar.MONTH, month);
            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDate(etStartDate, startCalendar);
        };

        DatePickerDialog.OnDateSetListener endListener = (view, year, month, dayOfMonth) -> {
            endCalendar.set(Calendar.YEAR, year);
            endCalendar.set(Calendar.MONTH, month);
            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDate(etEndDate, endCalendar);
        };

        etStartDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, startListener,
                    startCalendar.get(Calendar.YEAR),
                    startCalendar.get(Calendar.MONTH),
                    startCalendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        etEndDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, endListener,
                    endCalendar.get(Calendar.YEAR),
                    endCalendar.get(Calendar.MONTH),
                    endCalendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private void updateDate(TextInputEditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(calendar.getTime());
        editText.setText(formattedDate);
    }

    private void setupImagePicker() {
        findViewById(R.id.cardImageUpload).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePicker.launch(intent);
        });
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Giáo dục", "Nấu ăn và dinh dưỡng", "Môi trường", "Y tế", "Khẩn cấp"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        etCampaignName = findViewById(R.id.etCampaignName);
        etDescription = findViewById(R.id.etDescription);
        actvCategory = findViewById(R.id.actvCategory);
        ivCampaignImage = findViewById(R.id.ivCampaignImage);
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etLocation = findViewById(R.id.etLocation);
        etSpecificLocation = findViewById(R.id.etSpecificLocation);
        etYeuCau = findViewById(R.id.etYeuCau);
        switchNeedSponsor = findViewById(R.id.switchNeedSponsor);
        layoutSponsorDetails = findViewById(R.id.layoutSponsorDetails);
        etBudget = findViewById(R.id.etBudget);
        etPurpose = findViewById(R.id.etPurpose);
    }

    private boolean validateBasicInfo() {
        boolean isValid = true;

        // Kiểm tra tên chiến dịch
        if (TextUtils.isEmpty(etCampaignName.getText())) {
            etCampaignName.setError("Vui lòng nhập tên chiến dịch");
            etCampaignName.requestFocus();
            isValid = false;
        }

        // Kiểm tra mô tả
        if (TextUtils.isEmpty(etDescription.getText())) {
            etDescription.setError("Vui lòng nhập mô tả hoạt động");
            if (isValid) etDescription.requestFocus();
            isValid = false;
        }

        // Kiểm tra lĩnh vực
        if (TextUtils.isEmpty(actvCategory.getText())) {
            Toast.makeText(this, "Vui lòng chọn lĩnh vực hoạt động", Toast.LENGTH_SHORT).show();
            if (isValid) actvCategory.requestFocus();
            isValid = false;
        }

        // Kiểm tra ngày bắt đầu
        if (TextUtils.isEmpty(etStartDate.getText())) {
            Toast.makeText(this, "Vui lòng chọn thời gian bắt đầu", Toast.LENGTH_SHORT).show();
            if (isValid) etStartDate.requestFocus();
            isValid = false;
        }

        // Kiểm tra ngày kết thúc
        if (TextUtils.isEmpty(etEndDate.getText())) {
            Toast.makeText(this, "Vui lòng chọn thời gian kết thúc", Toast.LENGTH_SHORT).show();
            if (isValid) etEndDate.requestFocus();
            isValid = false;
        }

        // Kiểm tra địa điểm
        if (TextUtils.isEmpty(etLocation.getText())) {
            etLocation.setError("Vui lòng nhập địa điểm");
            if (isValid) etLocation.requestFocus();
            isValid = false;
        }

        // Kiểm tra địa điểm cụ thể
        if (TextUtils.isEmpty(etSpecificLocation.getText())) {
            etSpecificLocation.setError("Vui lòng nhập địa điểm cụ thể");
            if (isValid) etSpecificLocation.requestFocus();
            isValid = false;
        }

        // Kiểm tra yêu cầu
        if (TextUtils.isEmpty(etYeuCau.getText())) {
            etYeuCau.setError("Vui lòng nhập yêu cầu");
            if (isValid) etYeuCau.requestFocus();
            isValid = false;
        }

        // Kiểm tra thông tin tài trợ nếu bật switch
        if (switchNeedSponsor.isChecked()) {
            if (TextUtils.isEmpty(etBudget.getText())) {
                etBudget.setError("Vui lòng nhập ngân sách dự kiến");
                if (isValid) etBudget.requestFocus();
                isValid = false;
            }

            if (TextUtils.isEmpty(etPurpose.getText())) {
                etPurpose.setError("Vui lòng nhập mục đích sử dụng");
                if (isValid) etPurpose.requestFocus();
                isValid = false;
            }
        }

        // Thông báo tổng hợp nếu có lỗi
        if (!isValid) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_LONG).show();
        }

        return isValid;
    }
}

