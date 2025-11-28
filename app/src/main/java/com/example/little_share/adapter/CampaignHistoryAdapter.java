package com.example.little_share.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.CampaignHistoryModel;

import java.util.ArrayList;
import java.util.List;

public class CampaignHistoryAdapter extends RecyclerView.Adapter<CampaignHistoryAdapter.CampaignViewHolder> {

    private List<CampaignHistoryModel> campaignList;
    private OnCampaignClickListener clickListener;

    public interface OnCampaignClickListener {
        void onDetailClick(CampaignHistoryModel campaign, int position);
        void onItemClick(CampaignHistoryModel campaign, int position);
    }

    public CampaignHistoryAdapter() {
        this.campaignList = new ArrayList<>();
    }

    public void setCampaignList(List<CampaignHistoryModel> campaignList) {
        this.campaignList = campaignList;
        notifyDataSetChanged();
    }

    public void setClickListener(OnCampaignClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public CampaignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sponsor_campaign_history, parent, false);
        return new CampaignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignViewHolder holder, int position) {
        CampaignHistoryModel campaign = campaignList.get(position);
        holder.bind(campaign, position);
    }

    @Override
    public int getItemCount() {
        return campaignList != null ? campaignList.size() : 0;
    }

    class CampaignViewHolder extends RecyclerView.ViewHolder {
        private TextView campaignName;
        private TextView location;
        private TextView tvDate;
        private AppCompatButton btnDetail;

        public CampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            campaignName = itemView.findViewById(R.id.campaignName);
            location = itemView.findViewById(R.id.location);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }

        public void bind(CampaignHistoryModel campaign, int position) {
            campaignName.setText(campaign.getCampaignName());
            location.setText(campaign.getLocation());
            tvDate.setText(campaign.getDate());
            btnDetail.setText(campaign.getButtonText());

            // Tìm và cập nhật TextView hiển thị số tiền đóng góp
            View parent = (View) campaignName.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup container = (ViewGroup) parent;
                String donationText = "Đã đóng góp: " + campaign.getDonationAmount();

                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof TextView) {
                        TextView tv = (TextView) child;
                        String text = tv.getText().toString();
                        if (text.contains("Đã đóng góp")) {
                            tv.setText(donationText);
                            break;
                        }
                    }
                }
            }

            // Xử lý sự kiện click button
            btnDetail.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onDetailClick(campaign, position);
                }
            });

            // Xử lý sự kiện click item
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(campaign, position);
                }
            });
        }
    }
}