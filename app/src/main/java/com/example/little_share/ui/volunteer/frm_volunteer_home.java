package com.example.little_share.ui.volunteer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.little_share.R;

public class frm_volunteer_home extends Fragment {
    LinearLayout btnDonation, btnSchedule, btnHistory,btnGifts ;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnDonation = view.findViewById(R.id.btnDonation);

        btnSchedule = view.findViewById(R.id.btnSchedule);

        btnHistory = view.findViewById(R.id.btnHistory);

        btnGifts = view.findViewById(R.id.btnGifts);

        btnDonation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment donationFrm = new frm_volunteer_donation();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, donationFrm)
                        .commit();
            }
        });

        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment scheduleFrm = new frm_volunteer_calendar();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, scheduleFrm)
                        .commit();
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment historyFrm = new frm_volunteer_history();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, historyFrm)
                        .commit();
            }
        });

        btnGifts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment giftFrm = new frm_volunteer_gift_shop();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, giftFrm)
                        .commit();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frm_volunteer_home, container, false);
    }
}