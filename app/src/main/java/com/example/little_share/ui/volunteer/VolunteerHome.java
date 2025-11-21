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
import com.example.little_share.frm_donation;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class VolunteerHome extends Fragment {
    LinearLayout btnDonation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_volunteer_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnDonation = view.findViewById(R.id.btnDonation);
        btnDonation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment donationFragment = new frm_donation();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, donationFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
}