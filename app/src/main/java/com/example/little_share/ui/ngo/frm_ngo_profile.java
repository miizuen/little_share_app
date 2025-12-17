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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.repositories.OrganizationRepository;
import com.example.little_share.ui.spash_screen;

public class frm_ngo_profile extends Fragment {

    private static final String TAG = "frm_ngo_profile";
    private OrganizationRepository organizationRepository;
    private LinearLayout layoutLogout, layoutMyAccount;
    private ImageView ivAvatar;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        layoutMyAccount = view.findViewById(R.id.layoutMyAccount);
        layoutLogout = view.findViewById(R.id.layoutLogout);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        layoutMyAccount.setOnClickListener(v ->{
            Intent intent = new Intent(getContext(), activity_ngo_profile_edit.class);
            startActivity(intent);
        });

        layoutLogout.setOnClickListener(v -> {
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

    private void loadData() {
        organizationRepository = new OrganizationRepository();

        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Loading data for userId: " + currentUserId);

        organizationRepository.getCurrentOrganization(new OrganizationRepository.OnOrganizationListener() {
            @Override
            public void onSuccess(com.example.little_share.data.models.Organization organization) {
                if (organization == null) {
                    Log.e(TAG, "Organization is NULL!");
                    Toast.makeText(getContext(), "Không tìm thấy tổ chức", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isAdded()) {
                    return;
                }

                Log.d(TAG, "Organization loaded successfully: " + organization.getName());


                // Load logo
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

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading organization: " + error);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_frm_ngo_profile, container, false);
    }
}