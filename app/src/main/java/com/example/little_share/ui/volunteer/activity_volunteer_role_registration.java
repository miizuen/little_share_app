package com.example.little_share.ui.volunteer;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.little_share.data.repositories.NotificationRepository;



import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.CampaignRole;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.Shift;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ImageView;
import androidx.cardview.widget.CardView;

public class activity_volunteer_role_registration extends AppCompatActivity {

    private CampaignRole role;
    private Campaign campaign;
    private FirebaseFirestore db;
    private List<Shift> shiftList = new ArrayList<>();
    private ChipGroup chipGroupDates;
    private TextView tvSummaryRole, tvSummaryPoints, tvSummaryDate, tvSummaryShift;
    private String selectedDate;
    private Shift selectedShift;
    private String campaignId;
    private String campaignName;
    private MaterialButton btnConfirm;
    private EditText etNote;

    private ImageButton btnBack;
    private TextView tvRoleName, tvCampaignName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voluteer_role_registration);

        initViews();
        getDataFromIntent();
        displayData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvRoleName = findViewById(R.id.tvRoleName);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        chipGroupDates = findViewById(R.id.chipGroupDates);
        btnConfirm = findViewById(R.id.btnConfirm);
        etNote = findViewById(R.id.etNote);
        btnConfirm.setOnClickListener(v -> registerVolunteer());

        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        tvSummaryRole = findViewById(R.id.tvSummaryRole);
        tvSummaryPoints = findViewById(R.id.tvSummaryPoints);
        tvSummaryDate = findViewById(R.id.tvSummaryDate);
        tvSummaryShift = findViewById(R.id.tvSummaryShift);
    }

    private void registerVolunteer() {
        if (selectedDate == null) {
            Toast.makeText(this, "Vui lòng chọn ngày tham gia", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedShift == null) {
            Toast.makeText(this, "Vui lòng chọn ca làm việc", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang kiểm tra...");

        // Kiểm tra trùng lặp đăng ký (vai trò + ca + ngày) trước khi đăng ký
        checkDuplicateRegistration(currentUser.getUid());
    }

    // Kiểm tra TNV đã đăng ký với cùng vai trò + ca + ngày chưa
    private void checkDuplicateRegistration(String userId) {
        String shiftId = selectedShift.getId();

        db.collection("volunteer_registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("campaignId", campaignId)
                .whereEqualTo("shiftId", shiftId)
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean hasDuplicate = false;
                    String existingRoleName = "";
                    String existingShiftName = "";
                    String existingDate = "";

                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String status = doc.getString("status");
                        // Chỉ block nếu status KHÔNG phải cancelled hoặc rejected
                        if (status != null && !status.equals("cancelled") && !status.equals("rejected")) {
                            hasDuplicate = true;
                            existingRoleName = doc.getString("roleName");
                            existingShiftName = doc.getString("shiftName");
                            existingDate = doc.getString("date");
                            break;
                        }
                    }

                    if (hasDuplicate) {
                        resetConfirmButton();
                        showDuplicateRegistrationDialog(existingRoleName, existingShiftName, existingDate);
                    } else {
                        // Kiểm tra số lần hủy trước khi cho đăng ký
                        checkCancelCount(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    checkCampaignSlots();
                });
    }

    // Thêm method kiểm tra số lần hủy
    private void checkCancelCount(String userId) {
        db.collection("volunteer_registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("campaignId", campaignId)
                .whereEqualTo("status", "cancelled")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int cancelCount = querySnapshot.size();
                    if (cancelCount >= 2) {
                        resetConfirmButton();
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Không thể đăng ký")
                                .setMessage("Bạn đã hủy tham gia chiến dịch này 2 lần.\nKhông thể đăng ký lại.")
                                .setPositiveButton("Đã hiểu", null)
                                .show();
                    } else {
                        checkCampaignSlots();
                    }
                })
                .addOnFailureListener(e -> checkCampaignSlots());
    }


    // Hiển thị dialog khi đăng ký trùng
    private void showDuplicateRegistrationDialog(String existingRoleName, String existingShiftName, String existingDate) {
        String message = "Bạn đã đăng ký ca này rồi!\n\n" +
                "• Vai trò đã đăng ký: " + existingRoleName + "\n" +
                "• Ca làm: " + existingShiftName + "\n" +
                "• Ngày: " + existingDate + "\n\n" +
                "Mỗi ca làm trong ngày chỉ được đăng ký 1 vai trò.\nVui lòng chọn ca hoặc ngày khác.";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Trùng ca làm việc")
                .setMessage(message)
                .setPositiveButton("Chọn lại", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Quay lại", (dialog, which) -> finish())
                .setCancelable(true)
                .show();
    }

    // THÊM METHOD MỚI: Kiểm tra slot còn trống không
    private void checkCampaignSlots() {
        if (campaignId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin chiến dịch", Toast.LENGTH_SHORT).show();
            resetConfirmButton();
            return;
        }

        android.util.Log.d("SLOT_CHECK", "Checking slots for campaign: " + campaignId);

        db.collection("campaigns")
                .document(campaignId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Long currentVolunteers = doc.getLong("currentVolunteers");
                        Long maxVolunteers = doc.getLong("maxVolunteers");

                        int current = currentVolunteers != null ? currentVolunteers.intValue() : 0;
                        int max = maxVolunteers != null ? maxVolunteers.intValue() : 0;

                        android.util.Log.d("SLOT_CHECK", "Current: " + current + ", Max: " + max);

                        if (current >= max && max > 0) {
                            // HẾT SLOT
                            android.util.Log.d("SLOT_CHECK", "✗ Campaign is full");

                            resetConfirmButton();
                            showFullCampaignDialog(current, max);
                        } else {
                            // CÒN SLOT → Tiếp tục đăng ký
                            android.util.Log.d("SLOT_CHECK", "✓ Slots available, proceeding with registration");

                            btnConfirm.setText("Đang xử lý...");
                            proceedWithRegistration();
                        }
                    } else {
                        android.util.Log.e("SLOT_CHECK", "Campaign not found");
                        Toast.makeText(this, "Không tìm thấy thông tin chiến dịch", Toast.LENGTH_SHORT).show();
                        resetConfirmButton();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("SLOT_CHECK", "Error checking slots: " + e.getMessage());
                    Toast.makeText(this, "Lỗi kiểm tra thông tin chiến dịch", Toast.LENGTH_SHORT).show();
                    resetConfirmButton();
                });
    }

    // THÊM METHOD: Hiển thị dialog khi hết slot
    private void showFullCampaignDialog(int current, int max) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chiến dịch đã đầy")
                .setMessage("Rất tiếc! Chiến dịch này đã đủ số lượng tình nguyện viên.\n\n" +
                        "Số lượng hiện tại: " + current + "/" + max + " người\n\n" +
                        "Bạn có thể tham gia các chiến dịch khác hoặc theo dõi để đăng ký khi có slot trống.")
                .setPositiveButton("Đã hiểu", (dialog, which) -> {
                    dialog.dismiss();
                    finish(); // Quay lại màn hình trước
                })
                .setNegativeButton("Xem chiến dịch khác", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    // THÊM METHOD: Tiếp tục với đăng ký (code cũ)
    private void proceedWithRegistration() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String oderId = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String note = etNote.getText().toString().trim();
        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "";

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = "";
                    if (documentSnapshot.exists()) {
                        userName = documentSnapshot.getString("fullName");
                    }
                    if (userName == null || userName.isEmpty()) {
                        userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "";
                    }
                    saveRegistrationWithOrgId(oderId, note, userId, userName, userEmail);
                })
                .addOnFailureListener(e -> {
                    String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "";
                    saveRegistrationWithOrgId(oderId, note, userId, userName, userEmail);
                });
    }


    // === HÀM MỚI: Lấy organizationId (ưu tiên từ campaign object, fallback query Firestore) ===
    private void saveRegistrationWithOrgId(String oderId, String note, String userId, String userName, String userEmail) {
        android.util.Log.d("REG_DEBUG", "=== SAVING REGISTRATION ===");

        // Ưu tiên 1: Lấy từ campaign object
        if (campaign != null) {
            android.util.Log.d("REG_DEBUG", "Campaign object exists");
            android.util.Log.d("REG_DEBUG", "Campaign.organizationId: " + campaign.getOrganizationId());
            android.util.Log.d("REG_DEBUG", "Campaign.organizationName: " + campaign.getOrganizationName());

            if (campaign.getOrganizationId() != null && !campaign.getOrganizationId().isEmpty()) {
                saveRegistration(oderId, note, userId, userName, userEmail, campaign.getOrganizationId());
                return;
            }
        } else {
            android.util.Log.e("REG_DEBUG", "Campaign object is NULL!");
        }

        // Ưu tiên 2: Query từ Firestore
        if (campaignId == null || campaignId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin chiến dịch", Toast.LENGTH_SHORT).show();
            resetConfirmButton();
            android.util.Log.e("REG_DEBUG", "campaignId is null or empty!");
            return;
        }

        android.util.Log.d("REG_DEBUG", "Querying Firestore for campaignId: " + campaignId);

        db.collection("campaigns").document(campaignId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String orgId = "";
                    if (documentSnapshot.exists()) {
                        android.util.Log.d("REG_DEBUG", "Campaign document found");
                        android.util.Log.d("REG_DEBUG", "Document data: " + documentSnapshot.getData());

                        orgId = documentSnapshot.getString("organizationId");
                        android.util.Log.d("REG_DEBUG", "Extracted organizationId: " + orgId);

                        if (orgId == null) {
                            orgId = "";
                            android.util.Log.e("REG_DEBUG", "organizationId field is NULL in campaign!");
                        }
                    } else {
                        android.util.Log.e("REG_DEBUG", "Campaign document not found!");
                    }
                    saveRegistration(oderId, note, userId, userName, userEmail, orgId);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("REG_DEBUG", "Query failed: " + e.getMessage());
                    e.printStackTrace();
                    saveRegistration(oderId, note, userId, userName, userEmail, "");
                });
    }

    // === HÀM CŨ ĐÃ SỬA THAM SỐ: thêm organizationId ===
    private void saveRegistration(String oderId, String note, String userId, String userName, String userEmail, String organizationId) {
        Map<String, Object> registration = new HashMap<>();
        registration.put("oderId", oderId);
        registration.put("campaignId", campaignId);
        registration.put("campaignName", campaignName);
        registration.put("organizationId", organizationId); // <<< Đảm bảo luôn có giá trị đúng
        registration.put("roleId", role != null ? role.getId() : "");
        registration.put("roleName", role != null ? role.getRoleName() : "Tình nguyện viên");
        registration.put("userId", userId);
        registration.put("userName", userName);
        registration.put("userEmail", userEmail);
        registration.put("date", selectedDate);
        registration.put("shiftId", selectedShift.getId());
        registration.put("shiftName", selectedShift.getShiftName());
        registration.put("shiftTime", selectedShift.getTimeRange());
        registration.put("note", note);
        registration.put("status", "pending");
        registration.put("rejectionReason", "");
        registration.put("qrCode", "");
        registration.put("createdAt", System.currentTimeMillis());

        db.collection("volunteer_registrations")
                .add(registration)
                .addOnSuccessListener(documentReference -> {
                    // THÊM: Gửi thông báo cho tổ chức
                    sendRegistrationNotificationToOrg(organizationId);
                    showSuccessDialog(false, oderId);
                })
                .addOnFailureListener(e -> {
                    resetConfirmButton();
                    Toast.makeText(this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetConfirmButton() {
        btnConfirm.setEnabled(true);
        btnConfirm.setText("Xác nhận đăng kí");
    }

    private void getDataFromIntent() {
        role = (CampaignRole) getIntent().getSerializableExtra("role");
        campaignId = getIntent().getStringExtra("campaignId");
        campaignName = getIntent().getStringExtra("campaignName");
        campaign = (Campaign) getIntent().getSerializableExtra("campaign");

        android.util.Log.d("GetData", "role: " + (role != null ? role.getRoleName() : "null"));
        android.util.Log.d("GetData", "campaignId: " + campaignId);
        android.util.Log.d("GetData", "campaignName: " + campaignName);
        android.util.Log.d("GetData", "campaign: " + (campaign != null ? campaign.getName() : "null"));

        if (campaignId == null && campaign != null) {
            campaignId = campaign.getId();
        }

        if (campaignName == null && campaign != null) {
            campaignName = campaign.getName();
        }
    }

    // === Các hàm còn lại giữ nguyên (showSuccessDialog, generateQrCode, displayData, loadDates, loadShifts, displayShifts, updateSummary) ===

    private void showSuccessDialog(boolean isApproved, String qrContent) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_registration_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        ImageView ivSuccessIcon = dialog.findViewById(R.id.ivSuccessIcon);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvRoleInfo = dialog.findViewById(R.id.tvRoleInfo);
        TextView tvDateTime = dialog.findViewById(R.id.tvDateTime);
        CardView cardQRCode = dialog.findViewById(R.id.cardQRCode);
        ImageView ivQRCode = dialog.findViewById(R.id.ivQRCode);
        TextView tvNote = dialog.findViewById(R.id.tvNote);
        MaterialButton btnComplete = dialog.findViewById(R.id.btnComplete);
        ImageButton btnBack = dialog.findViewById(R.id.btnBack);

        tvRoleInfo.setText("Bạn đã đăng kí với vai trò: " + (role != null ? role.getRoleName() : "Tình nguyện viên"));
        tvDateTime.setText("Ngày: " + selectedDate + " · " + selectedShift.getTimeRange());

        if (isApproved) {
            tvTitle.setText("Đăng ký thành công");
            cardQRCode.setVisibility(View.VISIBLE);
            tvNote.setText("Lưu ý: Vui lòng chụp màn hình lại mã QR này hoặc lưu vào ảnh. Bạn sẽ cần xuất trình mã khi điểm danh.");
            Bitmap qrBitmap = generateQrCode(qrContent);
            if (qrBitmap != null) {
                ivQRCode.setImageBitmap(qrBitmap);
            }
        } else {
            tvTitle.setText("Đăng ký thành công!");
            cardQRCode.setVisibility(View.GONE);
            tvNote.setText("Đơn đăng ký của bạn đang chờ tổ chức thiện nguyện duyệt.\nBạn sẽ nhận được thông báo khi có kết quả.");
        }

        btnBack.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        btnComplete.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private Bitmap generateQrCode(String content) {
        try {
            com.google.zxing.qrcode.QRCodeWriter writer = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(content, com.google.zxing.BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void displayData() {
        if (role != null) {
            tvRoleName.setText("Đăng kí: " + role.getRoleName());
        } else {
            tvRoleName.setText("Đăng ký tham gia chiến dịch");
        }

        if (campaignName != null) {
            tvCampaignName.setText(campaignName.toUpperCase());
        }

        loadDates();
        loadShifts();
        updateSummary();
    }

    private void loadDates() {
        if (campaign == null || campaign.getStartDate() == null || campaign.getEndDate() == null) {
            return;
        }

        chipGroupDates.removeAllViews();
        chipGroupDates.setSingleSelection(true);
        chipGroupDates.setSelectionRequired(true);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat fullSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(campaign.getStartDate());

        Date endDate = campaign.getEndDate();

        boolean isFirst = true;
        while (!calendar.getTime().after(endDate)) {
            Chip chip = new Chip(this);
            Date currentDate = calendar.getTime();
            chip.setText(sdf.format(currentDate));
            chip.setTag(fullSdf.format(currentDate));
            chip.setCheckable(true);
            chip.setClickable(true);

            chip.setChipBackgroundColorResource(R.color.gray_light);
            chip.setTextColor(getResources().getColor(R.color.text_primary));

            if (isFirst) {
                chip.setChecked(true);
                chip.setChipBackgroundColorResource(R.color.primary_orange);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                selectedDate = fullSdf.format(currentDate);
                isFirst = false;
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chip.setChipBackgroundColorResource(R.color.primary_orange);
                    chip.setTextColor(getResources().getColor(android.R.color.white));
                    selectedDate = (String) chip.getTag();

                    // THÊM: Reset shift selection khi chọn ngày mới
                    selectedShift = null;
                    resetAllShifts();

                    updateSummary();
                } else {
                    chip.setChipBackgroundColorResource(R.color.gray_light);
                    chip.setTextColor(getResources().getColor(R.color.text_primary));
                }
            });

            chipGroupDates.addView(chip);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }


    private void loadShifts() {
        if (campaignId == null) {
            android.util.Log.e("LoadShifts", "campaignId is null!");
            return;
        }

        db.collection("shifts")
                .whereEqualTo("campaignId", campaignId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    shiftList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Shift shift = doc.toObject(Shift.class);
                        shift.setId(doc.getId());
                        shiftList.add(shift);
                    }
                    displayShifts();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("LoadShifts", "Error loading shifts: " + e.getMessage());
                });
    }
    private void displayShifts() {
        LinearLayout containerShifts = findViewById(R.id.containerShifts);
        containerShifts.removeAllViews();

        if (shiftList.isEmpty()) {
            TextView noShiftText = new TextView(this);
            noShiftText.setText("Chưa có ca làm việc nào");
            noShiftText.setPadding(16, 16, 16, 16);
            noShiftText.setTextSize(16);
            noShiftText.setGravity(android.view.Gravity.CENTER);
            noShiftText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            containerShifts.addView(noShiftText);
            return;
        }

        for (int i = 0; i < shiftList.size(); i++) {
            Shift shift = shiftList.get(i);

            // Sử dụng layout item_shift hiện có
            View shiftView = getLayoutInflater().inflate(R.layout.item_shift, containerShifts, false);

            // Tìm các view
            CardView cardShift = (CardView) shiftView;
            ImageView ivIcon = shiftView.findViewById(R.id.ivIcon);
            TextView tvTime = shiftView.findViewById(R.id.tvTime);
            TextView tvSlots = shiftView.findViewById(R.id.tvSlots);

            // Set dữ liệu
            tvTime.setText(shift.getTimeRange());

            // Tính số chỗ trống
            int currentVolunteers = shift.getCurrentVolunteers();
            int maxVolunteers = shift.getMaxVolunteers();
            int availableSlots = maxVolunteers - currentVolunteers;

            if (availableSlots <= 0) {
                tvSlots.setText("Đã đủ người");
                tvSlots.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                cardShift.setEnabled(false);
                cardShift.setAlpha(0.5f);
                if (ivIcon != null) {
                    ivIcon.setColorFilter(getResources().getColor(android.R.color.darker_gray));
                }
            } else {
                tvSlots.setText("Còn " + availableSlots + "/" + maxVolunteers + " chỗ");
                tvSlots.setTextColor(getResources().getColor(R.color.text_secondary));
                cardShift.setEnabled(true);
                cardShift.setAlpha(1.0f);
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_journey);
                    ivIcon.setColorFilter(getResources().getColor(R.color.text_secondary));
                }
            }

            // Set tag để lưu shift object
            shiftView.setTag(shift);

            // Xử lý click
            if (availableSlots > 0) {
                cardShift.setOnClickListener(v -> selectShift(shiftView, shift));
            }

            // Thêm vào container
            containerShifts.addView(shiftView);
        }
    }



    private void selectShift(View clickedView, Shift shift) {
        // Reset tất cả các shift về trạng thái không được chọn
        LinearLayout containerShifts = findViewById(R.id.containerShifts);
        for (int i = 0; i < containerShifts.getChildCount(); i++) {
            View childView = containerShifts.getChildAt(i);
            CardView cardShift = (CardView) childView;
            ImageView ivIcon = childView.findViewById(R.id.ivIcon);

            // Reset về trạng thái không được chọn
            cardShift.setCardBackgroundColor(getResources().getColor(R.color.gray_light));
            if (ivIcon != null) {
                ivIcon.setColorFilter(getResources().getColor(R.color.text_secondary));
            }
        }

        // Set trạng thái được chọn cho item được click
        CardView clickedCard = (CardView) clickedView;
        ImageView clickedIcon = clickedView.findViewById(R.id.ivIcon);

        clickedCard.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        // Tạo hiệu ứng viền bằng cách thay đổi elevation
        clickedCard.setCardElevation(8f);

        if (clickedIcon != null) {
            clickedIcon.setColorFilter(getResources().getColor(R.color.primary_orange));
        }

        // Lưu shift được chọn
        selectedShift = shift;

        // Cập nhật summary
        updateSummary();

        android.util.Log.d("ShiftSelection", "Selected shift: " + shift.getShiftName() + " (" + shift.getTimeRange() + ")");
    }


    private void updateSummary() {
        if (role != null) {
            tvSummaryRole.setText(role.getRoleName());
            tvSummaryPoints.setText(role.getPointsReward() + " điểm");
        } else {
            tvSummaryRole.setText("Tình nguyện viên");
            tvSummaryPoints.setText(campaign != null ? campaign.getPointsReward() + " điểm" : "0 điểm");
        }

        if (selectedDate != null) {
            tvSummaryDate.setText(selectedDate);
        } else {
            tvSummaryDate.setText("-");
        }

        if (selectedShift != null) {
            tvSummaryShift.setText(selectedShift.getShiftName() + " (" + selectedShift.getTimeRange() + ")");
        } else {
            tvSummaryShift.setText("-");
        }
    }

    private void sendRegistrationNotificationToOrg(String orgId) {
        NotificationRepository notificationRepo = new NotificationRepository();
        notificationRepo.notifyNGONewRegistrationWithUserLookup(
                orgId,
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                campaignName,
                role != null ? role.getRoleName() : "Tình nguyện viên",
                selectedShift.getShiftName(),
                selectedDate
        );
    }
    private void resetAllShifts() {
        LinearLayout containerShifts = findViewById(R.id.containerShifts);
        if (containerShifts == null) return;

        for (int i = 0; i < containerShifts.getChildCount(); i++) {
            View childView = containerShifts.getChildAt(i);
            CardView cardShift = (CardView) childView;
            ImageView ivIcon = childView.findViewById(R.id.ivIcon);

            // Reset về trạng thái không được chọn
            cardShift.setCardBackgroundColor(getResources().getColor(R.color.gray_light));
            cardShift.setCardElevation(0f); // Reset elevation

            if (ivIcon != null) {
                ivIcon.setColorFilter(getResources().getColor(R.color.text_secondary));
            }
        }
    }

}