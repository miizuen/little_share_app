package com.example.little_share.ui.ngo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.little_share.R;

public class frm_ngo_home extends Fragment {

    ImageView btnCreateCmp, btnReport, btnAttendance, btnReward, btnDonation;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnCreateCmp = view.findViewById(R.id.btnCreateCmp);
        btnReport = view.findViewById(R.id.btnReports);
        btnAttendance = view.findViewById(R.id.btnAttendance);
        btnReward = view.findViewById(R.id.btnReward);
        btnDonation = view.findViewById(R.id.btnDonation);

        btnCreateCmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), activity_ngo_create_campagin.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_ngo_home, container, false);
    }
}