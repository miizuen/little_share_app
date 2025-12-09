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
import com.example.little_share.data.models.Shift;

import java.util.List;

public class ShiftAdapter extends RecyclerView.Adapter<ShiftAdapter.ViewHolder> {
    private Context context;
    private List<Shift> shifts;
    private OnShiftActionListener listener;

    public ShiftAdapter(Context context, List<Shift> shifts, OnShiftActionListener listener) {
        this.context = context;
        this.shifts = shifts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShiftAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shift_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShiftAdapter.ViewHolder holder, int position) {
        Shift shift = shifts.get(position);
        holder.tvShiftName.setText(shift.getShiftName());
        holder.tvTimeRange.setText(shift.getTimeRange());
        holder.tvVolunteers.setText(shift.getMaxVolunteers() + "TNV");

        holder.btnEdit.setOnClickListener(v -> {
            if(listener != null){
                listener.onEditClick(shift, position);
            }
        });

        holder.btnDelete.setOnClickListener( v -> {
            if(listener != null){
                listener.onDeleteClick(shift, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return shifts.size();
    }

    public interface OnShiftActionListener {
        void onEditClick(Shift shift, int position);
        void onDeleteClick(Shift shift, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvShiftName, tvTimeRange, tvVolunteers;
        ImageView btnEdit, btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShiftName = itemView.findViewById(R.id.tvShiftName);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvVolunteers = itemView.findViewById(R.id.tvVolunteers);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
