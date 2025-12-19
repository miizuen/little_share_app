package com.example.little_share.ui.ngo;

import android.content.Intent;
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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.repositories.OrganizationRepository;

public class frm_ngo_home extends Fragment {

    private static final String TAG = "frm_ngo_home";

    ImageView btnCreateCmp, btnReport, btnAttendance, btnReward, btnDonation;
    private ImageView ivOrgLogo;
    private TextView tvOrganizationName, tvTotalVolunteers, tvTotalCampaigns, tvTotalSponsors, tvTotalPoints;
    private OrganizationRepository organizationRepository;

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

    private void initViews(View view) {
        // Quick action buttons
        btnCreateCmp = view.findViewById(R.id.btnCreateCmp);
        btnReport = view.findViewById(R.id.btnReports);
        btnAttendance = view.findViewById(R.id.btnAttendance);
        btnReward = view.findViewById(R.id.btnReward);
        btnDonation = view.findViewById(R.id.btnDonation);

        // Header views
        ivOrgLogo = view.findViewById(R.id.ivOrgLogo);
        tvOrganizationName = view.findViewById(R.id.tvOrganizationName);
        tvTotalVolunteers = view.findViewById(R.id.tvTotalVolunteers);
        tvTotalCampaigns = view.findViewById(R.id.tvTotalCampaigns);
        tvTotalSponsors = view.findViewById(R.id.tvTotalSponsors);
        tvTotalPoints = view.findViewById(R.id.tvTotalPoints);
    }

    private void setupQuickActions() {
        btnCreateCmp.setOnClickListener(v -> startActivity(new Intent(getActivity(), activity_ngo_create_campagin.class)));
        btnAttendance.setOnClickListener(v -> startActivity(new Intent(getActivity(), activity_ngo_attendance.class)));
        btnReport.setOnClickListener(v -> startActivity(new Intent(getActivity(), activity_ngo_finance_report.class)));
        btnReward.setOnClickListener(v -> startActivity(new Intent(getActivity(), activity_ngo_gift.class)));
        btnDonation.setOnClickListener(v -> Toast.makeText(getContext(), "Quyên góp", Toast.LENGTH_SHORT).show());
    }

    private void loadOrganizationData() {
        organizationRepository = new OrganizationRepository();

        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Loading data for userId: " + currentUserId);

        organizationRepository.getCurrentOrganization(new OrganizationRepository.OnOrganizationListener() {
            @Override
            public void onSuccess(com.example.little_share.data.models.Organization organization) {
                if (organization == null) {
                    Log.e(TAG, "Organization is NULL!");
                    Toast.makeText(getContext(), "Không tìm thấy tổ chức", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isAdded()) {
                    return;
                }

                Log.d(TAG, "Organization loaded successfully: " + organization.getName());

                tvOrganizationName.setText(organization.getName());

                // Load logo
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


                tvTotalVolunteers.setText(String.valueOf(organization.getTotalVolunteers()));
                tvTotalCampaigns.setText(String.valueOf(organization.getTotalCampaigns()));

                tvTotalSponsors.setText("15");

                tvTotalPoints.setText(String.valueOf(organization.getTotalPointsGiven()));
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading organization: " + error);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}