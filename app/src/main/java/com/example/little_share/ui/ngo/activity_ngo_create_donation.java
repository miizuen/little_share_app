package com.example.little_share.ui.ngo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import com.example.little_share.data.repositories.NotificationRepository;
import com.example.little_share.data.repositories.NotificationRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class activity_ngo_create_donation extends AppCompatActivity {

    // UI Components
    private TextInputEditText edtCampaignName, edtDescription, etStartDate, etEndDate;
    private TextInputEditText etLocation, etSpecificLocation, etContact;
    private TextInputEditText etOpenTime, etCloseTime;
    private AutoCompleteTextView actvCategory;
    private ImageView ivCampaignImage;
    private View layoutImagePlaceholder;
    private SeekBar seekBarPoints;
    private TextView tvPointsValue;
    private MaterialCheckBox cbUrgent;
    private MaterialButton btnCreateCampaign;

    // Data
    private Uri imageUri;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private final Calendar openTimeCalendar = Calendar.getInstance();
    private final Calendar closeTimeCalendar = Calendar.getInstance();

    // Repositories
    private CampaignRepository campaignRepository;
    private NotificationRepository notificationRepository;

    // Image picker
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
        setContentView(R.layout.activity_ngo_create_donation);

        initRepositories();
        initViews();
        setupToolbar();
        setupCategoryDropdown();
        setupImagePicker();
        setupDatePickers();
        setupTimePickers();
        setupPointsSeekBar();
        setupCreateButton();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initRepositories() {
        campaignRepository = new CampaignRepository();
        notificationRepository = new NotificationRepository();
    }

    private void initViews() {
        edtCampaignName = findViewById(R.id.edtCampaignName);
        edtDescription = findViewById(R.id.edtDescription);
        actvCategory = findViewById(R.id.actvCategory);
        ivCampaignImage = findViewById(R.id.ivCampaignImage);
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etLocation = findViewById(R.id.etLocation);
        etSpecificLocation = findViewById(R.id.etSpecificLocation);
        etOpenTime = findViewById(R.id.etOpenTime);
        etCloseTime = findViewById(R.id.etCloseTime);
        etContact = findViewById(R.id.etContact);
        seekBarPoints = findViewById(R.id.seekBarPoints);
        tvPointsValue = findViewById(R.id.tvPointsValue);
        cbUrgent = findViewById(R.id.cbUrgent);
        btnCreateCampaign = findViewById(R.id.btnCreateCampaign);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Giáo dục", "Nấu ăn và dinh dưỡng", "Môi trường", "Y tế", "Khẩn cấp"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);
    }

    private void setupImagePicker() {
        findViewById(R.id.cardImageUpload).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePicker.launch(intent);
        });
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

    private void setupTimePickers() {
        TimePickerDialog.OnTimeSetListener openTimeListener = (view, hourOfDay, minute) -> {
            openTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            openTimeCalendar.set(Calendar.MINUTE, minute);
            updateTime(etOpenTime, openTimeCalendar);
        };

        TimePickerDialog.OnTimeSetListener closeTimeListener = (view, hourOfDay, minute) -> {
            closeTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            closeTimeCalendar.set(Calendar.MINUTE, minute);
            updateTime(etCloseTime, closeTimeCalendar);
        };

        etOpenTime.setOnClickListener(v -> new TimePickerDialog(this, openTimeListener,
                openTimeCalendar.get(Calendar.HOUR_OF_DAY),
                openTimeCalendar.get(Calendar.MINUTE), true).show());

        etCloseTime.setOnClickListener(v -> new TimePickerDialog(this, closeTimeListener,
                closeTimeCalendar.get(Calendar.HOUR_OF_DAY),
                closeTimeCalendar.get(Calendar.MINUTE), true).show());
    }

    private void setupPointsSeekBar() {
        seekBarPoints.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int points = progress + 5; // Minimum 5 points
                tvPointsValue.setText(String.valueOf(points));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupCreateButton() {
        btnCreateCampaign.setOnClickListener(v -> createDonationCampaign());
    }

    private void updateDate(TextInputEditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
    }

    private void updateTime(TextInputEditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
    }

    private void createDonationCampaign() {
        if (!validateInput()) return;

        btnCreateCampaign.setEnabled(false);
        btnCreateCampaign.setText("Đang tạo...");

        // Tạo Campaign object
        Campaign campaign = new Campaign();
        campaign.setName(edtCampaignName.getText().toString().trim());
        campaign.setDescription(edtDescription.getText().toString().trim());

        // Set category
        String categoryText = actvCategory.getText().toString().trim();
        Campaign.CampaignCategory category = getCategoryEnum(categoryText);
        campaign.setCategoryCampaign(category);

        // Set dates
        campaign.setStartDate(startCalendar.getTime());
        campaign.setEndDate(endCalendar.getTime());

        // Set location
        campaign.setLocation(etLocation.getText().toString().trim());

        // Set working hours instead of specific location
        String workingHours = String.format("%s - %s",
                etOpenTime.getText().toString().trim(),
                etCloseTime.getText().toString().trim());
        campaign.setSpecificLocation(workingHours);

        // Set points reward
        int points = seekBarPoints.getProgress() + 5;
        campaign.setPointsReward(points);

        // Set contact
        campaign.setContactPhone(etContact.getText().toString().trim());

        // Set image
        if (imageUri != null) {
            campaign.setImageUrl(imageUri.toString());
        }

        // Set urgent status
        if (cbUrgent.isChecked()) {
            campaign.setCategoryCampaign(Campaign.CampaignCategory.URGENT);
        }

        // Donation campaigns don't need sponsor
        campaign.setNeedsSponsor(false);
        campaign.setTargetBudget(0);

        // Create campaign
        campaignRepository.getOrganizationNameAndCreate(campaign, new CampaignRepository.OnCampaignListener() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_ngo_create_donation.this,
                            "Tạo chiến dịch quyên góp thành công!", Toast.LENGTH_LONG).show();
                    NotificationRepository notificationRepo = new NotificationRepository();
                    notificationRepo.notifyVolunteersAboutNewCampaign(
                            campaign.getId(),
                            campaign.getName(),
                            campaign.getOrganizationName(),
                            new NotificationRepository.OnNotificationListener() {
                                @Override
                                public void onSuccess(String message) {
                                    Log.d("CreateDonation", "Notifications sent: " + message);
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.e("CreateDonation", "Failed to send notifications: " + error);
                                }
                            }
                    );
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_ngo_create_donation.this,
                            "Lỗi tạo chiến dịch: " + error, Toast.LENGTH_LONG).show();

                    btnCreateCampaign.setEnabled(true);
                    btnCreateCampaign.setText("Tạo hoạt động");
                });
            }
        });
    }

    private Campaign.CampaignCategory getCategoryEnum(String categoryText) {
        switch (categoryText) {
            case "Môi trường":
                return Campaign.CampaignCategory.ENVIRONMENT;
            case "Giáo dục":
                return Campaign.CampaignCategory.EDUCATION;
            case "Y tế":
                return Campaign.CampaignCategory.HEALTH;
            case "Nấu ăn và dinh dưỡng":
                return Campaign.CampaignCategory.FOOD;
            case "Khẩn cấp":
                return Campaign.CampaignCategory.URGENT;
            default:
                return Campaign.CampaignCategory.EDUCATION;
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(edtCampaignName.getText())) {
            edtCampaignName.setError("Vui lòng nhập tên chiến dịch");
            return false;
        }

        if (TextUtils.isEmpty(edtDescription.getText())) {
            edtDescription.setError("Vui lòng nhập mô tả");
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
            return false;
        }

        if (TextUtils.isEmpty(etOpenTime.getText()) || TextUtils.isEmpty(etCloseTime.getText())) {
            Toast.makeText(this, "Vui lòng chọn giờ mở cửa và đóng cửa", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(etContact.getText())) {
            etContact.setError("Vui lòng nhập thông tin liên hệ");
            return false;
        }

        return true;
    }
}
