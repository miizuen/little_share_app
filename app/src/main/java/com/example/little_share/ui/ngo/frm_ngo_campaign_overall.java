package com.example.little_share.ui.ngo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class frm_ngo_campaign_overall extends Fragment {

    private TextView tvLocation, tvDate, tvDetails;
    private Button btnEdit, btnRegister;
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

        btnRegister.setOnClickListener(v -> {
            // TODO: Xử lý đăng ký
        });
    }

    private void initViews(View view) {
        tvLocation = view.findViewById(R.id.tvLocation);
        tvDate = view.findViewById(R.id.tvDate);
        tvDetails = view.findViewById(R.id.tvDetails);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnRegister = view.findViewById(R.id.btnRegister);
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

        String location = campaign.getLocation();
        if (campaign.getSpecificLocation() != null && !campaign.getSpecificLocation().isEmpty()) {
            location += ", " + campaign.getSpecificLocation();
        }
        tvLocation.setText(location);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateRange = sdf.format(campaign.getStartDate()) + " - " + sdf.format(campaign.getEndDate());
        tvDate.setText(dateRange);
        tvDetails.setText(campaign.getDescription());
    }

}