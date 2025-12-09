package com.example.little_share.ui.ngo.adapter;

import android.content.Context;
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
import java.util.Map;

public class RoleAssignmentAdapter extends RecyclerView.Adapter<RoleAssignmentAdapter.ViewHolder> {
    private Context context;
    private List<CampaignRole> roles;
    private Map<String, Integer> assignments;

    public RoleAssignmentAdapter(Context context, List<CampaignRole> roles, Map<String, Integer> assignments) {
        this.context = context;
        this.roles = roles;
        this.assignments = assignments;

        // Khởi tạo count = 0 cho tất cả roles
        for (CampaignRole role : roles) {
            if (!assignments.containsKey(role.getRoleName())) {
                assignments.put(role.getRoleName(), 0);
            }
        }
    }

    @NonNull
    @Override
    public RoleAssignmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shift_role_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoleAssignmentAdapter.ViewHolder holder, int position) {
        CampaignRole role = roles.get(position);
        String roleName = role.getRoleName();

        holder.tvRoleName.setText(roleName);
        holder.tvRolePoints.setText(role.getPointsReward() + " điểm");

        int currentCount =assignments.getOrDefault(roleName, 0);
        holder.tvCount.setText(String.valueOf(currentCount));

        holder.btnDecrease.setOnClickListener(v -> {
            int count = assignments.getOrDefault(roleName, 0);
            if(count > 0){
                assignments.put(roleName, count - 1);
                holder.tvCount.setText(String.valueOf(count - 1));
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            int count = assignments.getOrDefault(roleName, 0);
            assignments.put(roleName, count + 1);
            holder.tvCount.setText(String.valueOf(count + 1));
        });
    }

    @Override
    public int getItemCount() {
        return roles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvRoleName, tvRolePoints, tvCount;
        ImageView btnDecrease, btnIncrease;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoleName = itemView.findViewById(R.id.tvRoleName);
            tvRolePoints = itemView.findViewById(R.id.tvRolePoints);
            tvCount = itemView.findViewById(R.id.tvCount);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
        }
    }
}
