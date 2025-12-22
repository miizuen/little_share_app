package com.example.little_share.ui.ngo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.data.repositories.OrganizationRepository;
import com.example.little_share.ui.ngo.dialog.QRScannerDialog;
import com.example.little_share.utils.QRCodeGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class frm_ngo_home extends Fragment {

    private static final String TAG = "frm_ngo_home";

    ImageView btnCreateCmp, btnReport, btnAttendance, btnReward, btnDonation;
    private ImageView ivOrgLogo;
    private TextView tvOrganizationName, tvTotalVolunteers, tvTotalCampaigns, tvTotalSponsors, tvTotalPoints;
    private OrganizationRepository organizationRepository;
    private CampaignRepository campaignRepository;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_ngo_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupQuickActions();
        loadOrganizationData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (organizationRepository != null) {
            loadOrganizationData();
        }
    }

    private void initViews(View view) {
        Log.d(TAG, "Initializing views...");

        btnCreateCmp = view.findViewById(R.id.btnCreateCmp);
        btnReport = view.findViewById(R.id.btnReports);
        btnAttendance = view.findViewById(R.id.btnAttendance);
        btnReward = view.findViewById(R.id.btnReward);
        btnDonation = view.findViewById(R.id.btnDonation);

        ivOrgLogo = view.findViewById(R.id.ivOrgLogo);
        tvOrganizationName = view.findViewById(R.id.tvOrganizationName);
        tvTotalVolunteers = view.findViewById(R.id.tvTotalVolunteers);
        tvTotalCampaigns = view.findViewById(R.id.tvTotalCampaigns);
        tvTotalSponsors = view.findViewById(R.id.tvTotalSponsors);
        tvTotalPoints = view.findViewById(R.id.tvTotalPoints);

        organizationRepository = new OrganizationRepository();
        campaignRepository = new CampaignRepository();
        db = FirebaseFirestore.getInstance();
    }

    private void setupQuickActions() {
        if (btnCreateCmp != null) {
            btnCreateCmp.setOnClickListener(v -> startActivity(new Intent(getActivity(), activity_ngo_create_campagin.class)));
        }
        if (btnAttendance != null) {
            btnAttendance.setOnClickListener(v -> {
                // Kiểm tra quyền camera trước khi mở QR Scanner
                if (checkCameraPermission()) {
                    showQRScanner();
                } else {
                    // Nếu chưa có quyền, chuyển đến activity để xin quyền
                    startActivity(new Intent(getActivity(), activity_ngo_attendance.class));
                }
            });
        }
        if (btnReport != null) {
            btnReport.setOnClickListener(v -> startActivity(new Intent(getActivity(), activity_ngo_finance_report.class)));
        }
        if (btnReward != null) {
            btnReward.setOnClickListener(v -> startActivity(new Intent(getActivity(), activity_ngo_gift.class)));
        }
        if (btnDonation != null) {
            btnDonation.setOnClickListener(v -> startActivity(new Intent(getActivity(), activity_ngo_donation.class)));
        }
    }

    private void loadOrganizationData() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Loading data for userId: " + currentUserId);

        organizationRepository.getCurrentOrganization(new OrganizationRepository.OnOrganizationListener() {
            @Override
            public void onSuccess(com.example.little_share.data.models.Organization organization) {
                if (organization == null) {
                    Log.e(TAG, "Organization is NULL!");
                    createDefaultOrganization(currentUserId);
                    return;
                }

                if (!isAdded()) {
                    Log.w(TAG, "Fragment not added, skipping UI update");
                    return;
                }

                Log.d(TAG, "Organization loaded successfully: " + organization.getName());

                if (tvOrganizationName != null) {
                    tvOrganizationName.setText(organization.getName());
                    Log.d(TAG, "Organization name set: " + organization.getName());
                }

                if (ivOrgLogo != null) {
                    if (organization.getLogo() != null && !organization.getLogo().isEmpty()) {
                        Glide.with(frm_ngo_home.this)
                                .load(organization.getLogo())
                                .placeholder(R.drawable.logo_thelight)
                                .error(R.drawable.logo_thelight)
                                .circleCrop()
                                .into(ivOrgLogo);
                    } else {
                        ivOrgLogo.setImageResource(R.drawable.logo_thelight);
                    }
                }

                loadRealTimeStats(organization.getId());
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading organization: " + error);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                    createDefaultOrganization(currentUserId);
                }
            }
        });
    }

    private void createDefaultOrganization(String userId) {
        Log.d(TAG, "Creating default organization for new NGO account");

        organizationRepository.createOrganization(
                userId,
                "Light of Heart",
                "contact@lightofheart.org",
                "0123456789",
                "123 Charity Street, Ho Chi Minh City",
                new OrganizationRepository.OnCreateOrgListener() {
                    @Override
                    public void onSuccess(String organizationId) {
                        Log.d(TAG, "Default organization created successfully");
                        updateUserOrganizationId(userId, organizationId);
                        if (getView() != null) {
                            getView().postDelayed(() -> loadOrganizationData(), 1000);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Failed to create default organization: " + error);
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Lỗi tạo tổ chức mặc định: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    private void updateUserOrganizationId(String userId, String organizationId) {
        db.collection("users")
                .document(userId)
                .update("organizationId", organizationId)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User organizationId updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update user organizationId: " + e.getMessage()));
    }

    private void loadRealTimeStats(String organizationId) {
        Log.d(TAG, "=== LOADING STATS FOR ORGANIZATION: " + organizationId + " ===");

        setInitialStats();

        loadTotalCampaigns(organizationId);
        loadTotalVolunteers(organizationId);
        loadTotalSponsors(organizationId);
        loadTotalPointsGiven(organizationId);
    }

    private void setInitialStats() {
        if (isAdded()) {
            if (tvTotalVolunteers != null) tvTotalVolunteers.setText("0");
            if (tvTotalCampaigns != null) tvTotalCampaigns.setText("0");
            if (tvTotalSponsors != null) tvTotalSponsors.setText("0");
            if (tvTotalPoints != null) tvTotalPoints.setText("0");
            Log.d(TAG, "Initial stats set to 0");
        }
    }

    // ✅ Số chiến dịch
    private void loadTotalCampaigns(String organizationId) {
        db.collection("campaigns")
                .whereEqualTo("organizationId", organizationId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading campaigns: " + error.getMessage());
                        return;
                    }

                    int totalCampaigns = snapshots != null ? snapshots.size() : 0;
                    Log.d(TAG, "Real-time campaigns count: " + totalCampaigns);

                    if (isAdded() && tvTotalCampaigns != null) {
                        tvTotalCampaigns.setText(String.valueOf(totalCampaigns));
                        Log.d(TAG, "✅ Campaign count updated: " + totalCampaigns);
                    }
                });
    }

    // ✅ Số tình nguyện viên
    private void loadTotalVolunteers(String organizationId) {
        Log.d(TAG, "Loading volunteers for organizationId: " + organizationId);

        db.collection("volunteer_registrations")
                .whereEqualTo("organizationId", organizationId)
                .whereEqualTo("status", "approved")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading approved volunteers: " + error.getMessage());
                        return;
                    }

                    java.util.Set<String> uniqueVolunteers = new java.util.HashSet<>();
                    if (snapshots != null) {
                        Log.d(TAG, "Found " + snapshots.size() + " approved registrations");
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String userId = doc.getString("userId");
                            if (userId != null) {
                                uniqueVolunteers.add(userId);
                            }
                        }
                    }

                    int totalVolunteers = uniqueVolunteers.size();
                    Log.d(TAG, "Real-time approved volunteers count: " + totalVolunteers);

                    if (isAdded() && tvTotalVolunteers != null) {
                        tvTotalVolunteers.setText(String.valueOf(totalVolunteers));
                        Log.d(TAG, "✅ Volunteer count updated: " + totalVolunteers);
                    }
                });
    }

    // ✅ Số nhà tài trợ
    private void loadTotalSponsors(String organizationId) {
        Log.d(TAG, "Loading sponsors for organizationId: " + organizationId);

        db.collection("campaigns")
                .whereEqualTo("organizationId", organizationId)
                .get()
                .addOnSuccessListener(campaignSnapshots -> {
                    java.util.List<String> campaignIds = new java.util.ArrayList<>();

                    if (campaignSnapshots != null) {
                        for (QueryDocumentSnapshot doc : campaignSnapshots) {
                            campaignIds.add(doc.getId());
                        }
                    }

                    Log.d(TAG, "Found " + campaignIds.size() + " campaigns for organization");

                    if (campaignIds.isEmpty()) {
                        if (isAdded() && tvTotalSponsors != null) {
                            tvTotalSponsors.setText("0");
                        }
                        return;
                    }

                    loadSponsorsFromCampaigns(campaignIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading campaigns for sponsor count: " + e.getMessage());
                    if (isAdded() && tvTotalSponsors != null) {
                        tvTotalSponsors.setText("0");
                    }
                });
    }

    private void loadSponsorsFromCampaigns(java.util.List<String> campaignIds) {
        db.collection("sponsorDonations")
                .whereEqualTo("status", "COMPLETED")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading sponsor donations: " + error.getMessage());
                        if (isAdded() && tvTotalSponsors != null) {
                            tvTotalSponsors.setText("0");
                        }
                        return;
                    }

                    java.util.Set<String> uniqueSponsors = new java.util.HashSet<>();
                    if (snapshots != null) {
                        Log.d(TAG, "Found " + snapshots.size() + " completed sponsor donations");

                        for (QueryDocumentSnapshot doc : snapshots) {
                            String campaignId = doc.getString("campaignId");
                            String sponsorId = doc.getString("sponsorId");

                            if (campaignId != null && sponsorId != null && campaignIds.contains(campaignId)) {
                                uniqueSponsors.add(sponsorId);
                                Log.d(TAG, "Added sponsor: " + sponsorId);
                            }
                        }
                    }

                    int totalSponsors = uniqueSponsors.size();
                    Log.d(TAG, "Total unique sponsors: " + totalSponsors);

                    if (isAdded() && tvTotalSponsors != null) {
                        tvTotalSponsors.setText(String.valueOf(totalSponsors));
                        Log.d(TAG, "✅ Sponsor count updated: " + totalSponsors);
                    }
                });
    }

    // ✅ Số điểm đã tặng - Tính ước tính dựa trên volunteers và campaigns
    private void loadTotalPointsGiven(String organizationId) {
        Log.d(TAG, "Loading total points given for organizationId: " + organizationId);
        calculatePointsFromExistingData(organizationId);
    }

    private void calculatePointsFromExistingData(String organizationId) {
        final int[] volunteerPoints = {0};
        final int[] campaignPoints = {0};
        final boolean[] volunteerLoaded = {false};
        final boolean[] campaignLoaded = {false};

        // 1. Tính điểm từ số tình nguyện viên
        db.collection("volunteer_registrations")
                .whereEqualTo("organizationId", organizationId)
                .whereEqualTo("status", "approved")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading volunteers for points: " + error.getMessage());
                        volunteerLoaded[0] = true;
                        updateEstimatedPoints(volunteerPoints[0], campaignPoints[0], volunteerLoaded[0], campaignLoaded[0]);
                        return;
                    }

                    java.util.Set<String> uniqueVolunteers = new java.util.HashSet<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String userId = doc.getString("userId");
                            if (userId != null) {
                                uniqueVolunteers.add(userId);
                            }
                        }
                    }

                    // Mỗi volunteer được tặng trung bình 150 điểm
                    volunteerPoints[0] = uniqueVolunteers.size() * 150;
                    Log.d(TAG, "Estimated points from " + uniqueVolunteers.size() + " volunteers: " + volunteerPoints[0]);

                    volunteerLoaded[0] = true;
                    updateEstimatedPoints(volunteerPoints[0], campaignPoints[0], volunteerLoaded[0], campaignLoaded[0]);
                });

        // 2. Tính điểm từ số chiến dịch
        db.collection("campaigns")
                .whereEqualTo("organizationId", organizationId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading campaigns for points: " + error.getMessage());
                        campaignLoaded[0] = true;
                        updateEstimatedPoints(volunteerPoints[0], campaignPoints[0], volunteerLoaded[0], campaignLoaded[0]);
                        return;
                    }

                    int totalCampaigns = snapshots != null ? snapshots.size() : 0;

                    // Mỗi chiến dịch tặng trung bình 75 điểm
                    campaignPoints[0] = totalCampaigns * 75;
                    Log.d(TAG, "Estimated points from " + totalCampaigns + " campaigns: " + campaignPoints[0]);

                    campaignLoaded[0] = true;
                    updateEstimatedPoints(volunteerPoints[0], campaignPoints[0], volunteerLoaded[0], campaignLoaded[0]);
                });
    }

    private void updateEstimatedPoints(int volunteerPoints, int campaignPoints, boolean volunteerLoaded, boolean campaignLoaded) {
        if (volunteerLoaded && campaignLoaded) {
            int totalEstimatedPoints = volunteerPoints + campaignPoints;

            Log.d(TAG, "=== ESTIMATED POINTS CALCULATION ===");
            Log.d(TAG, "Points from volunteers: " + volunteerPoints);
            Log.d(TAG, "Points from campaigns: " + campaignPoints);
            Log.d(TAG, "TOTAL ESTIMATED POINTS: " + totalEstimatedPoints);

            if (isAdded() && tvTotalPoints != null) {
                tvTotalPoints.setText(String.format("%,d", totalEstimatedPoints));
                Log.d(TAG, "✅ Estimated points display updated: " + totalEstimatedPoints);
            }
        }
    }

    // ===== PHƯƠNG THỨC MỚI CHO ĐIỂM DANH TRỰC TIẾP =====
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void showQRScanner() {
        QRScannerDialog qrDialog = new QRScannerDialog(getActivity(), new QRScannerDialog.OnQRScannedListener() {
            @Override
            public void onQRScanned(String code) {
                handleAttendanceCode(code, true);
            }

            @Override
            public void onManualCodeEntered(String code) {
                handleAttendanceCode(code, false);
            }

            @Override
            public void onGiftRedemptionScanned(String redemptionId, String userId, String giftId) {
                Toast.makeText(getContext(),
                        "QR code này dành cho đổi quà, không phải điểm danh", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCampaignRegistrationScanned(String registrationId, String userId, String campaignId) {
                showAttendanceConfirmation(registrationId, userId, campaignId, true);
            }

            @Override
            public void onVolunteerScanned(String volunteerId) {
                Toast.makeText(getContext(),
                        "QR code volunteer không được hỗ trợ ở đây", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInvalidQRScanned(String error) {
                showErrorDialog("QR code không hợp lệ", error);
            }
        });

        qrDialog.show();
    }

    private void handleAttendanceCode(String code, boolean isScanned) {
        Log.d(TAG, "Handling attendance code: " + code + ", isScanned: " + isScanned);
        
        QRCodeGenerator.QRCodeData qrData = QRCodeGenerator.parseQRCode(code);
        
        if (qrData == null || !"campaign".equals(qrData.type)) {
            showErrorDialog("QR code không hợp lệ", "QR code này không phải cho điểm danh chiến dịch");
            return;
        }

        String registrationId = qrData.getRegistrationId();
        String userId = qrData.getUserId();
        String campaignId = qrData.getReferenceId();

        showAttendanceConfirmation(registrationId, userId, campaignId, isScanned);
    }

    private void showAttendanceConfirmation(String registrationId, String userId, String campaignId, boolean isScanned) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xác nhận điểm danh")
                .setMessage("Bạn có chắc chắn muốn xác nhận điểm danh cho tình nguyện viên này?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    confirmAttendance(registrationId, userId, campaignId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmAttendance(String registrationId, String userId, String campaignId) {
        campaignRepository.confirmAttendance(registrationId, userId, campaignId, 
            new CampaignRepository.OnAttendanceListener() {
                @Override
                public void onSuccess(String message) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Điểm danh thành công!", Toast.LENGTH_SHORT).show();
                        // Refresh stats sau khi điểm danh thành công
                        loadOrganizationData();
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (isAdded()) {
                        showErrorDialog("Lỗi điểm danh", error);
                    }
                }
            });
    }

    private void showErrorDialog(String title, String message) {
        if (isAdded()) {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}
