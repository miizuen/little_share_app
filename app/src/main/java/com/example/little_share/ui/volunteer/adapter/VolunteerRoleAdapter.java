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

    public interface OnRoleClickListener {
        void onRegisterClick(CampaignRole role);
    }

    public VolunteerRoleAdapter(Context context, List<CampaignRole> roles, OnRoleClickListener listener) {
        this.context = context;
        this.roles = roles;
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
        // Query số lượng đăng ký đã được duyệt cho vai trò này
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("volunteer_registrations")
                .whereEqualTo("roleId", role.getId())
                .whereEqualTo("status", "approved")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        tvParticipants.setText("0/" + role.getMaxVolunteers() + " người");
                        return;
                    }

                    int currentCount = snapshots.size();
                    int maxCount = role.getMaxVolunteers();

                    // Cập nhật hiển thị số lượng
                    String countText = currentCount + "/" + maxCount + " người";
                    tvParticipants.setText(countText);

                    // Đổi màu và trạng thái nút theo tình trạng
                    if (currentCount >= maxCount && maxCount > 0) {
                        // HẾT SLOT
                        tvParticipants.setTextColor(android.graphics.Color.parseColor("#EF4444")); // Đỏ
                        btnRegister.setEnabled(false);
                        btnRegister.setText("Đã đủ người");
                        btnRegister.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#CCCCCC")));
                    } else if (currentCount >= maxCount * 0.8f) {
                        // GẦN ĐẦY (80% trở lên)
                        tvParticipants.setTextColor(android.graphics.Color.parseColor("#F59E0B")); // Cam
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Đăng kí với vai trò này");
                        btnRegister.setBackgroundTintList(null); // Dùng màu mặc định
                    } else {
                        // CÒN NHIỀU SLOT
                        tvParticipants.setTextColor(android.graphics.Color.parseColor("#22C55E")); // Xanh
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Đăng kí với vai trò này");
                        btnRegister.setBackgroundTintList(null);
                    }
                });
    }

}
