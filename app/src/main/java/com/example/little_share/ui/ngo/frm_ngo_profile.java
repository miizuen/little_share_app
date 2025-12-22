package com.example.little_share.ui.ngo;

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
import com.example.little_share.data.repositories.OrganizationRepository;
import com.example.little_share.ui.spash_screen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class frm_ngo_profile extends Fragment {

    private static final String TAG = "frm_ngo_profile";
    private OrganizationRepository organizationRepository;
    private LinearLayout layoutLogout, layoutMyAccount;
    private ImageView ivAvatar;
    private TextView tvUserName, tvUserEmail;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frm_ngo_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            initViews(view);
            setupClickListeners();
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
        // Reload data when returning from edit activity
        if (organizationRepository != null) {
            loadData();
        }
    }

    private void initViews(View view) {
        layoutMyAccount = view.findViewById(R.id.layoutMyAccount);
        layoutLogout = view.findViewById(R.id.layoutLogout);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);

        // Debug: Check if views are found
        Log.d(TAG, "layoutMyAccount: " + (layoutMyAccount != null ? "Found" : "NULL"));
        Log.d(TAG, "layoutLogout: " + (layoutLogout != null ? "Found" : "NULL"));
        Log.d(TAG, "ivAvatar: " + (ivAvatar != null ? "Found" : "NULL"));
        Log.d(TAG, "tvUserName: " + (tvUserName != null ? "Found" : "NULL"));
        Log.d(TAG, "tvUserEmail: " + (tvUserEmail != null ? "Found" : "NULL"));

        organizationRepository = new OrganizationRepository();
        db = FirebaseFirestore.getInstance();
    }

    private void setupClickListeners() {
        if (layoutMyAccount != null) {
            layoutMyAccount.setOnClickListener(v -> {
                Log.d(TAG, "My Account clicked!");
                try {
                    Intent intent = new Intent(getContext(), activity_ngo_profile_edit.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening edit profile: " + e.getMessage());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Log.e(TAG, "layoutMyAccount is NULL!");
        }

        if (layoutLogout != null) {
            layoutLogout.setOnClickListener(v -> {
                Log.d(TAG, "Logout clicked!");
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();

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
    }

    private void loadData() {
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Log.e(TAG, "Current user is null!");
                return;
            }

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d(TAG, "Loading data for userId: " + currentUserId);

            organizationRepository.getCurrentOrganization(new OrganizationRepository.OnOrganizationListener() {
                @Override
                public void onSuccess(com.example.little_share.data.models.Organization organization) {
                    try {
                        if (organization == null) {
                            Log.e(TAG, "Organization is NULL!");
                            // Create default organization for new NGO accounts
                            createDefaultOrganization(currentUserId);
                            return;
                        }

                        if (!isAdded()) {
                            Log.w(TAG, "Fragment not added, skipping UI update");
                            return;
                        }

                        Log.d(TAG, "Organization loaded successfully: " + organization.getName());

                        // Update UI with organization data
                        if (tvUserName != null) {
                            tvUserName.setText(organization.getName());
                        }

                        if (tvUserEmail != null) {
                            tvUserEmail.setText(organization.getEmail());
                        }

                        // Load logo with error handling
                        if (ivAvatar != null) {
                            if (organization.getLogo() != null && !organization.getLogo().isEmpty()) {
                                Glide.with(frm_ngo_profile.this)
                                        .load(organization.getLogo())
                                        .placeholder(R.drawable.logo_thelight)
                                        .error(R.drawable.logo_thelight)
                                        .circleCrop()
                                        .into(ivAvatar);
                            } else {
                                ivAvatar.setImageResource(R.drawable.logo_thelight);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onSuccess: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error loading organization: " + error);
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                        // Create default organization for new accounts
                        createDefaultOrganization(currentUserId);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadData: " + e.getMessage());
        }
    }

    private void createDefaultOrganization(String userId) {
        Log.d(TAG, "Creating default organization for new NGO account");

        organizationRepository.createOrganization(
                userId,
                "Light of Heart", // Default name
                "contact@lightofheart.org", // Default email
                "0123456789", // Default phone
                "123 Charity Street, Ho Chi Minh City", // Default address
                new OrganizationRepository.OnCreateOrgListener() {
                    @Override
                    public void onSuccess(String organizationId) {
                        Log.d(TAG, "Default organization created successfully");
                        // Update user document with organizationId
                        updateUserOrganizationId(userId, organizationId);
                        // Reload data after a short delay
                        if (getView() != null) {
                            getView().postDelayed(() -> loadData(), 1000);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Failed to create default organization: " + error);
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Lỗi tạo tổ chức mặc định: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    private void updateUserOrganizationId(String userId, String organizationId) {
        db.collection("users")
                .document(userId)
                .update("organizationId", organizationId)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User organizationId updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update user organizationId: " + e.getMessage());
                });
    }
}
