package com.example.little_share.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.little_share.R;
import com.example.little_share.ui.ngo.activity_ngo_main;
import com.example.little_share.ui.sponsor.activity_sponsor_main;
import com.example.little_share.ui.volunteer.activity_volunteer_main;

public class role_selection extends AppCompatActivity {
    TextView tvVolunteer, tvSponsor, tvNGO;
    CardView card_volunteer, card_sponsor, card_NGO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_role_selection);

        tvVolunteer = findViewById(R.id.tvVolunteer);
        tvNGO = findViewById(R.id.tvNGO);
        tvSponsor = findViewById(R.id.tvSponsor);
        card_volunteer = findViewById(R.id.card_volunteer);
        card_sponsor = findViewById(R.id.card_sponsor);
        card_NGO = findViewById(R.id.card_organization);

        Shader shader_volunteer = new LinearGradient(
                0, 0, 0, tvVolunteer.getTextSize(),
                new int[]{
                        Color.parseColor("#FB923D"),
                        Color.parseColor("#FB7973"),
                        Color.parseColor("#FB7283")
                },
                null,
                Shader.TileMode.CLAMP);
        tvVolunteer.getPaint().setShader(shader_volunteer);
        tvVolunteer.invalidate();

        Shader shader_sponsor = new LinearGradient(
                0, 0, 0, tvVolunteer.getTextSize(),
                new int[]{
                        Color.parseColor("#09D2A0"),
                        Color.parseColor("#71CA67")
                },
                null,
                Shader.TileMode.CLAMP);
        tvSponsor.getPaint().setShader(shader_sponsor);
        tvSponsor.invalidate();

        Shader shader_NGO = new LinearGradient(
                0, 0, 0, tvVolunteer.getTextSize(),
                new int[]{
                        Color.parseColor("#6465F1"),
                        Color.parseColor("#A755F7")
                },
                null,
                Shader.TileMode.CLAMP);
        tvNGO.getPaint().setShader(shader_NGO);
        tvNGO.invalidate();

        card_volunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(role_selection.this, signup_volunteer.class);
                startActivity(intent);
            }
        });

        card_sponsor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(role_selection.this, activity_signup_sponsor.class);
                startActivity(intent);
            }
        });

        card_NGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(role_selection.this, signup_ngo.class);
                startActivity(intent);
            }
        });


        // Xử lý edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
