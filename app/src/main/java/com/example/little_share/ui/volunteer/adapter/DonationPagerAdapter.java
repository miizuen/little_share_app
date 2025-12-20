package com.example.little_share.ui.volunteer.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.little_share.ui.volunteer.fragment_donation_campagin;
import com.example.little_share.ui.volunteer.fragment_donation_types;


public class DonationPagerAdapter extends FragmentStateAdapter {

    public DonationPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new fragment_donation_types();
            case 1:
                return new fragment_donation_campagin();
            default:
                return new fragment_donation_types();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
