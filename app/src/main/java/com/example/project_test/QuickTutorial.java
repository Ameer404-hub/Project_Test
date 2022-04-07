package com.example.project_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class QuickTutorial extends AppCompatActivity {

    TextView howWorks, howProfile, howPlaceitem, howUpdateitem, howJoin, howSeeMembers, howMyitems, howMyitemsDesc;
    ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_tutorial);

        howWorks = findViewById(R.id.howWorks);
        howProfile = findViewById(R.id.howProfile);
        howPlaceitem = findViewById(R.id.howPlaceitem);
        howUpdateitem = findViewById(R.id.howUpdateitem);
        howJoin = findViewById(R.id.howJoin);
        howSeeMembers = findViewById(R.id.howSeeMembers);

        howMyitems = findViewById(R.id.howMyitems);
        howMyitemsDesc = findViewById(R.id.howMyitems_decs);

        howMyitems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (howMyitemsDesc.getVisibility() != View.VISIBLE)
                    howMyitemsDesc.setVisibility(View.VISIBLE);
                else
                    howMyitemsDesc.setVisibility(View.GONE);
            }
        });

        backArrow = findViewById(R.id.goBack);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goBack = new Intent(getApplicationContext(), HomeScreen.class);
                startActivity(goBack);
                finish();
            }
        });
    }

    public void onBackPressed() {
        Intent goBack = new Intent(getApplicationContext(), HomeScreen.class);
        startActivity(goBack);
        finish();
    }
}