package com.example.little_share.ui.ngo.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.VolunteerRegistration;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class VolunteerListAdapter extends RecyclerView.Adapter<VolunteerListAdapter.ViewHolder> {

    private List<VolunteerRegistration> list = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onViewDetailClick(VolunteerRegistration registration);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<VolunteerRegistration> data) {
        this.list = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ngo_volunteer_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView volunteerAvt;
        TextView tvVolunteerName, tvVolunteerEmail, tvStatus;
        TextView tvCampaignName, tvRoleName, tvDate;
        MaterialButton btnAction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            volunteerAvt = itemView.findViewById(R.id.volunteerAvt);
            tvVolunteerName = itemView.findViewById(R.id.tvVolunteerName);
            tvVolunteerEmail = itemView.findViewById(R.id.tvVolunteerEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCampaignName = itemView.findViewById(R.id.tvCampaignName);
            tvRoleName = itemView.findViewById(R.id.tvRoleName);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnAction = itemView.findViewById(R.id.btnAction);
        }

        void bind(VolunteerRegistration reg) {
            // Tên - nếu null thì hiển thị phần đầu email
            String name = reg.getUserName();
            if (name == null || name.isEmpty()) {
                String email = reg.getUserEmail();
                if (email != null && email.contains("@")) {
                    name = email.substring(0, email.indexOf("@"));
                } else {
                    name = "N/A";
                }
            }
            tvVolunteerName.setText(name);
            tvVolunteerEmail.setText(reg.getUserEmail() != null ? reg.getUserEmail() : "N/A");

            // Thông tin chiến dịch
            tvCampaignName.setText(reg.getCampaignName() != null ? reg.getCampaignName() : "N/A");
            tvRoleName.setText(reg.getRoleName() != null ? reg.getRoleName() : "N/A");
            tvDate.setText(reg.getDate() != null ? reg.getDate() : "N/A");

            // Trạng thái
            String status = reg.getStatus();
            if ("approved".equals(status)) {
                tvStatus.setText("Đã đăng ký");
                tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0")); // Nền cam nhạt
                tvStatus.setTextColor(Color.parseColor("#FF9800")); // Chữ cam
            } else if ("joined".equals(status)) {
                tvStatus.setText("Đã tham gia");
                tvStatus.setBackgroundColor(Color.parseColor("#E3F2FD")); // Nền xanh nhạt
                tvStatus.setTextColor(Color.parseColor("#1976D2")); // Chữ xanh
            } else if ("completed".equals(status)) {
                tvStatus.setText("Hoàn thành");
                tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9")); // Nền xanh lá nhạt
                tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Chữ xanh lá
            }
            // Button
            // Button - chỉ hiện khi "joined"
            if ("joined".equals(status)) {
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewDetailClick(reg);
                    }
                });
            } else {
                btnAction.setVisibility(View.GONE);
            }
        }
    }
}
