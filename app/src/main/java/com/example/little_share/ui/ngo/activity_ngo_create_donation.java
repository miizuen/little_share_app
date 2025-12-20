package com.example.little_share.ui.ngo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class activity_ngo_create_donation extends AppCompatActivity {

    private static final String TAG = "CreateDonationCampaign";

    // Views
    private MaterialToolbar toolbar;
    private TextInputEditText edtCampaignName, edtDescription;
    private AutoCompleteTextView actvDonationType;
    private TextInputEditText etStartDate, etEndDate;
    private TextInputEditText etLocation;
    private TextInputEditText etOpenTime, etCloseTime;
    private TextInputEditText etContact;
    private MaterialCheckBox cbUrgent;
    private MaterialButton btnCreateCampaign;
    private ImageView ivCampaignImage;
    private View layoutImagePlaceholder;

    // Data
    private Uri selectedImageUri;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private String openTime = "";
    private String closeTime = "";

    // Repository
    private CampaignRepository repository;

    // Firebase Storage
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Image picker
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngo_create_donation);

        initializeComponents();
        setupToolbar();
        setupDonationTypeDropdown();
        setupDatePickers();
        setupTimePickers();
        setupImagePicker();
        setupCreateButton();
    }

    private void initializeComponents() {
        // Repository
        repository = new CampaignRepository();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Views
        toolbar = findViewById(R.id.toolbar);
        edtCampaignName = findViewById(R.id.edtCampaignName);
        edtDescription = findViewById(R.id.edtDescription);
        actvDonationType = findViewById(R.id.actvDonationType);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etLocation = findViewById(R.id.etLocation);
        etOpenTime = findViewById(R.id.etOpenTime);
        etCloseTime = findViewById(R.id.etCloseTime);
        etContact = findViewById(R.id.etContact);
        cbUrgent = findViewById(R.id.cbUrgent);
        btnCreateCampaign = findViewById(R.id.btnCreateCampaign);
        ivCampaignImage = findViewById(R.id.ivCampaignImage);
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder);

        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        displaySelectedImage(uri);
                    }
                }
        );
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDonationTypeDropdown() {
        String[] donationTypes = new String[]{
                "Sách vở",
                "Quần áo",
                "Đồ chơi",
                "Nhu yếu phẩm"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                donationTypes
        );

        actvDonationType.setAdapter(adapter);
    }

    private void setupDatePickers() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Start date picker
        etStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        startCalendar.set(year, month, dayOfMonth);
                        etStartDate.setText(dateFormat.format(startCalendar.getTime()));
                    },
                    startCalendar.get(Calendar.YEAR),
                    startCalendar.get(Calendar.MONTH),
                    startCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        // End date picker
        etEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        endCalendar.set(year, month, dayOfMonth);
                        etEndDate.setText(dateFormat.format(endCalendar.getTime()));
                    },
                    endCalendar.get(Calendar.YEAR),
                    endCalendar.get(Calendar.MONTH),
                    endCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
            datePickerDialog.show();
        });
    }

    private void setupTimePickers() {
        // Open time picker
        etOpenTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        openTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        etOpenTime.setText(openTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        // Close time picker
        etCloseTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        closeTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        etCloseTime.setText(closeTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });
    }

    private void setupImagePicker() {
        findViewById(R.id.cardImageUpload).setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });
    }

    private void displaySelectedImage(Uri uri) {
        ivCampaignImage.setVisibility(View.VISIBLE);
        layoutImagePlaceholder.setVisibility(View.GONE);

        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(ivCampaignImage);
    }

    private void setupCreateButton() {
        btnCreateCampaign.setOnClickListener(v -> {
            if (validateInputs()) {
                createCampaign();
            }
        });
    }

    private boolean validateInputs() {
        String name = edtCampaignName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String donationType = actvDonationType.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String contact = etContact.getText().toString().trim();

        if (name.isEmpty()) {
            edtCampaignName.setError("Vui lòng nhập tên hoạt động");
            edtCampaignName.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            edtDescription.setError("Vui lòng nhập mô tả");
            edtDescription.requestFocus();
            return false;
        }

        if (donationType.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn loại quyên góp", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (startDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày bắt đầu", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (endDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày kết thúc", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (startCalendar.after(endCalendar)) {
            Toast.makeText(this, "Ngày kết thúc phải sau ngày bắt đầu", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (location.isEmpty()) {
            etLocation.setError("Vui lòng nhập địa điểm");
            etLocation.requestFocus();
            return false;
        }

        if (openTime.isEmpty() || closeTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn giờ hoạt động", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (contact.isEmpty()) {
            etContact.setError("Vui lòng nhập thông tin liên hệ");
            etContact.requestFocus();
            return false;
        }

        return true;
    }

    private void createCampaign() {
        // Disable button
        btnCreateCampaign.setEnabled(false);
        btnCreateCampaign.setText("Đang tạo...");

        // Get form data
        String name = edtCampaignName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String donationTypeStr = actvDonationType.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        boolean isUrgent = cbUrgent.isChecked();

        // Convert donation type display name to enum
        Campaign.DonationType donationType = convertDonationType(donationTypeStr);

        // Convert to category for backward compatibility
        Campaign.CampaignCategory category = isUrgent ?
                Campaign.CampaignCategory.URGENT :
                Campaign.CampaignCategory.EDUCATION;

        // Create working hours string
        String workingHours = openTime + " - " + closeTime;

        // Create campaign object
        Campaign campaign = new Campaign();
        campaign.setName(name);
        campaign.setDescription(description);
        campaign.setDonationTypeEnum(donationType);
        campaign.setTypeCampaign(Campaign.CampaignType.DONATION);
        campaign.setCategoryCampaign(category);
        campaign.setLocation(location);
        campaign.setSpecificLocation(workingHours);
        campaign.setStartDate(startCalendar.getTime());
        campaign.setEndDate(endCalendar.getTime());
        campaign.setContactPhone(contact);
        campaign.setStatus(Campaign.CampaignStatus.UPCOMING.name());
        campaign.setPointsReward(0); // Không cần điểm cho donation campaign
        campaign.setMaxVolunteers(0); // Không cần số lượng tình nguyện viên
        campaign.setCurrentVolunteers(0);

        Log.d(TAG, "Creating donation campaign:");
        Log.d(TAG, "  Name: " + name);
        Log.d(TAG, "  Donation Type: " + donationType.name());
        Log.d(TAG, "  Category: " + category.name());
        Log.d(TAG, "  Location: " + location);
        Log.d(TAG, "  Working Hours: " + workingHours);

        // Upload image if selected
        if (selectedImageUri != null) {
            uploadImageAndCreateCampaign(campaign);
        } else {
            saveCampaignToFirebase(campaign);
        }
    }

    private Campaign.DonationType convertDonationType(String displayName) {
        switch (displayName) {
            case "Sách vở":
                return Campaign.DonationType.BOOKS;
            case "Quần áo":
                return Campaign.DonationType.CLOTHES;
            case "Đồ chơi":
                return Campaign.DonationType.TOYS;
            case "Nhu yếu phẩm":
                return Campaign.DonationType.ESSENTIALS;
            default:
                return Campaign.DonationType.MIXED;
        }
    }

    private void uploadImageAndCreateCampaign(Campaign campaign) {
        String fileName = "campaign_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child("campaigns/" + fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        campaign.setImageUrl(uri.toString());
                        saveCampaignToFirebase(campaign);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải ảnh lên. Tạo chiến dịch không có ảnh...", Toast.LENGTH_SHORT).show();
                    saveCampaignToFirebase(campaign);
                });
    }

    private void saveCampaignToFirebase(Campaign campaign) {
        repository.getOrganizationNameAndCreate(campaign, new CampaignRepository.OnCampaignListener() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_ngo_create_donation.this,
                            "Tạo chiến dịch quyên góp thành công!",
                            Toast.LENGTH_LONG).show();

                    Log.d(TAG, "Campaign created successfully: " + result);
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(activity_ngo_create_donation.this,
                            "Lỗi: " + error,
                            Toast.LENGTH_LONG).show();

                    Log.e(TAG, "Failed to create campaign: " + error);

                    // Re-enable button
                    btnCreateCampaign.setEnabled(true);
                    btnCreateCampaign.setText("Tạo hoạt động");
                });
            }
        });
    }
}