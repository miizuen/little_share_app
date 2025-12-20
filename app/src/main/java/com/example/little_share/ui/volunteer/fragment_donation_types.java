package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.little_share.R;

public class fragment_donation_types extends Fragment {

    private CardView btnBook, btnShirt, btnToy, btnEssentials;

    public fragment_donation_types() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donation_types, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupDonationButtons();
    }

    private void initViews(View view) {
        btnBook = view.findViewById(R.id.cvBook);
        btnShirt = view.findViewById(R.id.cvClothes);
        btnToy = view.findViewById(R.id.cvToy);
        btnEssentials = view.findViewById(R.id.cvEssentials);
    }

    private void setupDonationButtons() {
        btnBook.setOnClickListener(v -> openDonationForm("BOOKS"));
        btnShirt.setOnClickListener(v -> openDonationForm("CLOTHES"));
        btnToy.setOnClickListener(v -> openDonationForm("TOYS"));
        btnEssentials.setOnClickListener(v -> openDonationForm("ESSENTIALS"));
    }

    private void openDonationForm(String type) {
        Intent intent = new Intent(getActivity(), activity_volunteer_donation_form.class);
        intent.putExtra("DONATION_TYPE", type);
        startActivity(intent);
    }
}