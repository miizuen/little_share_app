package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.models.User;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.data.repositories.UserRepository;
import com.example.little_share.ui.volunteer.adapter.CampaignAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class frm_volunteer_home extends Fragment {
    LinearLayout btnDonation, btnSchedule, btnHistory, btnGifts;
    private TextView tvUserName, tvTotalPoints, tvTotalDonations, tvTotalCampaigns, tvCampaignCount;
    private ImageView ivUserAvatar;
    private Chip chipAll, chipEducation, chipFood, chipEnvironment, chipHealth, chipUrgent;
    private RecyclerView rvCampaigns;
    private UserRepository userRepository;
    private CampaignAdapter adapter;
    private CampaignRepository repository;
    private String selectedCategory = "ALL";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_volunteer_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        userRepository = new UserRepository();
        repository = new CampaignRepository();
        btnDonation = view.findViewById(R.id.btnDonation);
        btnSchedule = view.findViewById(R.id.btnSchedule);
        btnHistory = view.findViewById(R.id.btnHistory);
        btnGifts = view.findViewById(R.id.btnGifts);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvTotalPoints = view.findViewById(R.id.tvTotalPoints);
        tvTotalDonations = view.findViewById(R.id.tvTotalDonations);
        tvTotalCampaigns = view.findViewById(R.id.tvTotalCampaigns);
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar);
        tvCampaignCount = view.findViewById(R.id.tvCampaignCount);
        rvCampaigns = view.findViewById(R.id.rvCampaigns);
        chipAll = view.findViewById(R.id.chipAll);
        chipEducation = view.findViewById(R.id.chipEducation);
        chipFood = view.findViewById(R.id.chipFood);
        chipEnvironment = view.findViewById(R.id.chipEnvironment);
        chipHealth = view.findViewById(R.id.chipHealth);
        chipUrgent = view.findViewById(R.id.chipUrgent);

        loadCurrentUserData();

        setupQuickActions();

        setupRecyclerView();

        setupChips();

        loadCampaigns();
    }

    private void setupChips() {
        chipAll.setOnClickListener(v -> filterByCategory("ALL"));
        chipEducation.setOnClickListener(v -> filterByCategory("EDUCATION"));
        chipFood.setOnClickListener(v -> filterByCategory("FOOD"));
        chipEnvironment.setOnClickListener(v -> filterByCategory("ENVIRONMENT"));
        chipHealth.setOnClickListener(v -> filterByCategory("HEALTH"));
        chipUrgent.setOnClickListener(v -> filterByCategory("URGENT"));
    }

    private void updateCampaignCount(int count) {
        tvCampaignCount.setText(count + " chiến dịch");
    }

    private void insertMockData() {
        repository.insertMockCampaigns(new CampaignRepository.OnCampaignListener() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(getContext(),
                        "Đã thêm dữ liệu mẫu",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(),
                        "Lỗi thêm dữ liệu: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCampaigns() {
        if ("ALL".equals(selectedCategory)) {
            // Load all campaigns
            repository.getAllCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
                if (campaigns != null) {
                    if (campaigns.isEmpty()) {
                        // No data, insert mock campaigns
                        insertMockData();
                    } else {
                        adapter.updateData(campaigns);
                        updateCampaignCount(campaigns.size());
                    }
                }
            });
        } else {
            // Load by category
            repository.getCampaignsByCategory(selectedCategory)
                    .observe(getViewLifecycleOwner(), campaigns -> {
                        if (campaigns != null) {
                            adapter.updateData(campaigns);
                            updateCampaignCount(campaigns.size());
                        }
                    });
        }
    }

    private void filterByCategory(String category) {
        selectedCategory = category;

        // Bỏ chọn tất cả chip
        chipAll.setChecked(false);
        chipEducation.setChecked(false);
        chipFood.setChecked(false);
        chipEnvironment.setChecked(false);
        chipHealth.setChecked(false);
        chipUrgent.setChecked(false);


        switch (category) {
            case "ALL": chipAll.setChecked(true); break;
            case "EDUCATION": chipEducation.setChecked(true); break;
            case "FOOD": chipFood.setChecked(true); break;
            case "ENVIRONMENT": chipEnvironment.setChecked(true); break;
            case "HEALTH": chipHealth.setChecked(true); break;
            case "URGENT": chipUrgent.setChecked(true); break;
        }

        loadCampaigns();
    }

    private void setupRecyclerView() {
        adapter = new CampaignAdapter(getContext(), new ArrayList<>(),
                new CampaignAdapter.OnCampaignClickListener() {
                    @Override
                    public void onCampaignClick(Campaign campaign) {
                        openCampaignDetail(campaign);
                    }

                    @Override
                    public void onDetailClick(Campaign campaign) {
                       openCampaignDetail(campaign);
                    }
                });

        rvCampaigns.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCampaigns.setAdapter(adapter);
    }

    private void openCampaignDetail(Campaign campaign) {
        Intent intent = new Intent(getContext(), activity_voluteer_campaign_detail.class);
        intent.putExtra("campaign", campaign);
        startActivity(intent);
    }

    private void setupQuickActions() {
        btnDonation.setOnClickListener(v -> replaceFragment(new frm_volunteer_donation()));
        btnSchedule.setOnClickListener(v -> replaceFragment(new frm_volunteer_calendar()));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(getActivity(), activity_volunteer_campagin_history.class)));
        btnGifts.setOnClickListener(v -> startActivity(new Intent(getActivity(), activity_volunteer_gift_shop.class)));
    }

    private void loadCurrentUserData() {
        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null && isAdded()) { // isAdded() tránh crash khi fragment đã detach
                    tvUserName.setText(user.getFullName());

                    tvTotalPoints.setText(String.valueOf(user.getTotalPoints()));
                    tvTotalDonations.setText(String.valueOf(user.getTotalDonations()));
                    tvTotalCampaigns.setText(String.valueOf(user.getTotalCampaigns()));

                    // Load avatar (nếu có link ảnh thật thì hiện, không thì dùng placeholder)
                    String avatarUrl = user.getAvatar();
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(frm_volunteer_home.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.placeholder_avatar)
                                .error(R.drawable.placeholder_avatar)
                                .circleCrop()
                                .into(ivUserAvatar);
                    } else {
                        ivUserAvatar.setImageResource(R.drawable.placeholder_avatar);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải thông tin: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void replaceFragment(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}