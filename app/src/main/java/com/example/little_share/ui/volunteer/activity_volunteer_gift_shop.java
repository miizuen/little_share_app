package com.example.little_share.ui.volunteer;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.little_share.R;
import com.example.little_share.data.models.Gift;
import com.example.little_share.data.models.User;
import com.example.little_share.data.repositories.GiftRepository;
import com.example.little_share.data.repositories.UserRepository;
import com.example.little_share.ui.volunteer.adapter.GiftAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class activity_volunteer_gift_shop extends AppCompatActivity {
    private static final String TAG = "GiftShop";
    private RecyclerView recyclerGifts;
    private GiftAdapter giftAdapter;
    private List<Gift> giftList;
    private ImageButton btnBack;
    private TextView tvPoints;
    private FirebaseFirestore db;
    private UserRepository userRepository;
    private User currentUser;

    private GiftRepository giftRepository; // Thêm biến này

    private void initRepositories() {
        db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository();
        giftRepository = new GiftRepository(); // Thêm dòng này
    }

    private void loadGiftsFromFirestore() {
        Log.d(TAG, "Starting to load gifts using GiftRepository...");

        giftRepository.getAllGifts().observe(this, gifts -> {
            Log.d(TAG, "Observer triggered with " + (gifts != null ? gifts.size() : "null") + " gifts");

            // KIỂM TRA ADAPTER TRƯỚC
            if (giftAdapter == null) {
                Log.e(TAG, "GiftAdapter is null! Setting up RecyclerView first.");
                setupRecyclerView();
            }

            if (gifts != null && !gifts.isEmpty()) {
                Log.d(TAG, "Received " + gifts.size() + " gifts from repository");
                giftAdapter.updateGiftList(gifts);
                Log.d(TAG, "Adapter updated successfully");
            } else {
                Log.d(TAG, "No gifts found, creating sample gifts");
                createSampleGifts();
            }
        });
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_gift_shop);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initRepositories();
        setupRecyclerView(); // ĐẢM BẢO ADAPTER ĐƯỢC TẠO TRƯỚC
        setupClickListeners();

        // Load data SAU KHI setup UI
        loadCurrentUserData();
        loadGiftsFromFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data khi quay lại activity
        loadCurrentUserData();

        // THÊM: Refresh gifts data (nếu không dùng real-time listener)
        // loadGiftsFromFirestore(); // Không cần nếu đã dùng addSnapshotListener
    }


    private void initViews() {
        recyclerGifts = findViewById(R.id.recyclerGifts);
        btnBack = findViewById(R.id.btnBack);
        tvPoints = findViewById(R.id.tvPoints);
    }



    private void setupRecyclerView() {
        giftList = new ArrayList<>();
        giftAdapter = new GiftAdapter(this, giftList);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerGifts.setLayoutManager(gridLayoutManager);
        recyclerGifts.setAdapter(giftAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadCurrentUserData() {
        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                // Hiển thị điểm từ model User
                int userPoints = user.getTotalPoints();
                tvPoints.setText(String.valueOf(userPoints));
                Log.d(TAG, "User points loaded: " + userPoints);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading user data: " + error);
                Toast.makeText(activity_volunteer_gift_shop.this,
                        "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                tvPoints.setText("0");
            }
        });
    }

    private void createSampleGifts() {
        Log.d(TAG, "Creating sample gifts...");

        giftList.clear();

        Gift gift1 = new Gift("abc", "Đồ chơi", 400, 15);
        gift1.setDescription("Gấu bông mềm mại, dễ thương cho trẻ em");
        gift1.setImageUrl("");
        gift1.setAvailableQuantity(8);

        Gift gift2 = new Gift("Movie Ticket", "Giải trí", 400, 10);
        gift2.setDescription("Vé xem phim tại rạp CGV");
        gift2.setImageUrl("");
        gift2.setAvailableQuantity(5);

        Gift gift3 = new Gift("Gau bong teddy", "Đồ chơi", 800, 25);
        gift3.setDescription("Gấu bông teddy cao cấp");
        gift3.setImageUrl("");
        gift3.setAvailableQuantity(14);

        giftList.add(gift1);
        giftList.add(gift2);
        giftList.add(gift3);

        // SỬA: Sử dụng updateGiftList thay vì notifyDataSetChanged
        if (giftAdapter != null) {
            giftAdapter.updateGiftList(giftList);
            Log.d(TAG, "Sample gifts updated to adapter: " + giftList.size());
        } else {
            Log.e(TAG, "GiftAdapter is null when creating sample gifts!");
        }
    }


    // Phương thức public để lấy user hiện tại (dùng cho adapter hoặc activity khác)
    public User getCurrentUser() {
        return currentUser;
    }

    // Phương thức public để lấy điểm user hiện tại
    public int getCurrentUserPoints() {
        return currentUser != null ? currentUser.getTotalPoints() : 0;
    }
}
