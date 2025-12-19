package com.example.little_share.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.little_share.R;

public class activity_volunteer_detail_calendar extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvEventTitle, tvDate, tvTime, tvLocation, tvParticipants, tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_detail_calendar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadDataFromIntent();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvLocation = findViewById(R.id.tvLocation);
        tvParticipants = findViewById(R.id.tvParticipants);
        tvDescription = findViewById(R.id.tvDescription);
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            tvEventTitle.setText(intent.getStringExtra("campaign_title"));
            tvDate.setText(intent.getStringExtra("date"));
            tvTime.setText(intent.getStringExtra("time"));
            // Set other data...
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}
