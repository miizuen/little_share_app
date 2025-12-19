package com.example.little_share.ui.ngo.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.little_share.R;
import com.example.little_share.data.models.ReportExpense;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddSpendingDialog extends DialogFragment {

    public interface OnExpenseAddedListener {
        void onExpenseAdded(ReportExpense expense);
    }

    private EditText txtHangMuc, txtSoTien, txtGhiChu;
    private LinearLayout btnPickDate;
    private TextView txtNgayChi;
    private Button btnCancel, btnAdd;
    private ImageView btnClose;

    private OnExpenseAddedListener listener;
    private Date selectedDate;
    private ReportExpense editingExpense;
    private boolean isEditMode = false;

    public static AddSpendingDialog newInstanceForEdit(ReportExpense expense) {
        AddSpendingDialog dialog = new AddSpendingDialog();
        dialog.editingExpense = expense;
        dialog.isEditMode = true;
        return dialog;
    }

    public void setOnExpenseAddedListener(OnExpenseAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_ngo_add_spending, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();

        if (isEditMode && editingExpense != null) {
            populateEditData();
        } else {
            // Set default date to today
            selectedDate = new Date();
            updateDateDisplay();
        }
    }

    private void initViews(View view) {
        txtHangMuc = view.findViewById(R.id.txtHangMuc);
        txtSoTien = view.findViewById(R.id.txtSoTien);
        txtGhiChu = view.findViewById(R.id.txtGhiChu);
        btnPickDate = view.findViewById(R.id.btnPickDate);
        txtNgayChi = view.findViewById(R.id.txtNgayChi);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnClose = view.findViewById(R.id.btnClose);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());
        btnAdd.setOnClickListener(v -> addOrUpdateExpense());
        btnPickDate.setOnClickListener(v -> showDatePicker());
    }

    private void populateEditData() {
        txtHangMuc.setText(editingExpense.getCategory());
        txtSoTien.setText(String.valueOf((long) editingExpense.getAmount()));
        txtGhiChu.setText(editingExpense.getNotes());
        selectedDate = editingExpense.getExpenseDate();
        updateDateDisplay();
        btnAdd.setText("Cập nhật");
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDate = selectedCalendar.getTime();
                    updateDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        if (selectedDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            txtNgayChi.setText(dateFormat.format(selectedDate));
            txtNgayChi.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    private void addOrUpdateExpense() {
        if (!validateInput()) return;

        String category = txtHangMuc.getText().toString().trim();
        String amountStr = txtSoTien.getText().toString().trim();
        String notes = txtGhiChu.getText().toString().trim();

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        ReportExpense expense;
        if (isEditMode && editingExpense != null) {
            expense = editingExpense;
        } else {
            expense = new ReportExpense();
        }

        expense.setCategory(category);
        expense.setAmount(amount);
        expense.setNotes(notes);
        expense.setExpenseDate(selectedDate);

        if (listener != null) {
            listener.onExpenseAdded(expense);
        }

        dismiss();
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(txtHangMuc.getText())) {
            Toast.makeText(getContext(), "Vui lòng nhập hạng mục", Toast.LENGTH_SHORT).show();
            txtHangMuc.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(txtSoTien.getText())) {
            Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            txtSoTien.requestFocus();
            return false;
        }

        if (selectedDate == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ngày chi", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
