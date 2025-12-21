package com.example.little_share.ui.ngo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class frm_ngo_campaign_overall extends Fragment {

    private TextView tvLocation, tvDate, tvDetails, tvRequirements;
    private LinearLayout layoutRequirements;
    private Button btnEdit;
    private Campaign campaign;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_ngo_campaign_overall, container, false);

        initViews(view);
        loadData();
        setupButtons();
        if (getArguments() != null) {
            campaign = (Campaign) getArguments().getSerializable("campaign");
        }

        displayData();

        return view;
    }

    public void updateCampaign(Campaign newCampaign) {
        this.campaign = newCampaign;
        displayData();
    }

    private void setupButtons() {
        btnEdit.setOnClickListener(v -> {
            // TODO: Mở màn hình chỉnh sửa
        });
    }

    private void initViews(View view) {
        tvLocation = view.findViewById(R.id.tvLocation);
        tvDate = view.findViewById(R.id.tvDate);
        tvDetails = view.findViewById(R.id.tvDetails);
        tvRequirements = view.findViewById(R.id.tvRequirements);
        layoutRequirements = view.findViewById(R.id.layoutRequirements);
        btnEdit = view.findViewById(R.id.btnEdit);
    }

    private void loadData() {
        if (getArguments() != null) {
            campaign = (Campaign) getArguments().getSerializable("campaign");
            if (campaign != null) {
                displayData();
            }
        }
    }

    private void displayData() {
        if (campaign == null) {
            return;
        }

        // Hiển thị địa điểm
        String location = campaign.getLocation();
        if (campaign.getSpecificLocation() != null && !campaign.getSpecificLocation().isEmpty()) {
            location += ", " + campaign.getSpecificLocation();
        }
        tvLocation.setText(location);

        // Hiển thị thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateRange = sdf.format(campaign.getStartDate()) + " - " + sdf.format(campaign.getEndDate());
        tvDate.setText(dateRange);

        // Hiển thị mô tả
        tvDetails.setText(campaign.getDescription());

        // Hiển thị yêu cầu (chỉ cho chiến dịch không có roles)
        if (campaign.getRoles() == null || campaign.getRoles().isEmpty()) {
            // Chiến dịch không phân vai trò - hiển thị requirements
            layoutRequirements.setVisibility(View.VISIBLE);

            String requirements = campaign.getRequirements();
            if (requirements != null && !requirements.isEmpty()) {
                // Format requirements với bullet points nếu có nhiều dòng
                String[] lines = requirements.split("\n");
                StringBuilder formattedReqs = new StringBuilder();

                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        // Thêm bullet point nếu chưa có
                        if (!line.trim().startsWith("•") && !line.trim().startsWith("-")) {
                            formattedReqs.append("• ").append(line.trim()).append("\n");
                        } else {
                            formattedReqs.append(line.trim()).append("\n");
                        }
                    }
                }

                tvRequirements.setText(formattedReqs.toString().trim());
            } else {
                tvRequirements.setText("Không có yêu cầu đặc biệt");
            }
        } else {
            // Chiến dịch có phân vai trò - ẩn phần requirements
            layoutRequirements.setVisibility(View.GONE);
        }
    }
}