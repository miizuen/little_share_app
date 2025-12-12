package com.example.little_share.ui.ngo;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Gift;
import com.example.little_share.data.repositories.GiftRepository;
import com.example.little_share.helper.ImgBBUploader;
import com.example.little_share.ui.ngo.adapter.GiftAdapter;

import java.util.ArrayList;

public class activity_ngo_gift extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private Button btnAddGift;
    private RecyclerView rvGiftList;
    private TextView tvTotalGifts, tvAvailableGifts, tvRedeemedGifts;
    private TextView btnQRGift;
    private GiftRepository giftRepository;
    private GiftAdapter adapter;

    private Uri selectedImageUri = null;
    private ImageView currentPreviewImageView = null;

    // Launcher chọn ảnh
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (currentPreviewImageView != null) {
                        Glide.with(activity_ngo_gift.this)
                                .load(uri)
                                .circleCrop()
                                .into(currentPreviewImageView);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_gift);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        loadGifts();
        loadStats();
    }

    private void initViews() {
        btnAddGift = findViewById(R.id.button);
        rvGiftList = findViewById(R.id.rvGiftList);
        tvTotalGifts = findViewById(R.id.tvTotalMoney);
        tvAvailableGifts = findViewById(R.id.tvTotalCampaigns);
        tvRedeemedGifts = findViewById(R.id.tvdatrao);
        btnQRGift = findViewById(R.id.btnQRGift); // Nút quét QR

        giftRepository = new GiftRepository();

        btnAddGift.setOnClickListener(v -> showAddGiftDialog());

        // Xử lý sự kiện khi bấm nút Quét QR
        btnQRGift.setOnClickListener(v -> checkCameraPermissionAndScan());
    }

    private void setupRecyclerView() {
        adapter = new GiftAdapter(this, new ArrayList<>(), gift -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa quà tặng")
                    .setMessage("Bạn có chắc muốn xóa \"" + gift.getName() + "\"?")
                    .setPositiveButton("Xóa", (dialog, which) -> deleteGift(gift))
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        rvGiftList.setLayoutManager(new LinearLayoutManager(this));
        rvGiftList.setAdapter(adapter);
    }

    private void loadGifts() {
        giftRepository.getGiftsByOrganization().observe(this, gifts -> {
            if (gifts != null) {
                adapter.updateData(gifts);
                loadStats();
            }
        });
    }

    private void loadStats() {
        giftRepository.getGiftStats(new GiftRepository.OnStatsListener() {
            @Override
            public void onSuccess(int totalGifts, int availableGifts, int redeemedGifts) {
                tvTotalGifts.setText(String.valueOf(totalGifts));
                tvAvailableGifts.setText(String.valueOf(availableGifts));
                tvRedeemedGifts.setText(String.valueOf(redeemedGifts));
            }
        });
    }

    // ==================== QR SCANNER ====================

    /**
     * Kiểm tra quyền camera trước khi mở QR scanner
     */
    private void checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Chưa có quyền, yêu cầu quyền
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            // Đã có quyền, mở QR scanner
            openQRScanner();
        }
    }

    /**
     * Xử lý kết quả yêu cầu quyền
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền được cấp, mở QR scanner
                openQRScanner();
            } else {
                // Quyền bị từ chối
                Toast.makeText(this, "Cần cấp quyền camera để quét mã QR", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Mở dialog QR scanner
     */
    private void openQRScanner() {
        QRScannerDialog qrDialog = new QRScannerDialog(this, new QRScannerDialog.OnQRScannedListener() {
            @Override
            public void onQRScanned(String code) {
                // Xử lý mã QR đã quét được
                handleScannedCode(code);
            }

            @Override
            public void onManualCodeEntered(String code) {
                // Xử lý mã được nhập thủ công
                handleScannedCode(code);
            }
        });

        qrDialog.show();
    }

    /**
     * Xử lý mã QR sau khi quét hoặc nhập thủ công
     */
    private void handleScannedCode(String code) {
        Toast.makeText(this, "Đã quét mã: " + code, Toast.LENGTH_SHORT).show();

        // TODO: Xử lý logic đổi quà theo mã QR
        // Ví dụ: Kiểm tra mã có hợp lệ không, cập nhật trạng thái đơn đổi quà, v.v.

        // Tạm thời hiển thị dialog xác nhận
        showGiftRedemptionConfirmation(code);
    }

    /**
     * Hiển thị dialog xác nhận đổi quà
     */
    private void showGiftRedemptionConfirmation(String code) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đổi quà")
                .setMessage("Mã: " + code + "\n\nBạn có muốn xác nhận đơn đổi quà này?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // TODO: Gọi API xác nhận đổi quà
                    Toast.makeText(this, "Đã xác nhận đơn đổi quà", Toast.LENGTH_SHORT).show();
                    loadGifts(); // Refresh lại danh sách
                    loadStats(); // Cập nhật thống kê
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ==================== END QR SCANNER ====================

    private void showAddGiftDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_ngo_add_gift);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        // Views
        EditText edtGiftName = dialog.findViewById(R.id.edtGiftName);
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        EditText edtPoints = dialog.findViewById(R.id.edtPoints);
        EditText edtQuantity = dialog.findViewById(R.id.edtQuantity);
        EditText edtLocation = dialog.findViewById(R.id.edtLocation);
        ImageView imgGiftPreview = dialog.findViewById(R.id.imgGiftPreview);
        View btnChangePhoto = dialog.findViewById(R.id.btnChangePhoto);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnAdd = dialog.findViewById(R.id.btnAdd);

        // Reset trạng thái mỗi lần mở
        selectedImageUri = null;
        currentPreviewImageView = imgGiftPreview;
        imgGiftPreview.setImageResource(R.drawable.ic_gift_3d);

        // Spinner danh mục
        String[] categories = {"Đồ lưu niệm", "Quà gia dụng", "Văn phòng phẩm", "Thực phẩm", "Khác"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        // Mở thư viện ảnh khi bấm vào preview hoặc nút đổi ảnh
        View.OnClickListener openGallery = v -> galleryLauncher.launch("image/*");
        imgGiftPreview.setOnClickListener(openGallery);
        if (btnChangePhoto != null) {
            btnChangePhoto.setOnClickListener(openGallery);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String name = edtGiftName.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            String pointsStr = edtPoints.getText().toString().trim();
            String quantityStr = edtQuantity.getText().toString().trim();
            String location = edtLocation.getText().toString().trim();

            if (name.isEmpty() || pointsStr.isEmpty() || quantityStr.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh quà tặng", Toast.LENGTH_SHORT).show();
                return;
            }

            int points = Integer.parseInt(pointsStr);
            int quantity = Integer.parseInt(quantityStr);

            btnAdd.setEnabled(false);
            btnAdd.setText("Đang tải ảnh...");

            ImgBBUploader.uploadImage(this, selectedImageUri, new ImgBBUploader.UploadListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    Gift gift = new Gift();
                    gift.setName(name);
                    gift.setCategory(category);
                    gift.setPointsRequired(points);
                    gift.setTotalQuantity(quantity);
                    gift.setPickupLocation(location);
                    gift.setImageUrl(imageUrl);
                    gift.setDescription("");

                    giftRepository.createGift(gift, new GiftRepository.OnGiftListener() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(activity_ngo_gift.this, "Thêm quà thành công!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(activity_ngo_gift.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            btnAdd.setEnabled(true);
                            btnAdd.setText("Thêm quà");
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(activity_ngo_gift.this, "Upload ảnh thất bại: " + error, Toast.LENGTH_SHORT).show();
                    btnAdd.setEnabled(true);
                    btnAdd.setText("Thêm quà");
                }

                @Override
                public void onProgress(int progress) {
                    btnAdd.setText("Đang tải " + progress + "%");
                }
            });
        });

        dialog.show();
    }

    private void deleteGift(Gift gift) {
        giftRepository.deleteGift(gift.getId(), new GiftRepository.OnGiftListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(activity_ngo_gift.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(activity_ngo_gift.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}