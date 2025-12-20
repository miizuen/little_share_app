package com.example.little_share.ui.ngo;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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
import com.example.little_share.data.models.Donation;
import com.example.little_share.data.repositories.DonationRepository;
import com.example.little_share.ui.ngo.adapter.DonationConfirmedAdapter;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class activity_ngo_donation extends AppCompatActivity implements DonationConfirmedAdapter.OnDonationActionListener {

    private RecyclerView rvDonationsConfirmed, rvDonationsPending;
    private DonationConfirmedAdapter confirmedAdapter, pendingAdapter;
    private DonationRepository donationRepository;
    private List<Donation> confirmedDonations, pendingDonations;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_donation);

        initViews();
        initRepositories();
        setupRecyclerViews();
        loadDonations();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        rvDonationsConfirmed = findViewById(R.id.rvDonationsConfirmed);
        rvDonationsPending = findViewById(R.id.rvDonationsPending);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void initRepositories() {
        donationRepository = new DonationRepository();
    }

    private void setupRecyclerViews() {
        // Setup confirmed donations RecyclerView
        confirmedDonations = new ArrayList<>();
        confirmedAdapter = new DonationConfirmedAdapter(this, confirmedDonations);
        confirmedAdapter.setOnDonationActionListener(this);
        rvDonationsConfirmed.setLayoutManager(new LinearLayoutManager(this));
        rvDonationsConfirmed.setAdapter(confirmedAdapter);

        // Setup pending donations RecyclerView
        pendingDonations = new ArrayList<>();
        pendingAdapter = new DonationConfirmedAdapter(this, pendingDonations);
        pendingAdapter.setOnDonationActionListener(this);
        rvDonationsPending.setLayoutManager(new LinearLayoutManager(this));
        rvDonationsPending.setAdapter(pendingAdapter);
    }

    private void loadDonations() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d("NgoDonation", "=== LOADING DONATIONS ===");
        Log.d("NgoDonation", "Current User ID: " + currentUserId);

        donationRepository.getDonationsForOrganization(currentUserId, new DonationRepository.OnDonationListListener() {
            @Override
            public void onSuccess(List<Donation> donations) {
                runOnUiThread(() -> {
                    Log.d("NgoDonation", "=== DONATIONS LOADED ===");
                    Log.d("NgoDonation", "Total donations: " + donations.size());

                    for (int i = 0; i < donations.size(); i++) {
                        Donation d = donations.get(i);
                        Log.d("NgoDonation", "Donation " + i + ":");
                        Log.d("NgoDonation", "  - ID: " + d.getId());
                        Log.d("NgoDonation", "  - User: " + d.getUserName());
                        Log.d("NgoDonation", "  - Type: " + d.getType());
                        Log.d("NgoDonation", "  - Status: " + d.getStatus());
                        Log.d("NgoDonation", "  - Points: " + d.getPointsEarned());
                    }

                    separateAndDisplayDonations(donations);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Log.e("NgoDonation", "Error loading donations: " + error);
                    Toast.makeText(activity_ngo_donation.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void separateAndDisplayDonations(List<Donation> allDonations) {
        confirmedDonations.clear();
        pendingDonations.clear();

        for (Donation donation : allDonations) {
            Donation.DonationStatus status = donation.getStatusEnum();
            if (status == Donation.DonationStatus.PENDING) {
                pendingDonations.add(donation);
            } else {
                confirmedDonations.add(donation);
            }
        }

        confirmedAdapter.updateDonations(confirmedDonations);
        pendingAdapter.updateDonations(pendingDonations);
    }

    @Override
    public void onConfirm(Donation donation) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận quyên góp")
                .setMessage("Bạn có chắc chắn muốn xác nhận quyên góp từ " + donation.getUserName() + "?\nĐiểm thưởng: " + donation.getPointsEarned())
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Sử dụng method confirmDonation có sẵn
                    donationRepository.confirmDonation(donation.getId(), donation.getPointsEarned(),
                            new DonationRepository.OnDonationListener() {
                                @Override
                                public void onSuccess(String donationId) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(activity_ngo_donation.this,
                                                "Xác nhận thành công và đã cộng " + donation.getPointsEarned() + " điểm",
                                                Toast.LENGTH_LONG).show();
                                        loadDonations(); // Reload data
                                    });
                                }

                                @Override
                                public void onFailure(String error) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(activity_ngo_donation.this, error, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                })
                .setNegativeButton("Từ chối", (dialog, which) -> {
                    onReject(donation);
                })
                .setNeutralButton("Hủy", null)
                .show();
    }

    @Override
    public void onReject(Donation donation) {
        new AlertDialog.Builder(this)
                .setTitle("Từ chối quyên góp")
                .setMessage("Bạn có chắc chắn muốn từ chối quyên góp từ " + donation.getUserName() + "?")
                .setPositiveButton("Từ chối", (dialog, which) -> {
                    // Sử dụng method updateDonationStatus có sẵn
                    donationRepository.updateDonationStatus(donation.getId(), Donation.DonationStatus.REJECTED,
                            new DonationRepository.OnDonationListener() {
                                @Override
                                public void onSuccess(String donationId) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(activity_ngo_donation.this, "Đã từ chối quyên góp", Toast.LENGTH_SHORT).show();
                                        loadDonations(); // Reload data
                                    });
                                }

                                @Override
                                public void onFailure(String error) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(activity_ngo_donation.this, error, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onMarkReceived(Donation donation) {
        // Đối với donations đã CONFIRMED, chỉ cần update status thành RECEIVED
        // Points đã được cộng rồi khi confirm
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận nhận đồ")
                .setMessage("Xác nhận đã nhận đồ từ " + donation.getUserName() + "?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    donationRepository.updateDonationStatus(donation.getId(), Donation.DonationStatus.RECEIVED,
                            new DonationRepository.OnDonationListener() {
                                @Override
                                public void onSuccess(String donationId) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(activity_ngo_donation.this, "Đã xác nhận nhận đồ", Toast.LENGTH_SHORT).show();
                                        loadDonations(); // Reload data
                                    });
                                }

                                @Override
                                public void onFailure(String error) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(activity_ngo_donation.this, error, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
