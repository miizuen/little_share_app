package com.example.little_share.adapter.sponsor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.sponsor.CampaignSponsorModel;

import java.util.ArrayList;
import java.util.List;

public class CampaignSponsorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SPONSORED = 0;
    private static final int VIEW_TYPE_NEED_SPONSOR = 1;

    private List<CampaignSponsorModel> campaignList;
    private OnCampaignClickListener clickListener;

    public interface OnCampaignClickListener {
        void onButtonClick(CampaignSponsorModel campaign, int position);
        void onItemClick(CampaignSponsorModel campaign, int position);
    }

    public CampaignSponsorAdapter() {
        this.campaignList = new ArrayList<>();
    }

    public void setCampaignList(List<CampaignSponsorModel> campaignList) {
        this.campaignList = campaignList;
        notifyDataSetChanged();
    }

    public void setClickListener(OnCampaignClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return campaignList.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SPONSORED) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_campaign_sponsored, parent, false);
            return new SponsoredViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_campaign_need_sponsor, parent, false);
            return new NeedSponsorViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CampaignSponsorModel campaign = campaignList.get(position);

        if (holder instanceof SponsoredViewHolder) {
            ((SponsoredViewHolder) holder).bind(campaign, position);
        } else if (holder instanceof NeedSponsorViewHolder) {
            ((NeedSponsorViewHolder) holder).bind(campaign, position);
        }
    }

    @Override
    public int getItemCount() {
        return campaignList != null ? campaignList.size() : 0;
    }

    // ViewHolder cho chiến dịch đang tài trợ
    class SponsoredViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgCampaign;
        private TextView tvCategory, tvStatus, tvCampaignName;
        private TextView tvOrganization, tvLocation, tvDate;
        private TextView tvProgressNumber, tvMoney;
        private ProgressBar progressBar;
        private Button btnDetail;

        public SponsoredViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCampaign = itemView.findViewById(R.id.imgCampaign);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCampaignName = itemView.findViewById(R.id.tvCampaignName);
            tvOrganization = itemView.findViewById(R.id.tvOrganization);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvProgressNumber = itemView.findViewById(R.id.tvProgressNumber);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvMoney = itemView.findViewById(R.id.tvMoney);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }

        public void bind(CampaignSponsorModel campaign, int position) {
            if (campaign.getImageResId() != 0) {
                imgCampaign.setImageResource(campaign.getImageResId());
            }

            tvCategory.setText(campaign.getCategory());
            setTextColor(tvCategory, campaign.getCategoryColor(), "#FF6F00");

            tvStatus.setText(campaign.getStatus());
            setTextColor(tvStatus, campaign.getStatusColor(), "#1B6A07");

            tvCampaignName.setText(campaign.getCampaignName());
            tvOrganization.setText(campaign.getOrganization());
            tvLocation.setText(campaign.getLocation());
            tvDate.setText(campaign.getDate());
            tvProgressNumber.setText(campaign.getProgress() + "%");
            tvMoney.setText(campaign.getSponsoredAmount());

            if (progressBar != null) {
                progressBar.setProgress(campaign.getProgress());
            }

            btnDetail.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onButtonClick(campaign, position);
                }
            });

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(campaign, position);
                }
            });
        }
    }

    // ViewHolder cho chiến dịch cần tài trợ
    class NeedSponsorViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgCampaign;
        private TextView tvCategory, tvCampaignName, tvGroup;
        private TextView tvLocation, tvDate;
        private TextView tvTargetAmount, tvBeneficiaries, tvDuration;
        private TextView tvProgressText;
        private ProgressBar progressBar;
        private Button btnDonate;

        public NeedSponsorViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCampaign = itemView.findViewById(R.id.imgCampaign);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCampaignName = itemView.findViewById(R.id.tvCampaignName);
            tvGroup = itemView.findViewById(R.id.tvGroup);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDonate = itemView.findViewById(R.id.btnDonate);
            progressBar = itemView.findViewById(R.id.progressBar);

            findInfoBoxTextViews(itemView);
        }

        private void findInfoBoxTextViews(View root) {
            ViewGroup parent = (ViewGroup) root;
            findTextViewsRecursive(parent);
        }

        private void findTextViewsRecursive(ViewGroup parent) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child instanceof ViewGroup) {
                    findTextViewsRecursive((ViewGroup) child);
                } else if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    String text = tv.getText().toString();

                    if (text.equals("50M") && tvTargetAmount == null) {
                        tvTargetAmount = tv;
                    } else if (text.equals("1000") && tvBeneficiaries == null) {
                        tvBeneficiaries = tv;
                    } else if (text.equals("6 tháng") && tvDuration == null) {
                        tvDuration = tv;
                    } else if (text.contains("Đã huy động") && tvProgressText == null) {
                        tvProgressText = tv;
                    }
                }
            }
        }

        public void bind(CampaignSponsorModel campaign, int position) {
            if (campaign.getImageResId() != 0) {
                imgCampaign.setImageResource(campaign.getImageResId());
            }

            tvCategory.setText(campaign.getCategory());
            setTextColor(tvCategory, campaign.getCategoryColor(), "#FF6F00");

            tvCampaignName.setText(campaign.getCampaignName());
            tvGroup.setText(campaign.getGroup());
            tvLocation.setText(campaign.getLocation());
            tvDate.setText(campaign.getDate());

            if (tvTargetAmount != null) tvTargetAmount.setText(campaign.getTargetAmount());
            if (tvBeneficiaries != null) tvBeneficiaries.setText(campaign.getBeneficiaries());
            if (tvDuration != null) tvDuration.setText(campaign.getDuration());

            if (tvProgressText != null) {
                String progressText = "Đã huy động: " + campaign.getRaisedAmount() +
                        "/" + campaign.getTotalAmount();
                tvProgressText.setText(progressText);
            }

            if (progressBar != null) {
                progressBar.setProgress(campaign.getProgress());
            }

            btnDonate.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onButtonClick(campaign, position);
                }
            });

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(campaign, position);
                }
            });
        }
    }

    // Helper method để set màu
    private void setTextColor(TextView textView, String color, String defaultColor) {
        try {
            textView.setTextColor(Color.parseColor(color));
        } catch (Exception e) {
            textView.setTextColor(Color.parseColor(defaultColor));
        }
    }
}