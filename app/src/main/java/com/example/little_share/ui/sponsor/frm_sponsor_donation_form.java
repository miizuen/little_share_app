package com.example.little_share.ui.sponsor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.little_share.R;
import com.example.little_share.frm_payment;
import com.example.little_share.ui.volunteer.frm_volunteer_donation;

public class frm_sponsor_donation_form extends Fragment {
    LinearLayout btnSponsor, btnCancel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnSponsor = view.findViewById(R.id.btnSponsor);

        btnCancel = view.findViewById(R.id.btnCancel);

        btnSponsor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment paymentFrm = new frm_payment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, paymentFrm)
                        .commit();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment cancelFrm = new frm_sponsor_home();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, cancelFrm)
                        .commit();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_sponsor_donation_form, container, false);
    }
}