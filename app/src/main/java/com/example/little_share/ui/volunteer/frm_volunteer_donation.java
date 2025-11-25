package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.little_share.R;

public class frm_volunteer_donation extends Fragment {

    LinearLayout btnBook, btnMoney, btnToy, btnShirt;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnBook = view.findViewById(R.id.btnBook);
        btnShirt = view.findViewById(R.id.btnShirt);
        btnToy = view.findViewById(R.id.btnToy);
        btnMoney = view.findViewById(R.id.btnMoney);

        btnBook.setOnClickListener(v -> openDonationForm("BOOK"));
        btnShirt.setOnClickListener(v -> openDonationForm("SHIRT"));
        btnToy.setOnClickListener(v -> openDonationForm("TOY"));
        btnMoney.setOnClickListener(v -> openDonationForm("MONEY"));

    }

    private void openDonationForm(String type) {
        Intent intent = new Intent(getActivity(), activity_volunteer_donation_form.class);
        intent.putExtra("DONATION_TYPE", type);
        startActivity(intent);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frm_volunteer_donation, container, false);
    }
}
