package com.example.little_share.ui.sponsor;

import android.os.Bundle;
<<<<<<< HEAD
=======

import androidx.fragment.app.Fragment;

>>>>>>> 1021f6666a9fdef6213bea93a837382b23605876
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

<<<<<<< HEAD
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.little_share.R;

public class frm_sponsor_notification extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sponsor_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
=======
import com.example.little_share.R;

public class frm_sponsor_notification extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frm_sponsor_notification, container, false);
>>>>>>> 1021f6666a9fdef6213bea93a837382b23605876
    }
}