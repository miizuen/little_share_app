package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.little_share.R;
import com.example.little_share.data.models.Donation;
import com.example.little_share.data.models.User;
import com.example.little_share.data.repositories.DonationRepository;
import com.example.little_share.data.repositories.UserRepository;
import com.example.little_share.ui.volunteer.adapter.DonationPagerAdapter; // THÊM DÒNG NÀY
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class frm_volunteer_donation extends Fragment {

    private static final String TAG = "frm_volunteer_donation";
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DonationPagerAdapter pagerAdapter;

    // Repositories
    private DonationRepository donationRepository;
    private UserRepository userRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_volunteer_donation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initRepositories();
        initViews(view);
        setupTabsAndViewPager();
    }

    // THÊM METHOD NÀY
    private void initRepositories() {
        donationRepository = new DonationRepository();
        userRepository = new UserRepository();
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
    }

    private void setupTabsAndViewPager() {
        pagerAdapter = new DonationPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Bạn muốn quyên góp gì?");
                    break;
                case 1:
                    tab.setText("Chiến dịch quyên góp");
                    break;
            }
        }).attach();
    }

    private void loadUserData() {
        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return;

                Log.d(TAG, "User data loaded: " + user.getFullName());
                Log.d(TAG, "Total points: " + user.getTotalPoints());
                Log.d(TAG, "Total donations: " + user.getTotalDonations());
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
        donationRepository.getVolunteerDonations(new DonationRepository.OnDonationListListener() {
            @Override
            public void onSuccess(List<Donation> donations) {
                if (!isAdded()) return;

                Log.d(TAG, "Loaded " + donations.size() + " donations");

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
        loadUserData();
        loadDonationHistory();
    }
}
