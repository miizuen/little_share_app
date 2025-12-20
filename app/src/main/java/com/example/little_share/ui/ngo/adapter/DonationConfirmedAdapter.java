package com.example.little_share.ui.ngo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.little_share.R;
import com.example.little_share.data.models.Donation;
import com.example.little_share.data.models.DonationItem;
import com.example.little_share.data.repositories.DonationRepository;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DonationConfirmedAdapter extends RecyclerView.Adapter<DonationConfirmedAdapter.ViewHolder> {

    private Context context;
    private List<Donation> donations;
    private OnDonationActionListener listener;
    private DonationRepository donationRepository;

    public interface OnDonationActionListener {
        void onConfirm(Donation donation);
        void onReject(Donation donation);
        void onMarkReceived(Donation donation);
    }

    public DonationConfirmedAdapter(Context context, List<Donation> donations) {
        this.context = context;
        this.donations = donations;
        this.donationRepository = new DonationRepository();
    }

    public void setOnDonationActionListener(OnDonationActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donation_confirmed, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Donation donation = donations.get(position);

        holder.tvDonorName.setText(donation.getUserName());
        holder.tvPoints.setText(donation.getPointsEarned() + " điểm");

        // Load donation items để hiển thị chi tiết
        loadDonationItems(donation.getId(), holder);

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(donation.getDonationDate()));

        // Handle status and buttons
        Donation.DonationStatus status = donation.getStatusEnum();
        setupStatusAndButtons(holder, donation, status);
    }

    private void loadDonationItems(String donationId, ViewHolder holder) {
        donationRepository.getDonationItems(donationId, new DonationRepository.OnDonationItemsListener() {
            @Override
            public void onSuccess(List<DonationItem> items) {
                // Format item info từ DonationItems
                StringBuilder itemInfo = new StringBuilder();
                int totalQuantity = 0;

                for (int i = 0; i < items.size(); i++) {
                    DonationItem item = items.get(i);
                    if (i > 0) itemInfo.append(", ");

                    // ← FIX: Dùng getConditionEnum()
                    String conditionText = item.getConditionEnum() != null ?
                            item.getConditionEnum().getDisplayName() : "Không rõ";

                    itemInfo.append(item.getCategory())
                            .append(" (").append(item.getQuantity()).append(") - ")
                            .append(conditionText);
                    totalQuantity += item.getQuantity();
                }

                if (items.isEmpty()) {
                    itemInfo.append("Không có thông tin chi tiết");
                } else {
                    itemInfo.append(" • Tổng: ").append(totalQuantity).append(" món");
                }

                holder.tvItemInfo.setText(itemInfo.toString());

                // Hiển thị notes nếu có
                if (!items.isEmpty() && items.get(0).getNotes() != null && !items.get(0).getNotes().isEmpty()) {
                    holder.tvNote.setVisibility(View.VISIBLE);
                    holder.tvNote.setText("Ghi chú: " + items.get(0).getNotes());
                } else {
                    holder.tvNote.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("DonationAdapter", "Error loading items: " + error);
                holder.tvItemInfo.setText("Lỗi tải thông tin: " + error);
            }
        });
    }

    private void setupStatusAndButtons(ViewHolder holder, Donation donation, Donation.DonationStatus status) {
        // Reset visibility
        holder.tvStatusPending.setVisibility(View.GONE);
        holder.tvStatusReceived.setVisibility(View.GONE);
        holder.btnConfirm.setVisibility(View.GONE);

        switch (status) {
            case PENDING:
                holder.tvStatusPending.setVisibility(View.VISIBLE);
                holder.tvStatusPending.setText("Chờ xác nhận");
                holder.btnConfirm.setVisibility(View.VISIBLE);
                holder.btnConfirm.setText("Xác nhận / Từ chối");
                holder.btnConfirm.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onConfirm(donation);
                    }
                });
                break;

            case CONFIRMED:
                holder.tvStatusReceived.setVisibility(View.VISIBLE);
                holder.tvStatusReceived.setText("Đã xác nhận");
                holder.tvStatusReceived.setBackgroundResource(R.drawable.bg_badge_points);
                holder.btnConfirm.setVisibility(View.VISIBLE);
                holder.btnConfirm.setText("Xác nhận nhận đồ");
                holder.btnConfirm.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onMarkReceived(donation);
                    }
                });
                break;

            case RECEIVED:
                holder.tvStatusReceived.setVisibility(View.VISIBLE);
                holder.tvStatusReceived.setText("Đã nhận");
                holder.tvStatusReceived.setBackgroundResource(R.drawable.bg_button_gradient);
                break;

            case REJECTED:
                holder.tvStatusReceived.setVisibility(View.VISIBLE);
                holder.tvStatusReceived.setText("Đã từ chối");
                holder.tvStatusReceived.setBackgroundResource(R.drawable.bg_badge_hot);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return donations.size();
    }

    public void updateDonations(List<Donation> newDonations) {
        this.donations = newDonations;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDonorName, tvPoints, tvItemInfo, tvDate, tvNote;
        TextView tvStatusReceived, tvStatusPending;
        Button btnConfirm;

        ViewHolder(View itemView) {
            super(itemView);
            tvDonorName = itemView.findViewById(R.id.tvDonorName);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvItemInfo = itemView.findViewById(R.id.tvItemInfo);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvStatusReceived = itemView.findViewById(R.id.tvStatusReceived);
            tvStatusPending = itemView.findViewById(R.id.tvStatusPending);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
        }
    }
}
