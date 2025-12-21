package com.example.little_share.ui.ngo.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.VolunteerInfo;
import com.example.little_share.data.models.VolunteerRegistration;

import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
public class VolunteerListAdapter extends RecyclerView.Adapter<VolunteerListAdapter.ViewHolder> {

    private List<VolunteerInfo> list = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onViewDetailClick(VolunteerRegistration registration);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<VolunteerInfo> data) {
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
        TextView tvVolunteerName, tvPoints, tvEvents;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            volunteerAvt = itemView.findViewById(R.id.volunteerAvt);
            tvVolunteerName = itemView.findViewById(R.id.tvVolunteerName);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvEvents = itemView.findViewById(R.id.tvEvents);
        }

        void bind(VolunteerInfo volunteerInfo) {
            VolunteerRegistration reg = volunteerInfo.getRegistration();

            // Tên
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

            // Điểm và sự kiện từ dữ liệu thực
            tvPoints.setText(volunteerInfo.getTotalPoints() + " điểm");
            tvEvents.setText(volunteerInfo.getTotalCampaigns() + " sự kiện");

            // Load avatar với Glide
            String avatarUrl = volunteerInfo.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.placeholder_avatar)
                        .error(R.drawable.placeholder_avatar)
                        .transform(new CircleCrop())
                        .into(volunteerAvt);
            } else {
                // Nếu không có avatar, hiển thị placeholder
                Glide.with(itemView.getContext())
                        .load(R.drawable.placeholder_avatar)
                        .transform(new CircleCrop())
                        .into(volunteerAvt);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailClick(reg);
                }
            });

            Log.d("AVATAR_DEBUG", "Avatar URL: " + avatarUrl);
            Log.d("AVATAR_DEBUG", "User name: " + name);
        }
    }
}
