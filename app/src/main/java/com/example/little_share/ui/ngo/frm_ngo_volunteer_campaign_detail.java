package com.example.little_share.ui.ngo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.data.models.VolunteerRegistration;
import com.example.little_share.ui.ngo.adapter.VolunteerRegistrationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class frm_ngo_volunteer_campaign_detail extends Fragment implements VolunteerRegistrationAdapter.OnActionListener {

    private static final String TAG = "VolunteerList";

    private RecyclerView rvPendingList;
    private TextView tvTitle;
    private VolunteerRegistrationAdapter adapter;
    private List<VolunteerRegistration> registrationList = new ArrayList<>();
    private FirebaseFirestore db;
    private String campaignId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frm_ngo_volunteer_campaign_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            campaignId = getArguments().getString("campaignId");
        }

        initViews(view);
        setupRecyclerView();
        loadRegistrations();
    }

    private void initViews(View view) {
        rvPendingList = view.findViewById(R.id.rvPendingList);
        tvTitle = view.findViewById(R.id.tvTitle);
    }

    private void setupRecyclerView() {
        adapter = new VolunteerRegistrationAdapter(getContext(), registrationList, this);
        rvPendingList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPendingList.setAdapter(adapter);
    }

    private void loadRegistrations() {
        if (campaignId == null) {
            Log.e(TAG, "campaignId is null");
            return;
        }

        Log.d(TAG, "Loading for campaignId: " + campaignId);

        db.collection("volunteer_registrations")
                .whereEqualTo("campaignId", campaignId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    registrationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        VolunteerRegistration reg = doc.toObject(VolunteerRegistration.class);
                        reg.setId(doc.getId());
                        registrationList.add(reg);
                    }
                    adapter.notifyDataSetChanged();
                    updateTitle();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error: " + e.getMessage()));
    }

    @Override
    public void onApprove(VolunteerRegistration registration, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận duyệt")
                .setMessage("Duyệt đăng ký của " + registration.getUserName() + "?")
                .setPositiveButton("Duyệt", (d, w) -> approveRegistration(registration, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onReject(VolunteerRegistration registration, int position) {
        EditText etReason = new EditText(getContext());
        etReason.setHint("Nhập lý do từ chối...");
        etReason.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(getContext())
                .setTitle("Từ chối đăng ký")
                .setView(etReason)
                .setPositiveButton("Từ chối", (d, w) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) reason = "Không đủ điều kiện";
                    rejectRegistration(registration, position, reason);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void approveRegistration(VolunteerRegistration registration, int position) {
        db.collection("volunteer_registrations")
                .document(registration.getId())
                .update("status", "approved", "approvedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã duyệt!", Toast.LENGTH_SHORT).show();
                    adapter.removeItem(position);
                    updateTitle();
                });
    }

    private void rejectRegistration(VolunteerRegistration registration, int position, String reason) {
        db.collection("volunteer_registrations")
                .document(registration.getId())
                .update("status", "rejected", "rejectionReason", reason, "rejectedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã từ chối!", Toast.LENGTH_SHORT).show();
                    adapter.removeItem(position);
                    updateTitle();
                });
    }

    private void updateTitle() {
        tvTitle.setText(registrationList.isEmpty()
                ? "Chưa có đơn đăng ký nào"
                : "Đơn đăng ký chờ duyệt (" + registrationList.size() + ")");
    }
}
