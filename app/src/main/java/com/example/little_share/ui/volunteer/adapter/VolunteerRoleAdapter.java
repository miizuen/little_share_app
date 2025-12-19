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
        holder.tvParticipants.setText(role.getCurrentVolunteers() + "/" + role.getMaxVolunteers() + " người");

        // Kiểm tra nếu vai trò đã đầy
        if (role.isFull()) {
            holder.btnRegister.setEnabled(false);
            holder.btnRegister.setText("Đã đủ người");
        } else {
            holder.btnRegister.setEnabled(true);
            holder.btnRegister.setText("Đăng kí với vai trò này");
        }

        holder.btnRegister.setOnClickListener(v -> {
            if (listener != null && !role.isFull()) {
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
}
