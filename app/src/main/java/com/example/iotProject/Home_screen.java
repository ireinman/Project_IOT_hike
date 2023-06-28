package com.example.iotProject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Home_screen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Button signOutButton = findViewById(R.id.goBackButton);
        signOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Log_in.class);
            startActivity(intent);
            finish();
        });

        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(intent);
            finish();
        });
        Button progressButton = findViewById(R.id.progressButton);
        progressButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Progress.class);
            startActivity(intent);
            finish();
        });

        Button trainButton = findViewById(R.id.trainButton);
        trainButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TrainSettings.class);
            startActivity(intent);
            finish();
        });

        Button bringUpButton = findViewById(R.id.bringUpButton);
        bringUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), BringUp.class);
            startActivity(intent);
            finish();
        });
    }
}