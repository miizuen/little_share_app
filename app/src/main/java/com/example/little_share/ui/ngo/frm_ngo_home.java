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

        cleanupTestData();
        deleteAllTestDataOnce();
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
                if (checkCameraPermission()) {
                    showQRScanner();
                } else {
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
        }
    }

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

    private void loadTotalPointsGiven(String organizationId) {
        Log.d(TAG, "=== LOADING TOTAL POINTS FROM DONATIONS AND VOLUNTEERS ===");

        // Listener cho tổng điểm từ cả 2 nguồn
        final int[] totalPointsArray = {0};
        final boolean[] donationLoaded = {false};
        final boolean[] volunteerLoaded = {false};

        // 1. Tính điểm từ DONATIONS (pointsEarned)
        db.collection("donations")
                .whereEqualTo("organizationId", organizationId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading donations: " + error.getMessage());
                        donationLoaded[0] = true;
                        updatePointsDisplay(totalPointsArray[0], donationLoaded[0], volunteerLoaded[0]);
                        return;
                    }

                    int donationPoints = 0;
                    if (snapshots != null) {
                        Log.d(TAG, "Found " + snapshots.size() + " donations for this org");

                        for (QueryDocumentSnapshot doc : snapshots) {
                            Integer points = doc.getLong("pointsEarned") != null ?
                                    doc.getLong("pointsEarned").intValue() : 0;
                            String status = doc.getString("status");

                            Log.d(TAG, "Donation " + doc.getId() + ": " + points + " points, status: " + status);

                            // Tính donations có status RECEIVED
                            if (status != null && status.equalsIgnoreCase("RECEIVED")) {
                                donationPoints += points;
                                Log.d(TAG, "✅ Added " + points + " points from donation " + doc.getId());
                            }
                        }
                    }

                    Log.d(TAG, "=== DONATION POINTS: " + donationPoints + " ===");
                    totalPointsArray[0] = donationPoints;
                    donationLoaded[0] = true;
                    updatePointsDisplay(totalPointsArray[0], donationLoaded[0], volunteerLoaded[0]);
                });

        // 2. Tính điểm từ VOLUNTEER REGISTRATIONS (pointsEarned)
        db.collection("volunteer_registrations")
                .whereEqualTo("organizationId", organizationId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading volunteer registrations: " + error.getMessage());
                        volunteerLoaded[0] = true;
                        updatePointsDisplay(totalPointsArray[0], donationLoaded[0], volunteerLoaded[0]);
                        return;
                    }

                    int volunteerPoints = 0;
                    if (snapshots != null) {
                        Log.d(TAG, "Found " + snapshots.size() + " volunteer registrations for this org");

                        for (QueryDocumentSnapshot doc : snapshots) {
                            Integer points = doc.getLong("pointsEarned") != null ?
                                    doc.getLong("pointsEarned").intValue() : 0;
                            String status = doc.getString("status");

                            Log.d(TAG, "Volunteer reg " + doc.getId() + ": " + points + " points, status: " + status);

                            // Tính registrations có status completed
                            if (status != null && status.equalsIgnoreCase("completed")) {
                                volunteerPoints += points;
                                Log.d(TAG, "✅ Added " + points + " points from volunteer " + doc.getId());
                            }
                        }
                    }

                    Log.d(TAG, "=== VOLUNTEER POINTS: " + volunteerPoints + " ===");
                    totalPointsArray[0] += volunteerPoints;
                    volunteerLoaded[0] = true;
                    updatePointsDisplay(totalPointsArray[0], donationLoaded[0], volunteerLoaded[0]);
                });
    }

    private void updatePointsDisplay(int totalPoints, boolean donationLoaded, boolean volunteerLoaded) {
        // Chỉ update UI khi cả 2 nguồn đã load xong
        if (donationLoaded && volunteerLoaded && isAdded() && tvTotalPoints != null) {
            Log.d(TAG, "=== TOTAL POINTS FROM ALL SOURCES: " + totalPoints + " ===");
            tvTotalPoints.setText(String.format("%,d", totalPoints));
            Log.d(TAG, "✅ Points display updated to: " + totalPoints);
        }
    }

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

    private void cleanupTestData() {
        Log.d(TAG, "Cleaning up test data...");

        db.collection("volunteer_registrations")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String userId = doc.getString("userId");
                            if (userId != null && userId.startsWith("test")) {
                                doc.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "✅ Deleted test registration: " + doc.getId());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "❌ Failed to delete test registration: " + e.getMessage());
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading registrations for cleanup: " + e.getMessage());
                });
    }

    private void deleteAllTestDataOnce() {
        Log.d(TAG, "=== DELETING ALL TEST DATA FROM FIREBASE ===");

        db.collection("volunteer_registrations")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null && !snapshots.isEmpty()) {
                        int totalDocs = snapshots.size();
                        int testDocs = 0;

                        for (QueryDocumentSnapshot doc : snapshots) {
                            String userId = doc.getString("userId");
                            String campaignId = doc.getString("campaignId");

                            if ((userId != null && (userId.contains("test") || userId.startsWith("test_"))) ||
                                    (campaignId != null && (campaignId.contains("test") || campaignId.startsWith("test_")))) {

                                testDocs++;
                                Log.d(TAG, "Deleting test doc: " + doc.getId() + " - userId: " + userId + " - campaignId: " + campaignId);

                                doc.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "✅ DELETED test document: " + doc.getId());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "❌ FAILED to delete test document: " + e.getMessage());
                                        });
                            }
                        }

                        Log.d(TAG, "=== CLEANUP SUMMARY ===");
                        Log.d(TAG, "Total documents: " + totalDocs);
                        Log.d(TAG, "Test documents deleted: " + testDocs);
                        Log.d(TAG, "Real documents remaining: " + (totalDocs - testDocs));
                    } else {
                        Log.d(TAG, "No documents found in volunteer_registrations");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error during cleanup: " + e.getMessage());
                });
    }
}
