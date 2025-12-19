package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.little_share.R;
import com.example.little_share.data.models.Donation;
import com.example.little_share.data.models.User;
import com.example.little_share.data.repositories.DonationRepository;
import com.example.little_share.data.repositories.UserRepository;

import java.util.List;

public class frm_volunteer_donation extends Fragment {

    private static final String TAG = "frm_volunteer_donation";

    // Views for donation types
    private LinearLayout btnBook, btnMoney, btnToy, btnShirt;

    // Repositories
    private DonationRepository donationRepository;
    private UserRepository userRepository;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initRepositories();
        setupDonationButtons();

        // Load user data để hiển thị thống kê (nếu cần)
        loadUserData();
    }

    private void initViews(View view) {
        // Donation type buttons
        btnBook = view.findViewById(R.id.btnBook);
        btnShirt = view.findViewById(R.id.btnShirt);
        btnToy = view.findViewById(R.id.btnToy);
        btnMoney = view.findViewById(R.id.btnMoney);
    }

    private void initRepositories() {
        donationRepository = new DonationRepository();
        userRepository = new UserRepository();
    }

    private void setupDonationButtons() {
        btnBook.setOnClickListener(v -> openDonationForm("BOOK"));
        btnShirt.setOnClickListener(v -> openDonationForm("SHIRT"));
        btnToy.setOnClickListener(v -> openDonationForm("TOY"));
        btnMoney.setOnClickListener(v -> openDonationForm("MONEY"));
    }

    private void loadUserData() {
        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return;

                Log.d(TAG, "User data loaded: " + user.getFullName());
                Log.d(TAG, "Total points: " + user.getTotalPoints());
                Log.d(TAG, "Total donations: " + user.getTotalDonations());

                // Có thể hiển thị toast chào mừng
                Toast.makeText(getContext(),
                        "Xin chào " + user.getFullName() + "! Bạn có " + user.getTotalPoints() + " điểm",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                if (!isAdded()) return;

                Log.e(TAG, "Error loading user data: " + error);
                Toast.makeText(getContext(), "Lỗi tải thông tin: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDonationHistory() {
        // Method để load lịch sử quyên góp (có thể dùng sau)
        donationRepository.getVolunteerDonations(new DonationRepository.OnDonationListListener() {
            @Override
            public void onSuccess(List<Donation> donations) {
                if (!isAdded()) return;

                Log.d(TAG, "Loaded " + donations.size() + " donations");

                // Hiển thị thống kê đơn giản
                if (!donations.isEmpty()) {
                    Toast.makeText(getContext(),
                            "Bạn đã quyên góp " + donations.size() + " lần",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (!isAdded()) return;

                Log.e(TAG, "Error loading donations: " + error);
            }
        });
    }

    private void openDonationForm(String type) {
        Intent intent = new Intent(getActivity(), activity_volunteer_donation_form.class);
        intent.putExtra("DONATION_TYPE", type);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning from donation form
        loadUserData();
        loadDonationHistory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frm_volunteer_donation, container, false);
    }
}
