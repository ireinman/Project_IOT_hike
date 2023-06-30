package com.example.iotProject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Progress extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        CardView goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Home_screen.class);
                startActivity(intent);
                finish();
            }
        });
        CardView goAcheivementsButton = findViewById(R.id.AchievementsButton);
        goAcheivementsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AcheivementsScreen.class);
                startActivity(intent);
                finish();
            }
        });
        CardView goStatisticsButton = findViewById(R.id.statisticsButton);
        goStatisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AdvancedStatistics.class);
                startActivity(intent);
                finish();
            }
        });
        CardView goLeaderBoardsButton = findViewById(R.id.leaderBoardsButton);
        goLeaderBoardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LeaderBoardsScreen.class);
                startActivity(intent);
                finish();
            }
        });
    }
}