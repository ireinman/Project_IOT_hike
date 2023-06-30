package com.example.iotProject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TrainSettings extends AppCompatActivity {
    private Button goBackButton;
    private CardView goPreviousWorkouts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_settings);
        goBackButton = findViewById(R.id.goBackButton);
        goPreviousWorkouts = findViewById(R.id.previousWorkoutsCardView);
        CardView goNewWorkout = findViewById(R.id.newWorkoutCard);
        goNewWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), NewWorkout.class);
            startActivity(intent);
            finish();
        });
        goPreviousWorkouts.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PreviousWorkouts.class);
            startActivity(intent);
            finish();
        });
        goBackButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Home_screen.class);
            startActivity(intent);
            finish();
        });
    }
}