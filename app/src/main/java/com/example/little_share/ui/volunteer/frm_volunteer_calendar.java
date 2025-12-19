package com.example.little_share.ui.volunteer;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.VolunteerRegistration;
import com.example.little_share.ui.volunteer.adapter.MyRegistrationAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.List;

public class frm_volunteer_calendar extends Fragment implements MyRegistrationAdapter.OnViewQRListener {

    private RecyclerView rvHistory;
    private MyRegistrationAdapter adapter;
    private ImageButton btnBack;
    private TextView tvCampaignName;
    private List<VolunteerRegistration> registrationList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_volunteer_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupRecyclerView();
        loadMyRegistrations();
        setupClickListeners();
    }

    private void initViews(View view) {
        rvHistory = view.findViewById(R.id.rv_history);
        btnBack = view.findViewById(R.id.btnBack);
        tvCampaignName = view.findViewById(R.id.tvCampaignName);
    }

    private void setupRecyclerView() {
        adapter = new MyRegistrationAdapter(getContext(), registrationList, this);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(adapter);
    }

    private void loadMyRegistrations() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        if (userId.isEmpty()) {
            tvCampaignName.setText("Vui lòng đăng nhập");
            return;
        }

        db.collection("volunteer_registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "approved")  // THÊM DÒNG NÀY
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    registrationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        VolunteerRegistration reg = doc.toObject(VolunteerRegistration.class);
                        reg.setId(doc.getId());
                        registrationList.add(reg);
                    }
                    adapter.notifyDataSetChanged();

                    // Cập nhật subtitle
                    if (registrationList.isEmpty()) {
                        tvCampaignName.setText("Chưa có hoạt động nào");
                    } else {
                        tvCampaignName.setText("Bạn có " + registrationList.size() + " hoạt động");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onViewQR(VolunteerRegistration registration) {
        showQRDialog(registration);
    }

    private void showQRDialog(VolunteerRegistration registration) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_registration_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Bind views
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvRoleInfo = dialog.findViewById(R.id.tvRoleInfo);
        TextView tvDateTime = dialog.findViewById(R.id.tvDateTime);
        ImageView ivQRCode = dialog.findViewById(R.id.ivQRCode);
        MaterialButton btnComplete = dialog.findViewById(R.id.btnComplete);
        ImageButton btnBackDialog = dialog.findViewById(R.id.btnBack);

        // Set data
        tvTitle.setText("Mã điểm danh");
        tvRoleInfo.setText("Vai trò: " + registration.getRoleName());
        tvDateTime.setText("Ngày: " + registration.getDate() + " · " + registration.getShiftTime());

        // Sinh QR Code từ mã đã lưu
        String qrContent = registration.getQrCode();
        if (qrContent != null && !qrContent.isEmpty()) {
            Bitmap qrBitmap = generateQrCode(qrContent);
            if (qrBitmap != null) {
                ivQRCode.setImageBitmap(qrBitmap);
            }
        } else {
            Toast.makeText(getContext(), "Chưa có mã QR", Toast.LENGTH_SHORT).show();
        }

        btnComplete.setOnClickListener(v -> dialog.dismiss());
        if (btnBackDialog != null) {
            btnBackDialog.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private Bitmap generateQrCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
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

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }
}
