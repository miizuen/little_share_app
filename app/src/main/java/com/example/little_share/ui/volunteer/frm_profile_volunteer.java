package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class frm_profile_volunteer extends Fragment {

    private static final String TAG = "frm_profile_volunteer";
    private UserRepository userRepository;
    private LinearLayout layoutLogout, layoutMyAccount, layoutAboutApp;
    private ImageView ivAvatar;
    private TextView tvUserName, tvUserEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_profile_volunteer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Initialize views
            layoutMyAccount = view.findViewById(R.id.layoutMyAccount);
            layoutLogout = view.findViewById(R.id.layoutLogout);
            layoutAboutApp = view.findViewById(R.id.layoutAboutApp);
            ivAvatar = view.findViewById(R.id.ivAvatar);
            tvUserName = view.findViewById(R.id.tvUserName);
            tvUserEmail = view.findViewById(R.id.tvUserEmail);

            Log.d(TAG, "Views initialized");

            // Click listener for My Account
            if (layoutMyAccount != null) {
                layoutMyAccount.setOnClickListener(v -> {
                    Log.d(TAG, "My Account clicked!");
                    try {
                        Intent intent = new Intent(getContext(), activity_volunteer_edit_profile.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error: " + e.getMessage());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Log.e(TAG, "layoutMyAccount is NULL!");
            }

            // Click listener for Logout
            if (layoutLogout != null) {
                layoutLogout.setOnClickListener(v -> {
                    Log.d(TAG, "Logout clicked!");
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
            } else {
                Log.e(TAG, "layoutLogout is NULL!");
            }

            loadData();

        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage());
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userRepository != null) {
            loadData();
        }
    }

    private void loadData() {
        try {
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
                    try {
                        if (user == null || !isAdded()) {
                            return;
                        }

                        Log.d(TAG, "User loaded: " + user.getFullName());

                        // Update UI
                        if (tvUserName != null) {
                            tvUserName.setText(user.getFullName());
                        }
                        if (tvUserEmail != null) {
                            tvUserEmail.setText(user.getEmail());
                        }

                        // Load avatar
                        if (ivAvatar != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                            Glide.with(frm_profile_volunteer.this)
                                    .load(user.getAvatar())
                                    .placeholder(R.drawable.img_profile_volunteer)
                                    .error(R.drawable.img_profile_volunteer)
                                    .circleCrop()
                                    .into(ivAvatar);
                        } else if (ivAvatar != null) {
                            ivAvatar.setImageResource(R.drawable.img_profile_volunteer);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onSuccess: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error loading user: " + error);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in loadData: " + e.getMessage());
        }
    }
}
