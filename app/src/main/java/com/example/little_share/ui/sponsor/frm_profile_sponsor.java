package com.example.little_share.ui.sponsor;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.User;
import com.example.little_share.data.repositories.UserRepository;
import com.example.little_share.ui.spash_screen;

public class frm_profile_sponsor extends Fragment {

    private static final String TAG = "frm_profile_sponsor";
    private UserRepository userRepository;
    private CardView cardAccount, cardLogout;
    private ImageView ivAvatar;
    private TextView tvUserName, tvUserEmail;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        cardAccount = view.findViewById(R.id.cardAccount);
        cardLogout = view.findViewById(R.id.cardLogout);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);

        // Check if ivAvatar is null
        if (ivAvatar == null) {
            Log.e(TAG, "ivAvatar is NULL! Check if ImageView has android:id=\"@+id/ivAvatar\" in XML");
        }

        // Click listener for My Account - Navigate to Bio Profile Activity
        cardAccount.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), activity_bio_profile_sponsor.class);
            startActivity(intent);
        });

        // Click listener for Logout
        cardLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(getContext(), spash_screen.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data when returning from edit activity
        if (userRepository != null) {
            loadData();
        }
    }

    private void loadData() {
        userRepository = new UserRepository();

        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "Current user is null!");
            return;
        }

        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Loading data for userId: " + currentUserId);

        userRepository.getCurrentUserData(new UserRepository.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    Log.e(TAG, "User is NULL!");
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                if (!isAdded()) {
                    return;
                }

                Log.d(TAG, "User loaded successfully: " + user.getFullName());

                // Update UI with user data
                if (tvUserName != null) {
                    tvUserName.setText(user.getFullName());
                }
                if (tvUserEmail != null) {
                    tvUserEmail.setText(user.getEmail());
                }

                // Load avatar - check if ivAvatar is not null
                if (ivAvatar != null) {
                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        Log.d(TAG, "Loading avatar: " + user.getAvatar());
                        Glide.with(frm_profile_sponsor.this)
                                .load(user.getAvatar())
                                .placeholder(R.drawable.logo_d_japan)
                                .error(R.drawable.logo_d_japan)
                                .circleCrop()
                                .into(ivAvatar);
                    } else {
                        Log.d(TAG, "No avatar URL, using default image");
                        ivAvatar.setImageResource(R.drawable.logo_d_japan);
                    }
                } else {
                    Log.e(TAG, "ivAvatar is NULL! Cannot load avatar image");
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading user: " + error);
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frm_profile_sponsor, container, false);
    }
}
