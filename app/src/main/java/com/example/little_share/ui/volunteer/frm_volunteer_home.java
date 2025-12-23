package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.little_share.ui.volunteer.adapter.UrgentCampaignAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class frm_volunteer_home extends Fragment {
    LinearLayout btnDonation, btnSchedule, btnHistory, btnGifts;
    private TextView tvUserName, tvTotalPoints, tvTotalDonations, tvTotalCampaigns, tvCampaignCount;
    private TextView tvUrgentTitle, tvUrgentLocation;
    private Campaign currentUrgentCampaign;
    private ImageView ivUserAvatar;
    private EditText etSearch;
    private Chip chipAll, chipEducation, chipFood, chipEnvironment, chipHealth, chipUrgent;
    private RecyclerView rvCampaigns, rvUrgentCampaigns;
    private LinearLayout layoutUrgentSection;
    private UrgentCampaignAdapter urgentAdapter;
    private UserRepository userRepository;
    private CampaignAdapter adapter;
    private CampaignRepository repository;
    private String selectedCategory = "ALL";
    private List<Campaign> allCampaigns = new ArrayList<>();

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
        layoutUrgentSection = view.findViewById(R.id.layoutUrgentSection);
        rvUrgentCampaigns = view.findViewById(R.id.rvUrgentCampaigns);
        etSearch = view.findViewById(R.id.etSearch);

        loadCurrentUserData();

        setupQuickActions();

        setupRecyclerView();

        setupUrgentRecyclerView();

        setupChips();

        setupSearch();

        loadCampaigns();

        loadUrgentDonationCampaigns();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCampaigns(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCampaigns(String query) {
        if (query.isEmpty()) {
            adapter.updateData(allCampaigns);
            updateCampaignCount(allCampaigns.size());
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<Campaign> filtered = new ArrayList<>();

        for (Campaign campaign : allCampaigns) {
            String name = campaign.getName() != null ? campaign.getName().toLowerCase() : "";
            String orgName = campaign.getOrganizationName() != null ? campaign.getOrganizationName().toLowerCase() : "";

            if (name.contains(lowerQuery) || orgName.contains(lowerQuery)) {
                filtered.add(campaign);
            }
        }

        adapter.updateData(filtered);
        updateCampaignCount(filtered.size());
    }

    private void setupUrgentRecyclerView() {
        urgentAdapter = new UrgentCampaignAdapter(getContext(), new ArrayList<>(),
                campaign -> openDonationCampaignDetail(campaign));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvUrgentCampaigns.setLayoutManager(layoutManager);
        rvUrgentCampaigns.setAdapter(urgentAdapter);
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



    private void loadCampaigns() {
        if ("ALL".equals(selectedCategory)) {
            // Load all campaigns
            repository.getAllCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
                if (campaigns != null) {
                    if (campaigns.isEmpty()) {
                        // No data, insert mock campaigns
                    } else {
                        allCampaigns = new ArrayList<>(campaigns);
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
                            allCampaigns = new ArrayList<>(campaigns);
                            adapter.updateData(campaigns);
                            updateCampaignCount(campaigns.size());
                        }
                    });
        }
    }

    private void filterByCategory(String category) {
        selectedCategory = category;

        // Reset tất cả chip về màu xám
        resetChipStyle(chipAll);
        resetChipStyle(chipEducation);
        resetChipStyle(chipFood);
        resetChipStyle(chipEnvironment);
        resetChipStyle(chipHealth);
        resetChipStyle(chipUrgent);

        // Set chip được chọn thành màu cam
        switch (category) {
            case "ALL": setSelectedChipStyle(chipAll); break;
            case "EDUCATION": setSelectedChipStyle(chipEducation); break;
            case "FOOD": setSelectedChipStyle(chipFood); break;
            case "ENVIRONMENT": setSelectedChipStyle(chipEnvironment); break;
            case "HEALTH": setSelectedChipStyle(chipHealth); break;
            case "URGENT": setSelectedChipStyle(chipUrgent); break;
        }

        loadCampaigns();
    }

    // Chip được chọn: nền cam, chữ trắng
    private void setSelectedChipStyle(Chip chip) {
        chip.setChipBackgroundColorResource(R.color.primary_orange);
        chip.setTextColor(getResources().getColor(R.color.white));
    }

    // Chip không được chọn: nền xám, chữ đen
    private void resetChipStyle(Chip chip) {
        chip.setChipBackgroundColorResource(R.color.gray_light);
        chip.setTextColor(getResources().getColor(R.color.black));
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
                if (user != null && isAdded()) {
                    tvUserName.setText(user.getFullName());
                    tvTotalPoints.setText(String.valueOf(user.getTotalPoints()));
                    tvTotalDonations.setText(String.valueOf(user.getTotalDonations()));

                    // Load avatar
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

                    // ===== LOAD SỐ CHIẾN DỊCH TỪ FIREBASE =====
                    loadCompletedCampaignsCount();
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

    private void loadCompletedCampaignsCount() {
        repository.getRegistrationStats(new CampaignRepository.OnRegistrationStatsListener() {
            @Override
            public void onSuccess(int totalCampaigns, int totalPoints) {
                if (isAdded()) {
                    android.util.Log.d("HOME", "Completed campaigns: " + totalCampaigns);
                    tvTotalCampaigns.setText(String.valueOf(totalCampaigns));
                }
            }
        });
    }

    private void loadUrgentDonationCampaigns() {
        repository.getAllCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            if (campaigns != null) {
                List<Campaign> urgentCampaigns = new ArrayList<>();

                // Tìm tất cả campaigns urgent
                for (Campaign campaign : campaigns) {
                    if ("URGENT".equals(campaign.getCategory())) {
                        urgentCampaigns.add(campaign);
                    }
                }

                if (!urgentCampaigns.isEmpty()) {
                    urgentAdapter.updateData(urgentCampaigns);
                    layoutUrgentSection.setVisibility(View.VISIBLE);
                } else {
                    layoutUrgentSection.setVisibility(View.GONE);
                }
            }
        });
    }


    private void displayUrgentCampaign(Campaign campaign) {
        currentUrgentCampaign = campaign;
        tvUrgentTitle.setText(campaign.getName());

        // Format location với working hours
        String locationText = campaign.getLocation();
        if (campaign.getSpecificLocation() != null && !campaign.getSpecificLocation().isEmpty()) {
            locationText += " • " + campaign.getSpecificLocation();
        }
        locationText += " • Cần gấp";

        tvUrgentLocation.setText(locationText);
    }

    private void openDonationCampaignDetail(Campaign campaign) {
        // Mở activity đặc biệt cho donation campaign thay vì campaign detail thông thường
        Intent intent = new Intent(getContext(), activity_volunteer_donation_campaign_detail.class);
        intent.putExtra("campaign", campaign);
        startActivity(intent);
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
    @Override
    public void onResume() {
        super.onResume();
        // Refresh user data khi fragment được hiển thị lại
        loadCurrentUserData();
    }

    // Phương thức public để refresh từ bên ngoài
    public void refreshUserData() {
        if (isAdded()) {
            loadCurrentUserData();
        }
    }

}