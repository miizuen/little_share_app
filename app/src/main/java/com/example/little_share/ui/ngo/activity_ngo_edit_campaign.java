package com.example.little_share.ui.ngo;

import android.app.AlertDialog;
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
import android.widget.TextView;
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
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.helper.ImgBBUploader;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class activity_ngo_edit_campaign extends AppCompatActivity {
    private TextInputEditText etCampaignName, etDescription, etStartDate, etEndDate;
    private TextInputEditText etLocation, etSpecificLocation, etBudget, etPurpose;
    private AutoCompleteTextView actvCategory;
    private ImageView ivCampaignImage;
    private View layoutImagePlaceholder;
    private SwitchMaterial switchNeedSponsor;
    private LinearLayout layoutSponsorDetails;
    private TextView tvRoleType;
    private MaterialButton btnUpdate, btnDelete;

    private Uri imageUri;
    private String currentImageUrl;
    private Campaign campaign;
    private boolean hasRoles = false;

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
        setContentView(R.layout.activity_ngo_edit_campaign);

        campaign = (Campaign) getIntent().getSerializableExtra("campaign");
        if (campaign == null) {
            Toast.makeText(this, "Không tìm thấy thông tin chiến dịch", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupCategoryDropdown();
        setupImagePicker();
        setupDatePickers();
        setupSponsorSwitch();
        fillDataForEdit();
        setupUpdateButton();
        setupDeleteButton();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fillDataForEdit() {
        // Fill basic info
        etCampaignName.setText(campaign.getName());
        etDescription.setText(campaign.getDescription());

        // Set category
        Campaign.CampaignCategory categoryEnum = campaign.getCategoryEnum();
        actvCategory.setText(categoryEnum.getDisplayName(), false);

        // Load image
        currentImageUrl = campaign.getImageUrl();
        if (!TextUtils.isEmpty(currentImageUrl)) {
            Glide.with(this).load(currentImageUrl).into(ivCampaignImage);
            ivCampaignImage.setVisibility(View.VISIBLE);
            layoutImagePlaceholder.setVisibility(View.GONE);
        }

        // Set dates
        if (campaign.getStartDate() != null) {
            startCalendar.setTime(campaign.getStartDate());
            updateDate(etStartDate, startCalendar);
        }
        if (campaign.getEndDate() != null) {
            endCalendar.setTime(campaign.getEndDate());
            updateDate(etEndDate, endCalendar);
        }

        // Set location
        etLocation.setText(campaign.getLocation());
        etSpecificLocation.setText(campaign.getSpecificLocation());

        // Set sponsor info
        switchNeedSponsor.setChecked(campaign.isNeedsSponsor());
        if (campaign.isNeedsSponsor()) {
            layoutSponsorDetails.setVisibility(View.VISIBLE);
            etBudget.setText(String.valueOf((int) campaign.getTargetBudget()));
            etPurpose.setText(campaign.getBudgetPurpose());
        }

        // Check if has roles and display
        hasRoles = campaign.getRoles() != null && !campaign.getRoles().isEmpty();
        if (hasRoles) {
            tvRoleType.setText("✓ Có phân chia vai trò (" + campaign.getRoles().size() + " vai trò)");
        } else {
            tvRoleType.setText("✓ Không phân chia vai trò");
        }
    }

    private void setupDeleteButton() {
        btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa chiến dịch")
                .setMessage("Bạn có chắc chắn muốn xóa chiến dịch này?\n\nHành động này không thể hoàn tác và sẽ xóa tất cả dữ liệu liên quan.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCampaign())
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteCampaign() {
        Toast.makeText(this, "Đang xóa...", Toast.LENGTH_SHORT).show();

        new CampaignRepository().deleteCampaign(campaign.getId(), new CampaignRepository.OnCampaignListener() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(activity_ngo_edit_campaign.this,
                        "Đã xóa chiến dịch thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(activity_ngo_edit_campaign.this,
                        "Lỗi khi xóa: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUpdateButton() {
        btnUpdate.setOnClickListener(v -> {
            if (!validateBasicInfo()) return;

            // Update campaign data
            campaign.setName(etCampaignName.getText().toString().trim());
            campaign.setDescription(etDescription.getText().toString().trim());

            String category = actvCategory.getText().toString().trim();
            switch (category) {
                case "Môi trường":
                    campaign.setCategoryCampaign(Campaign.CampaignCategory.ENVIRONMENT);
                    break;
                case "Giáo dục":
                    campaign.setCategoryCampaign(Campaign.CampaignCategory.EDUCATION);
                    break;
                case "Y tế":
                    campaign.setCategoryCampaign(Campaign.CampaignCategory.HEALTH);
                    break;
                case "Nấu ăn và dinh dưỡng":
                    campaign.setCategoryCampaign(Campaign.CampaignCategory.FOOD);
                    break;
                case "Khẩn cấp":
                    campaign.setCategoryCampaign(Campaign.CampaignCategory.URGENT);
                    break;
            }

            campaign.setLocation(etLocation.getText().toString().trim());
            campaign.setSpecificLocation(etSpecificLocation.getText().toString().trim());
            campaign.setStartDate(startCalendar.getTime());
            campaign.setEndDate(endCalendar.getTime());
            campaign.setNeedsSponsor(switchNeedSponsor.isChecked());

            if (switchNeedSponsor.isChecked()) {
                String budgetStr = etBudget.getText().toString().replace(".", "").trim();
                try {
                    double budget = TextUtils.isEmpty(budgetStr) ? 0 : Double.parseDouble(budgetStr);
                    campaign.setTargetBudget(budget);
                } catch (Exception e) {
                    campaign.setTargetBudget(0);
                }
                campaign.setBudgetPurpose(etPurpose.getText().toString().trim());
            }

            // Handle image upload if changed
            if (imageUri != null) {
                uploadImageAndUpdate();
            } else {
                updateCampaignDirectly();
            }
        });
    }

    private void uploadImageAndUpdate() {
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

        ImgBBUploader.uploadImage(this, imageUri, new ImgBBUploader.UploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    campaign.setImageUrl(imageUrl);
                    updateCampaignDirectly();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_ngo_edit_campaign.this,
                            "Upload ảnh thất bại: " + error, Toast.LENGTH_LONG).show();

                    new AlertDialog.Builder(activity_ngo_edit_campaign.this)
                            .setTitle("Upload ảnh thất bại")
                            .setMessage("Bạn có muốn cập nhật mà không thay đổi ảnh?")
                            .setPositiveButton("Có", (dialog, which) -> {
                                campaign.setImageUrl(currentImageUrl);
                                updateCampaignDirectly();
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                });
            }

            @Override
            public void onProgress(int progress) {
            }
        });
    }

    private void updateCampaignDirectly() {
        Toast.makeText(this, "Đang cập nhật...", Toast.LENGTH_SHORT).show();

        new CampaignRepository().updateCampaign(campaign.getId(), campaign,
                new CampaignRepository.OnCampaignListener() {
                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(activity_ngo_edit_campaign.this,
                                "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(activity_ngo_edit_campaign.this,
                                "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSponsorSwitch() {
        switchNeedSponsor.setOnCheckedChangeListener((buttonView, isChecked) ->
                layoutSponsorDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    private void setupDatePickers() {
        DatePickerDialog.OnDateSetListener startListener = (view, year, month, dayOfMonth) -> {
            startCalendar.set(year, month, dayOfMonth);
            updateDate(etStartDate, startCalendar);
        };

        DatePickerDialog.OnDateSetListener endListener = (view, year, month, dayOfMonth) -> {
            endCalendar.set(year, month, dayOfMonth);
            updateDate(etEndDate, endCalendar);
        };

        etStartDate.setOnClickListener(v -> new DatePickerDialog(this, startListener,
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)).show());

        etEndDate.setOnClickListener(v -> new DatePickerDialog(this, endListener,
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void updateDate(TextInputEditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
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
        switchNeedSponsor = findViewById(R.id.switchNeedSponsor);
        layoutSponsorDetails = findViewById(R.id.layoutSponsorDetails);
        etBudget = findViewById(R.id.etBudget);
        etPurpose = findViewById(R.id.etPurpose);
        tvRoleType = findViewById(R.id.tvRoleType);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private boolean validateBasicInfo() {
        if (TextUtils.isEmpty(etCampaignName.getText())) {
            etCampaignName.setError("Vui lòng nhập tên chiến dịch");
            etCampaignName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etDescription.getText())) {
            etDescription.setError("Vui lòng nhập mô tả");
            etDescription.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(actvCategory.getText())) {
            Toast.makeText(this, "Vui lòng chọn lĩnh vực", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etStartDate.getText()) || TextUtils.isEmpty(etEndDate.getText())) {
            Toast.makeText(this, "Vui lòng chọn ngày bắt đầu và kết thúc", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etLocation.getText())) {
            etLocation.setError("Vui lòng nhập địa điểm");
            etLocation.requestFocus();
            return false;
        }
        return true;
    }
}