package com.example.iotProject;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                buildDialog();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        Button signOutButton = findViewById(R.id.goBackButton);
        signOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LogIn.class);
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
            Intent intent = new Intent(getApplicationContext(), DeviceActivity.class);
            intent.putExtra("type", 1);
            startActivity(intent);
            finish();
        });
    }

    private void buildDialog() {
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(HomeScreen.this)
                .setMessage("Sign out?")
                .setNegativeButton("Ok", (dialog1, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LogIn.class);
                    startActivity(intent);
                    finish();
                })
                .setPositiveButton("Cancel", null)
                .create();
        dialog.show();
    }
}