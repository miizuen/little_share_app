package com.example.little_share.ui.ngo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.Sponsor;

import java.util.List;
import java.util.Locale;

public class SponsorAdapter extends RecyclerView.Adapter<SponsorAdapter.ViewHolder> {
    private Context context;
    private List<Sponsor> sponsors;
    public SponsorAdapter(Context context, List<Sponsor> sponsors) {
        this.context = context;
        this.sponsors = sponsors;
    }


    @NonNull
    @Override
    public SponsorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sponsor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SponsorAdapter.ViewHolder holder, int position) {
        Sponsor sponsor = sponsors.get(position);
        holder.tvSponsorName.setText(sponsor.getName());
        holder.tvSponsorLocation.setText("Mỹ");
        holder.tvSponsorAmount.setText(String.valueOf(190000));
    }

    @Override
    public int getItemCount() {
        return sponsors.size();
    }

    private String formatMoney(double amount) {
        return String.format(Locale.getDefault(), "%,.0f", amount).replace(",", ".");
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSponsorName, tvSponsorLocation, tvSponsorAmount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSponsorName = itemView.findViewById(R.id.tvSponsorName);
            tvSponsorLocation = itemView.findViewById(R.id.tvSponsorLocation);
            tvSponsorAmount = itemView.findViewById(R.id.tvSponsorAmount);
        }
    }
}
