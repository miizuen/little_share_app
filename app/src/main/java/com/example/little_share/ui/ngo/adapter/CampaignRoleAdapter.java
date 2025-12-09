package com.example.little_share.ui.ngo.adapter;

import android.content.Context;
import android.hardware.lights.LightState;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Campain.CampaignRole;

import java.util.List;

public class CampaignRoleAdapter extends RecyclerView.Adapter<CampaignRoleAdapter.ViewHolder> {
    private Context context;
    private List<CampaignRole> roles;
    private OnRoleActionListener listener;

    public CampaignRoleAdapter(Context context, List<CampaignRole> roles, OnRoleActionListener listener) {
        this.context = context;
        this.roles = roles;
        this.listener = listener;
    }
    public interface OnRoleActionListener {
        void onEditClick(CampaignRole role, int position);
        void onDeleteClick(CampaignRole role, int position);
    }


    @NonNull
    @Override
    public CampaignRoleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_campagin_role_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignRoleAdapter.ViewHolder holder, int position) {
        CampaignRole role = roles.get(position);
        holder.tvRoleName.setText(role.getRoleName());
        holder.tvRoleDescription.setText(role.getDescription());
        holder.tvRolePoints.setText(role.getPointsReward() + " điểm");

        holder.btnEdit.setOnClickListener(v -> {
            if(listener != null){
                listener.onEditClick(role, position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if(listener != null){
                listener.onDeleteClick(role, position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return roles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoleName, tvRoleDescription, tvRolePoints;
        ImageView btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvRoleName = v.findViewById(R.id.tvRoleName);
            tvRoleDescription = v.findViewById(R.id.tvRoleDescription);
            tvRolePoints = v.findViewById(R.id.tvRolePoints);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }








}
