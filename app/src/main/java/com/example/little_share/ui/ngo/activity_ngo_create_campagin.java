package com.example.little_share.ui.ngo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;

public class activity_ngo_create_campagin extends AppCompatActivity {
    CardView cardDonationCampaign, cardVolunteerCampaign;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ngo_create_campagin);

        cardDonationCampaign = findViewById(R.id.cardDonationCampaign);
        cardVolunteerCampaign = findViewById(R.id.cardVolunteerCampaign);
        btnBack = findViewById(R.id.btnBack);

        cardVolunteerCampaign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_ngo_create_campagin.this, activity_ngo_create_campaign_form.class);
                startActivity(intent);
            }
        });

        cardDonationCampaign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_ngo_create_campagin.this, activity_ngo_create_donation.class);
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}