package com.example.little_share.ui.volunteer;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.View;
import android.widget.LinearLayout;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import android.graphics.Bitmap;

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
    private TextView tvConfirmRole, tvConfirmPoints;

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
        // Validate
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

        // Disable button
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang xử lý...");

        String oderId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String note = etNote.getText().toString().trim();
        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "";

        // Lấy fullName từ collection users
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

                    // Lưu đăng ký với userName đã lấy được
                    saveRegistration(oderId, note, userId, userName, userEmail);
                })
                .addOnFailureListener(e -> {
                    // Nếu lỗi, vẫn lưu với displayName
                    String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "";
                    saveRegistration(oderId, note, userId, userName, userEmail);
                });
    }

    private void saveRegistration(String oderId, String note, String userId, String userName, String userEmail) {
        Map<String, Object> registration = new HashMap<>();
        registration.put("oderId", oderId);
        registration.put("campaignId", campaignId);
        registration.put("campaignName", campaignName);
        registration.put("organizationId", campaign != null ? campaign.getOrganizationId() : "");
        registration.put("roleId", role != null ? role.getId() : "");
        registration.put("roleName", role != null ? role.getRoleName() : "");
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
                    showSuccessDialog(false, oderId);
                })
                .addOnFailureListener(e -> {
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("Xác nhận đăng kí");
                    Toast.makeText(this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getDataFromIntent() {
        role = (CampaignRole) getIntent().getSerializableExtra("role");
        campaignId = getIntent().getStringExtra("campaignId");
        campaignName = getIntent().getStringExtra("campaignName");
        campaign = (Campaign) getIntent().getSerializableExtra("campaign");
    }
    private void showSuccessDialog(boolean isApproved, String qrContent) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_registration_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        // Bind views
        ImageView ivSuccessIcon = dialog.findViewById(R.id.ivSuccessIcon);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvRoleInfo = dialog.findViewById(R.id.tvRoleInfo);
        TextView tvDateTime = dialog.findViewById(R.id.tvDateTime);
        CardView cardQRCode = dialog.findViewById(R.id.cardQRCode);
        ImageView ivQRCode = dialog.findViewById(R.id.ivQRCode);
        TextView tvNote = dialog.findViewById(R.id.tvNote);
        MaterialButton btnComplete = dialog.findViewById(R.id.btnComplete);
        ImageButton btnBack = dialog.findViewById(R.id.btnBack);

        // Set data chung
        tvRoleInfo.setText("Bạn đã đăng kí với vai trò: " + role.getRoleName());
        tvDateTime.setText("Ngày: " + selectedDate + " · " + selectedShift.getTimeRange());

        if (isApproved) {
            // Đã được duyệt - Hiển thị QR Code
            tvTitle.setText("Đăng ký thành công");
            cardQRCode.setVisibility(View.VISIBLE);
            tvNote.setText("Lưu ý: Vui lòng chụp màn hình lại mã QR này hoặc lưu vào ảnh. Bạn sẽ cần xuất trình mã khi điểm danh.");

            // Generate QR Code
            Bitmap qrBitmap = generateQrCode(qrContent);
            if (qrBitmap != null) {
                ivQRCode.setImageBitmap(qrBitmap);
            }
        } else {
            // Chờ duyệt - Ẩn QR Code
            tvTitle.setText("Đăng ký thành công!");
            cardQRCode.setVisibility(View.GONE);
            tvNote.setText("Đơn đăng ký của bạn đang chờ tổ chức thiện nguyện duyệt.\nBạn sẽ nhận được thông báo khi có kết quả.");
        }

        // Button events
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
            chip.setTag(fullSdf.format(currentDate)); // Lưu ngày đầy đủ vào tag
            chip.setCheckable(true);
            chip.setClickable(true);

            // Style cho chip chưa chọn
            chip.setChipBackgroundColorResource(R.color.gray_light);
            chip.setTextColor(getResources().getColor(R.color.text_primary));

            if (isFirst) {
                chip.setChecked(true);
                chip.setChipBackgroundColorResource(R.color.primary_orange);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                selectedDate = fullSdf.format(currentDate); // Lưu ngày được chọn
                isFirst = false;
            }

            // Xử lý sự kiện khi chọn chip
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Đổi màu chip được chọn
                    chip.setChipBackgroundColorResource(R.color.primary_orange);
                    chip.setTextColor(getResources().getColor(android.R.color.white));
                    selectedDate = (String) chip.getTag();
                    updateSummary(); // Cập nhật card xác nhận
                } else {
                    // Đổi màu chip không được chọn
                    chip.setChipBackgroundColorResource(R.color.gray_light);
                    chip.setTextColor(getResources().getColor(R.color.text_primary));
                }
            });

            chipGroupDates.addView(chip);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
    private void loadShifts() {
        if (campaignId == null) return;

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
                });
    }
    private void displayShifts() {
        RadioGroup radioGroupShifts = findViewById(R.id.radioGroupShifts);
        radioGroupShifts.removeAllViews();

        for (int i = 0; i < shiftList.size(); i++) {
            Shift shift = shiftList.get(i);

            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());
            radioButton.setText(shift.getShiftName() + " (" + shift.getTimeRange() + ") - Còn "
                    + (shift.getMaxVolunteers() - shift.getCurrentVolunteers()) + " chỗ");
            radioButton.setTag(shift);
            radioButton.setPadding(16, 32, 16, 32);
            radioButton.setTextSize(15);

            if (shift.isFull()) {
                radioButton.setEnabled(false);
                radioButton.setText(shift.getShiftName() + " (" + shift.getTimeRange() + ") - Đã đủ người");
            }

            radioGroupShifts.addView(radioButton);
        }

        // Xử lý khi chọn ca
        radioGroupShifts.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selected = findViewById(checkedId);
            if (selected != null) {
                selectedShift = (Shift) selected.getTag();
                updateSummary(); // Cập nhật card xác nhận
            }
        });
    }
    private void updateSummary() {
        if (role != null) {
            tvSummaryRole.setText(role.getRoleName());
            tvSummaryPoints.setText(role.getPointsReward() + " điểm");
        }

        if (selectedDate != null) {
            tvSummaryDate.setText(selectedDate);
        }

        if (selectedShift != null) {
            tvSummaryShift.setText(selectedShift.getShiftName() + " (" + selectedShift.getTimeRange() + ")");
        }
    }
}
