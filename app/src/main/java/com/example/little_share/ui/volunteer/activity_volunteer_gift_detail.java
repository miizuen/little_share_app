package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Gift;
import com.example.little_share.data.models.User;
import com.example.little_share.data.repositories.GiftRepository;
import com.example.little_share.data.repositories.UserRepository;

public class activity_volunteer_gift_detail extends AppCompatActivity {
    private static final String TAG = "GiftDetail";

    private ImageView ivGiftImage;
    private TextView tvGiftName, tvRequiredPoints, tvRemainingStock, tvUserPoints, tvStatus, tvLocationAddress;
    private ImageButton btnBack;
    private Button btnExchange;
    private Gift currentGift;
    private User currentUser;
    private UserRepository userRepository;
    private GiftRepository giftRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_gift_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initRepositories();
        getGiftDataFromIntent();
        loadCurrentUserData();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data khi quay lại activity
        loadCurrentUserData();
    }

    private void initViews() {
        ivGiftImage = findViewById(R.id.ivGiftImage);
        tvGiftName = findViewById(R.id.tvGiftName);
        tvRequiredPoints = findViewById(R.id.tvRequiredPoints);
        tvRemainingStock = findViewById(R.id.tvRemainingStock);
        tvUserPoints = findViewById(R.id.tvUserPoints);
        tvStatus = findViewById(R.id.tvStatus);
        tvLocationAddress = findViewById(R.id.tvLocationAddress);
        btnBack = findViewById(R.id.btnBack);
        btnExchange = findViewById(R.id.btnExchange);
    }

    private void initRepositories() {
        userRepository = new UserRepository();
        giftRepository = new GiftRepository();
    }

    private void loadCurrentUserData() {
        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                updateUserPointsDisplay();
                updateExchangeButton();
                Log.d(TAG, "User data loaded: " + user.getTotalPoints() + " points");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading user data: " + error);
                Toast.makeText(activity_volunteer_gift_detail.this,
                        "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserPointsDisplay() {
        if (currentUser != null) {
            int userPoints = currentUser.getTotalPoints();
            tvUserPoints.setText(String.valueOf(userPoints));

            if (currentGift != null) {
                // Kiểm tra đủ điểm hay không
                if (userPoints >= currentGift.getPointsRequired()) {
                    tvStatus.setText("Đủ điểm");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    tvStatus.setText("Không đủ điểm");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }
        }
    }

    private void getGiftDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("gift")) {
            currentGift = (Gift) intent.getSerializableExtra("gift");
            if (currentGift != null) {
                displayGiftData();
            }
        }
    }

    private void displayGiftData() {
        // Hiển thị tên quà
        tvGiftName.setText(currentGift.getName());

        // Hiển thị điểm cần thiết
        tvRequiredPoints.setText(String.valueOf(currentGift.getPointsRequired()));

        // Hiển thị số lượng còn lại
        tvRemainingStock.setText(currentGift.getAvailabilityText());

        // Hiển thị địa điểm nhận quà
        if (currentGift.getPickupLocation() != null && !currentGift.getPickupLocation().isEmpty()) {
            tvLocationAddress.setText(currentGift.getPickupLocation());
        } else {
            tvLocationAddress.setText("Văn phòng Tổ chức Ánh Sáng - 123 Trần Hưng Đạo,\nThành phố Đà Nẵng");
        }

        // Load hình ảnh
        if (currentGift.getImageUrl() != null && !currentGift.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentGift.getImageUrl())
                    .placeholder(R.drawable.gift_teddy_bear)
                    .error(R.drawable.gift_teddy_bear)
                    .into(ivGiftImage);
        } else {
            ivGiftImage.setImageResource(R.drawable.gift_teddy_bear);
        }

        // Cập nhật trạng thái nút đổi quà
        updateExchangeButton();
    }

    private void updateExchangeButton() {
        if (currentGift != null && currentUser != null) {
            int userPoints = currentUser.getTotalPoints();

            if (!currentGift.isAvailable()) {
                btnExchange.setText("HẾT HÀNG");
                btnExchange.setEnabled(false);
                btnExchange.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            } else if (userPoints < currentGift.getPointsRequired()) {
                btnExchange.setText("KHÔNG ĐỦ ĐIỂM");
                btnExchange.setEnabled(false);
                btnExchange.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            } else {
                btnExchange.setText("ĐỔI QUÀ NGAY");
                btnExchange.setEnabled(true);
                // Giữ nguyên background gradient
            }
        }
    }

    private void setupClickListeners() {
        // Nút back
        btnBack.setOnClickListener(v -> finish());

        // Nút đổi quà
        btnExchange.setOnClickListener(v -> {
            if (currentGift != null && currentUser != null) {
                int userPoints = currentUser.getTotalPoints();
                if (currentGift.isAvailable() && userPoints >= currentGift.getPointsRequired()) {
                    handleExchangeGift();
                }
            }
        });
    }

    private void handleExchangeGift() {
        if (currentGift == null || currentUser == null) return;

        // Hiển thị loading
        btnExchange.setEnabled(false);
        btnExchange.setText("ĐANG XỬ LÝ...");

        // Gọi GiftRepository để thực hiện đổi quà
        giftRepository.redeemGift(
                currentUser.getId(),
                currentUser.getFullName(), // userName
                currentGift.getId(),
                currentGift.getName(),
                currentGift.getPointsRequired(), // pointsSpent
                new GiftRepository.OnRedemptionListener() {
                    @Override
                    public void onSuccess(String redemptionId, String qrCode) {
                        // Cập nhật điểm user local
                        int newPoints = currentUser.getTotalPoints() - currentGift.getPointsRequired();
                        currentUser.setTotalPoints(newPoints);

                        // Cập nhật số lượng quà local
                        currentGift.setAvailableQuantity(currentGift.getAvailableQuantity() - 1);

                        // Hiển thị dialog thành công
                        showSuccessDialog(newPoints, qrCode);

                        // Reset button
                        btnExchange.setEnabled(true);
                        updateExchangeButton();
                        updateUserPointsDisplay();
                        displayGiftData(); // Refresh gift data

                        Log.d(TAG, "Gift redemption successful. Remaining points: " + newPoints);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(activity_volunteer_gift_detail.this,
                                "Lỗi: " + error, Toast.LENGTH_LONG).show();

                        // Reset button
                        btnExchange.setEnabled(true);
                        btnExchange.setText("ĐỔI QUÀ NGAY");

                        Log.e(TAG, "Gift redemption failed: " + error);
                    }
                }
        );
    }

    private void showSuccessDialog(int remainingPoints, String qrCode) {
        GiftRedemptionSuccessDialog dialog = new GiftRedemptionSuccessDialog(
                this,
                currentGift.getName(),
                remainingPoints,
                qrCode
        );

        dialog.setOnDialogActionListener(() -> {
            // Quay về gift shop và refresh dữ liệu
            finish();
        });

        dialog.show();
    }
}
