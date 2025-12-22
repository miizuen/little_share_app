package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.little_share.R;
import com.example.little_share.data.models.VolunteerRegistration;
import com.example.little_share.utils.QRCodeGenerator;
import com.example.little_share.data.repositories.NotificationRepository;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.button.MaterialButton;
import android.widget.Toast;

public class activity_volunteer_detail_calendar extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvEventTitle, tvDate, tvTime, tvLocation, tvParticipants, tvDescription;
    private ImageView ivQRCode;
    private MaterialButton btnCancel;
    private VolunteerRegistration registration;
    private Chip chipRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_detail_calendar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadDataFromIntent();
        setupClickListeners();
    }
    private void generateAndDisplayQR() {
        if (registration != null && registration.getQrCode() != null) {
            // Dùng QRCodeGenerator có sẵn với size 400
            Bitmap qrBitmap = QRCodeGenerator.generateQRCode(registration.getQrCode(), 400);
            if (qrBitmap != null) {
                ivQRCode.setImageBitmap(qrBitmap);
            }
        }
    }



    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvLocation = findViewById(R.id.tvLocation);
        tvParticipants = findViewById(R.id.tvParticipants);
        tvDescription = findViewById(R.id.tvDescription);

        // Thêm các view mới
        ivQRCode = findViewById(R.id.ivQRCode);
        btnCancel = findViewById(R.id.btnCancel);
        chipRole = findViewById(R.id.chipRole);
    }


    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // THÊM LOG DEBUG
            android.util.Log.d("CALENDAR_DEBUG", "=== LOADING DATA FROM INTENT ===");

            // Cách 1: Nếu có registrationId từ notification
            String registrationId = intent.getStringExtra("registrationId");
            android.util.Log.d("CALENDAR_DEBUG", "registrationId: " + registrationId);

            if (registrationId != null) {
                android.util.Log.d("CALENDAR_DEBUG", "Loading from Firebase with ID: " + registrationId);
                loadRegistrationFromFirebase(registrationId);
                return;
            }

            // Cách 2: Nếu có registration object trực tiếp
            registration = (VolunteerRegistration) intent.getSerializableExtra("registration");
            android.util.Log.d("CALENDAR_DEBUG", "registration object: " + (registration != null ? "Found" : "NULL"));

            if (registration != null) {
                android.util.Log.d("CALENDAR_DEBUG", "Displaying registration data directly");
                displayRegistrationData();
                return;
            }

            // Cách 3: Fallback - load từ intent extras (code cũ)
            android.util.Log.d("CALENDAR_DEBUG", "Using fallback method");
            tvEventTitle.setText(intent.getStringExtra("campaign_title"));
            tvDate.setText(intent.getStringExtra("date"));
            tvTime.setText(intent.getStringExtra("time"));
        } else {
            android.util.Log.d("CALENDAR_DEBUG", "Intent is NULL");
        }
    }
    private void displayRegistrationData() {
        if (registration == null) return;

        // Load từ VolunteerRegistration object
        tvEventTitle.setText(registration.getCampaignName());
        tvDate.setText(registration.getDate());

        // Kết hợp shift name và time
        String timeInfo = registration.getShiftName() + " (" + registration.getShiftTime() + ")";
        tvTime.setText(timeInfo);

        // Hiển thị role trong chip
        if (chipRole != null) {
            chipRole.setText(registration.getRoleName());
        }

        // Hiển thị location (có thể cần load từ campaign hoặc có sẵn)
        // Tạm thời để mặc định, có thể cần load từ campaign details
        tvLocation.setText("Đang tải địa điểm...");

        // Hiển thị notes trong description (nếu có)
        String description = registration.getNote();
        if (description != null && !description.isEmpty()) {
            tvDescription.setText(description);
        } else {
            tvDescription.setText("Không có ghi chú");
        }

        // Load thông tin tổ chức
        loadOrganizationInfo(registration.getOrganizationId());

        // Load thông tin địa điểm từ campaign
        loadCampaignLocation(registration.getCampaignId());

        // Kiểm tra trạng thái và hiển thị QR code
        String status = registration.getStatus();
        if ("approved".equals(status)) {
            generateAndDisplayQR(); // Hiển thị QR code
        }
    }

    private void loadRegistrationFromFirebase(String registrationId) {
        android.util.Log.d("CALENDAR_DEBUG", "Starting Firebase query for: " + registrationId);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("volunteer_registrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    android.util.Log.d("CALENDAR_DEBUG", "Firebase query success");
                    android.util.Log.d("CALENDAR_DEBUG", "Document exists: " + documentSnapshot.exists());

                    if (documentSnapshot.exists()) {
                        registration = documentSnapshot.toObject(VolunteerRegistration.class);
                        android.util.Log.d("CALENDAR_DEBUG", "Registration parsed: " + (registration != null ? "SUCCESS" : "FAILED"));

                        if (registration != null) {
                            registration.setId(documentSnapshot.getId());
                            android.util.Log.d("CALENDAR_DEBUG", "Campaign name: " + registration.getCampaignName());
                            displayRegistrationData();
                        }
                    } else {
                        android.util.Log.d("CALENDAR_DEBUG", "Document not found");
                        Toast.makeText(this, "Không tìm thấy thông tin đăng ký", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CALENDAR_DEBUG", "Firebase query failed: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        // Xử lý nút hủy tham gia
        btnCancel.setOnClickListener(v -> showCancelConfirmDialog());
    }

    // Hiển thị dialog xác nhận hủy
    private void showCancelConfirmDialog() {
        if (registration == null) {
            Toast.makeText(this, "Không có thông tin đăng ký", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy")
                .setMessage("Bạn có chắc chắn muốn hủy tham gia chiến dịch này?\n\nLưu ý: Bạn chỉ được hủy tối đa 2 lần cho mỗi chiến dịch.")
                .setPositiveButton("Hủy tham gia", (dialog, which) -> cancelRegistration())
                .setNegativeButton("Quay lại", null)
                .show();
    }

    // Thực hiện hủy đăng ký
    private void cancelRegistration() {
        if (registration == null || registration.getId() == null) {
            Toast.makeText(this, "Không tìm thấy thông tin đăng ký", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCancel.setEnabled(false);
        btnCancel.setText("Đang xử lý...");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("volunteer_registrations")
                .document(registration.getId())
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    // Gửi thông báo cho tổ chức
                    sendCancelNotificationToOrg();
                    
                    Toast.makeText(this, "Đã hủy tham gia thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnCancel.setEnabled(true);
                    btnCancel.setText("Hủy tham gia");
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Gửi thông báo hủy cho tổ chức
    private void sendCancelNotificationToOrg() {
        if (registration == null) return;
        
        String orgId = registration.getOrganizationId();
        if (orgId == null || orgId.isEmpty()) return;

        NotificationRepository notificationRepo = new NotificationRepository();
        notificationRepo.notifyNGOCancelRegistrationWithUserLookup(
                orgId,
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                registration.getCampaignName(),
                registration.getRoleName(),
                registration.getDate()
        );
    }

    private void loadOrganizationInfo(String organizationId) {
        android.util.Log.d("ORG_DEBUG", "=== LOADING ORGANIZATION INFO ===");
        android.util.Log.d("ORG_DEBUG", "organizationId: " + organizationId);

        if (organizationId == null || organizationId.trim().isEmpty()) {
            tvParticipants.setText("Không có thông tin tổ chức");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // THÊM LOG: Kiểm tra user authentication
        android.util.Log.d("ORG_DEBUG", "Current user: " +
                (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? "Logged in" : "Not logged in"));

        android.util.Log.d("ORG_DEBUG", "Starting query for ID: " + organizationId);

        db.collection("organization")
                .whereEqualTo(FieldPath.documentId(), organizationId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    android.util.Log.d("ORG_DEBUG", "Query by whereEqualTo completed");
                    android.util.Log.d("ORG_DEBUG", "Documents found: " + querySnapshot.size());

                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String name = doc.getString("name");
                        tvParticipants.setText(name != null ? name : "Không có tên");
                    } else {
                        tvParticipants.setText("Không tìm thấy tổ chức");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ORG_DEBUG", "Alternative query failed: " + e.getMessage());
                });
    }


    // THÊM HÀM FALLBACK MỚI
    private void loadOrganizationFromCampaign(String campaignId) {
        android.util.Log.d("ORG_DEBUG", "=== FALLBACK: Loading org from campaign ===");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("campaigns")
                .document(campaignId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String orgId = documentSnapshot.getString("organizationId");
                        String orgName = documentSnapshot.getString("organizationName");

                        android.util.Log.d("ORG_DEBUG", "Campaign orgId: " + orgId);
                        android.util.Log.d("ORG_DEBUG", "Campaign orgName: " + orgName);

                        if (orgName != null && !orgName.isEmpty()) {
                            // Nếu campaign có sẵn tên tổ chức
                            tvParticipants.setText(orgName);
                        } else if (orgId != null && !orgId.isEmpty()) {
                            // Nếu chỉ có ID, load từ organizations
                            loadOrganizationInfo(orgId);
                        } else {
                            tvParticipants.setText("Không tìm thấy thông tin tổ chức");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ORG_DEBUG", "Fallback failed: " + e.getMessage());
                    tvParticipants.setText("Lỗi tải thông tin");
                });
    }
    private void loadCampaignLocation(String campaignId) {
        if (campaignId == null || campaignId.isEmpty()) {
            tvLocation.setText("Không có thông tin địa điểm");
            return;
        }

        android.util.Log.d("CALENDAR_DEBUG", "Loading campaign location for ID: " + campaignId);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("campaigns")
                .document(campaignId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String location = documentSnapshot.getString("location");
                        String specificLocation = documentSnapshot.getString("specificLocation");

                        android.util.Log.d("CALENDAR_DEBUG", "Location: " + location + ", Specific: " + specificLocation);

                        String displayLocation = "";
                        if (specificLocation != null && !specificLocation.isEmpty()) {
                            displayLocation = specificLocation;
                        } else if (location != null && !location.isEmpty()) {
                            displayLocation = location;
                        } else {
                            displayLocation = "Chưa có thông tin địa điểm";
                        }

                        tvLocation.setText(displayLocation);
                    } else {
                        android.util.Log.d("CALENDAR_DEBUG", "Campaign document not found");
                        tvLocation.setText("Không tìm thấy thông tin chiến dịch");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CALENDAR_DEBUG", "Failed to load campaign: " + e.getMessage());
                    tvLocation.setText("Lỗi tải thông tin địa điểm");
                });
    }
}
