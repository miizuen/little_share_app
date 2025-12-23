package com.example.little_share.ui.volunteer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.CampaignRole;

import java.util.List;

public class VolunteerRoleAdapter extends RecyclerView.Adapter<VolunteerRoleAdapter.ViewHolder> {
    private Context context;
    private List<CampaignRole> roles;
    private OnRoleClickListener listener;
    private String campaignId;
    private String shiftId;


    public interface OnRoleClickListener {
        void onRegisterClick(CampaignRole role);
    }

    public VolunteerRoleAdapter(Context context, List<CampaignRole> roles,
                                String campaignId, String shiftId, OnRoleClickListener listener) {
        this.context = context;
        this.roles = roles;
        this.campaignId = campaignId;
        this.shiftId = shiftId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_role_card, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CampaignRole role = roles.get(position);

        android.util.Log.d("RoleAdapter", "Role: " + role.getRoleName()
                + ", maxVol: " + role.getMaxVolunteers()
                + ", currentVol: " + role.getCurrentVolunteers());

        holder.tvRoleTitle.setText(role.getRoleName());
        holder.tvRoleDescription.setText(role.getDescription());

        // THÊM: Load số lượng realtime
        loadRoleParticipants(role, holder.tvParticipants, holder.btnRegister);

        holder.btnRegister.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRegisterClick(role);
            }
        });
    }


    @Override
    public int getItemCount() {
        return roles != null ? roles.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoleTitle, tvRoleDescription, tvParticipants;
        Button btnRegister;

        ViewHolder(View v) {
            super(v);
            tvRoleTitle = v.findViewById(R.id.tvRoleTitle);
            tvRoleDescription = v.findViewById(R.id.tvRoleDescription);
            tvParticipants = v.findViewById(R.id.tvParticipants);
            btnRegister = v.findViewById(R.id.btnRegister);
        }
    }
    // THÊM METHOD: Load số lượng người tham gia realtime
    private void loadRoleParticipants(CampaignRole role, TextView tvParticipants, Button btnRegister) {
        android.util.Log.d("ROLE_SLOTS", "=== LOADING ROLE PARTICIPANTS ===");
        android.util.Log.d("ROLE_SLOTS", "Role ID: " + role.getId());
        android.util.Log.d("ROLE_SLOTS", "Role Name: " + role.getRoleName());
        android.util.Log.d("ROLE_SLOTS", "Campaign ID: " + this.campaignId);
        android.util.Log.d("ROLE_SLOTS", "Role's Shift ID: " + role.getShiftId());

        if (this.campaignId == null || this.campaignId.isEmpty()) {
            android.util.Log.e("ROLE_SLOTS", "Campaign ID is null or empty");
            tvParticipants.setText("0/" + role.getMaxVolunteers() + " người");
            return;
        }

        // Query cơ bản
        com.google.firebase.firestore.Query query = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("volunteer_registrations")
                .whereEqualTo("campaignId", this.campaignId)
                .whereEqualTo("roleId", role.getId());

        // ✅ SỬ DỤNG shiftId TỪ ROLE (không phải từ adapter)
        if (role.getShiftId() != null && !role.getShiftId().isEmpty()) {
            query = query.whereEqualTo("shiftId", role.getShiftId());
            android.util.Log.d("ROLE_SLOTS", "Filtering by shiftId: " + role.getShiftId());
        } else {
            android.util.Log.d("ROLE_SLOTS", "Role has no shiftId - counting all shifts");
        }

        query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                android.util.Log.e("ROLE_SLOTS", "Error: " + error.getMessage());
                tvParticipants.setText("0/" + role.getMaxVolunteers() + " người");
                return;
            }

            if (snapshots == null) {
                android.util.Log.w("ROLE_SLOTS", "Snapshots is null");
                tvParticipants.setText("0/" + role.getMaxVolunteers() + " người");
                return;
            }

            int validCount = 0;
            android.util.Log.d("ROLE_SLOTS", "=== PROCESSING REGISTRATIONS ===");
            android.util.Log.d("ROLE_SLOTS", "Total documents found: " + snapshots.size());

            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshots) {
                String status = doc.getString("status");
                String docShiftId = doc.getString("shiftId");

                android.util.Log.d("ROLE_SLOTS", "Doc: " + doc.getId()
                        + ", status: " + status
                        + ", shiftId: " + docShiftId);

                if (status != null) {
                    switch (status.toLowerCase()) {
                        case "approved":
                        case "pending":
                        case "completed":
                            validCount++;
                            break;
                    }
                }
            }

            android.util.Log.d("ROLE_SLOTS", "Total valid registrations: " + validCount);
            android.util.Log.d("ROLE_SLOTS", "Max volunteers: " + role.getMaxVolunteers());

            updateRoleUI(validCount, role.getMaxVolunteers(), tvParticipants, btnRegister);
        });
    }

    private void updateRoleUI(int currentCount, int maxCount, TextView tvParticipants, Button btnRegister) {
        // Hiển thị số lượng
        String countText = currentCount + "/" + maxCount + " người";
        tvParticipants.setText(countText);

        android.util.Log.d("ROLE_SLOTS", "Updating UI: " + countText);

        // Đổi màu và trạng thái nút theo tình trạng
        if (currentCount >= maxCount && maxCount > 0) {
            // HẾT SLOT
            tvParticipants.setTextColor(android.graphics.Color.parseColor("#EF4444")); // Đỏ
            btnRegister.setEnabled(false);
            btnRegister.setText("Đã đủ người");
            btnRegister.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#CCCCCC")));
            android.util.Log.d("ROLE_SLOTS", "Role is FULL");

        } else if (currentCount >= maxCount * 0.8f && maxCount > 0) {
            // GẦN ĐẦY (80% trở lên)
            tvParticipants.setTextColor(android.graphics.Color.parseColor("#F59E0B")); // Cam
            btnRegister.setEnabled(true);
            btnRegister.setText("Đăng kí với vai trò này");
            btnRegister.setBackgroundTintList(null); // Dùng màu mặc định
            android.util.Log.d("ROLE_SLOTS", "Role is ALMOST FULL");

        } else {
            // CÒN NHIỀU SLOT
            tvParticipants.setTextColor(android.graphics.Color.parseColor("#22C55E")); // Xanh
            btnRegister.setEnabled(true);
            btnRegister.setText("Đăng kí với vai trò này");
            btnRegister.setBackgroundTintList(null);
            android.util.Log.d("ROLE_SLOTS", "Role has AVAILABLE slots");
        }
    }
}
