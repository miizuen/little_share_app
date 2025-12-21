package com.example.little_share.ui.ngo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.ReportExpense;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseActionListener {
        void onEdit(ReportExpense expense, int position);
        void onDelete(ReportExpense expense, int position);
    }

    private List<ReportExpense> expenseList;
    private OnExpenseActionListener listener;
    private boolean isReadOnly;

    // Constructor for editable mode
    public ExpenseAdapter(List<ReportExpense> expenseList, OnExpenseActionListener listener) {
        this.expenseList = expenseList;
        this.listener = listener;
        this.isReadOnly = (listener == null);
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isReadOnly ? R.layout.item_expense_readonly : R.layout.item_expense;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ReportExpense expense = expenseList.get(position);
        holder.bind(expense, position);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategory, tvAmount, tvNote, tvDate;
        private ImageView btnEdit, btnDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvDate = itemView.findViewById(R.id.tvDate);

            // Only find edit/delete buttons if not in read-only mode
            if (!isReadOnly) {
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }

        public void bind(ReportExpense expense, int position) {
            tvCategory.setText(expense.getCategory());

            // Format amount
            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedAmount = currencyFormat.format(expense.getAmount()) + " đ";
            tvAmount.setText(formattedAmount);

            tvNote.setText(expense.getNotes() != null ? expense.getNotes() : "");

            // Format date
            if (expense.getExpenseDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvDate.setText(dateFormat.format(expense.getExpenseDate()));
            }

            // Setup action buttons only in editable mode
            if (!isReadOnly && listener != null) {
                btnEdit.setOnClickListener(v -> listener.onEdit(expense, position));
                btnDelete.setOnClickListener(v -> listener.onDelete(expense, position));
            }
        }
    }
}