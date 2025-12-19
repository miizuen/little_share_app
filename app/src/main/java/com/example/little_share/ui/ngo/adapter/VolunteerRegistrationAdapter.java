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
import com.example.little_share.data.models.VolunteerRegistration;

import java.util.List;

public class VolunteerRegistrationAdapter extends RecyclerView.Adapter<VolunteerRegistrationAdapter.ViewHolder> {

    private Context context;
    private List<VolunteerRegistration> registrations;
    private OnActionListener listener;

    public interface OnActionListener {
        void onApprove(VolunteerRegistration registration, int position);
        void onReject(VolunteerRegistration registration, int position);
    }

    public VolunteerRegistrationAdapter(Context context, List<VolunteerRegistration> registrations, OnActionListener listener) {
        this.context = context;
        this.registrations = registrations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_volunteer_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VolunteerRegistration reg = registrations.get(position);

        // Tên (dùng tvSponsorName theo layout của bạn)
        String name = reg.getUserName();
        holder.tvSponsorName.setText(name != null && !name.isEmpty() ? name : "Tình nguyện viên");

        // Vai trò
        holder.tvJob.setText(reg.getRoleName());

        // Ngày và ca
        holder.tvDate.setText(reg.getDate() + " - " + reg.getShiftTime());

        // Click events
        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(reg, position);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(reg, position);
        });
    }

    @Override
    public int getItemCount() {
        return registrations.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < registrations.size()) {
            registrations.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSponsorName, tvJob, tvDate;
        ImageView btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSponsorName = itemView.findViewById(R.id.tvSponsorName);
            tvJob = itemView.findViewById(R.id.tvJob);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
