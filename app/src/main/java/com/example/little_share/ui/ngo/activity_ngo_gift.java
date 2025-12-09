package com.example.little_share.ui.ngo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Gift;
import com.example.little_share.data.repositories.GiftRepository;
import com.example.little_share.ui.ngo.adapter.GiftAdapter;

import java.util.ArrayList;

public class activity_ngo_gift extends AppCompatActivity {

    private Button btnAddGift;
    private RecyclerView rvGiftList;
    private TextView tvTotalGifts, tvAvailableGifts, tvRedeemedGifts;

    private GiftRepository giftRepository;
    private GiftAdapter adapter;

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

        giftRepository = new GiftRepository();

        btnAddGift.setOnClickListener(v -> showAddGiftDialog());
    }

    private void setupRecyclerView() {
        adapter = new GiftAdapter(this, new ArrayList<>(), gift -> {
            // Xác nhận xóa
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
                loadStats(); // Cập nhật stats khi có thay đổi
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

    private void showAddGiftDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_ngo_add_gift);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Views
        EditText edtGiftName = dialog.findViewById(R.id.edtGiftName);
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        EditText edtPoints = dialog.findViewById(R.id.edtPoints);
        EditText edtQuantity = dialog.findViewById(R.id.edtQuantity);
        EditText edtLocation = dialog.findViewById(R.id.edtLocation);
        EditText edtImageLink = dialog.findViewById(R.id.edtImageLink);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnAdd = dialog.findViewById(R.id.btnAdd);

        // Setup spinner
        String[] categories = {"Đồ lưu niệm", "Quà gia dụng", "Văn phòng phẩm", "Khác"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String name = edtGiftName.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            String pointsStr = edtPoints.getText().toString().trim();
            String quantityStr = edtQuantity.getText().toString().trim();
            String location = edtLocation.getText().toString().trim();
            String imageUrl = edtImageLink.getText().toString().trim();

            // Validate
            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên quà", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pointsStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập điểm đổi", Toast.LENGTH_SHORT).show();
                return;
            }
            if (quantityStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                return;
            }

            int points = Integer.parseInt(pointsStr);
            int quantity = Integer.parseInt(quantityStr);

            // Create gift
            Gift gift = new Gift();
            gift.setName(name);
            gift.setCategory(category);
            gift.setPointsRequired(points);
            gift.setTotalQuantity(quantity);
            gift.setPickupLocation(location);
            gift.setImageUrl(imageUrl);
            gift.setDescription("");

            // Save to Firestore
            btnAdd.setEnabled(false);
            btnAdd.setText("Đang thêm...");

            giftRepository.createGift(gift, new GiftRepository.OnGiftListener() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(activity_ngo_gift.this, message, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(activity_ngo_gift.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    btnAdd.setEnabled(true);
                    btnAdd.setText("Thêm quà");
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