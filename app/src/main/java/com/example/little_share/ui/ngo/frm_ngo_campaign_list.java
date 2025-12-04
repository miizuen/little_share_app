package com.example.little_share.ui.ngo;

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
import android.widget.Toast;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.Campaign;
import com.example.little_share.data.repositories.CampaignRepository;
import com.example.little_share.ui.ngo.adapter.NGOCampaignAdapter;
import com.example.little_share.ui.volunteer.activity_voluteer_campaign_detail;
import com.example.little_share.ui.volunteer.adapter.CampaignAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class frm_ngo_campaign_list extends Fragment {

    private RecyclerView rvCampaigns;
    private CampaignRepository repo;
    private NGOCampaignAdapter adapter;
    private List<Campaign> campaignList = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = view.findViewById(R.id.rvCampaigns);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_ngo_campaign_list, container, false);

        rvCampaigns = view.findViewById(R.id.rvCampaigns);
        rvCampaigns.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NGOCampaignAdapter(
                getContext(),
                campaignList, 
                new NGOCampaignAdapter.OnItemClickListener() {
                    @Override public void onEditClick(Campaign campaign) { /* TODO */ }
                    @Override public void onDetailClick(Campaign campaign) { /* TODO */ }
                }
        );
        rvCampaigns.setAdapter(adapter);

        loadCampaigns();
        return view;
    }

    private void loadCampaigns() {
        new CampaignRepository().getCampaignsByCurrentNgo().observe(getViewLifecycleOwner(), campaigns -> {
            if (campaigns != null) {
                campaignList.clear();
                campaignList.addAll(campaigns);
                adapter.updateData(campaignList);

                Collections.reverse(campaignList);
                adapter.updateData(campaignList);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCampaigns();
    }


}