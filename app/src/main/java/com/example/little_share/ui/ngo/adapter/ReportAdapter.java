package com.example.little_share.ui.ngo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.FinancialReport;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private Context context;
    private List<FinancialReport> reports;
    private OnReportClickListener listener;

    public interface OnReportClickListener {
        void onReportClick(FinancialReport report);
        void onDetailClick(FinancialReport report);
    }

    public ReportAdapter(Context context, List<FinancialReport> reports, OnReportClickListener listener) {
        this.context = context;
        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ngo_finance_campaign_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FinancialReport report = reports.get(position);

        holder.tvEventName.setText(report.getCampaignName());
        holder.tvCategory.setText("Báo cáo tài chính");

        // Total expense
        DecimalFormat formatter = new DecimalFormat("#,###");
        String totalExpense = formatter.format(report.getTotalExpense()) + "vnđ";
        holder.tvTotalExpense.setText(totalExpense);

        // Volunteers
        holder.tvVolunteers.setText(report.getTotalVolunteers() + " người");

        // Spent (same as total expense for now)
        holder.tvSpent.setText(totalExpense);

        // Click listeners
        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailClick(report);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReportClick(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reports != null ? reports.size() : 0;
    }

    public void updateData(List<FinancialReport> newReports) {
        this.reports = newReports;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvCategory, tvTotalExpense, tvVolunteers, tvSpent;
        Button btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTotalExpense = itemView.findViewById(R.id.tvTotalExpense);
            tvVolunteers = itemView.findViewById(R.id.tvVolunteers);
            tvSpent = itemView.findViewById(R.id.tvSpent);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}