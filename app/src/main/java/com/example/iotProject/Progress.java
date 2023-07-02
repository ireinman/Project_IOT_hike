package com.example.iotProject;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;

public class Progress extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        CardView goBackButton = findViewById(R.id.goBackButton);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                startActivity(intent);
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        goBackButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
            startActivity(intent);
            finish();
        });
        CardView goAchievementsButton = findViewById(R.id.AchievementsButton);
        goAchievementsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AchievementsScreen.class);
            startActivity(intent);
            finish();
        });
        CardView goStatisticsButton = findViewById(R.id.statisticsButton);
        goStatisticsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdvancedStatistics.class);
            startActivity(intent);
            finish();
        });
        CardView goLeaderBoardsButton = findViewById(R.id.leaderBoardsButton);
        goLeaderBoardsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LeaderBoardsScreen.class);
            startActivity(intent);
            finish();
        });
    }
}