package com.example.iotProject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Home_screen extends AppCompatActivity {
    private AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        buildDialog();
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                dialog.show();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
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

    private void buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sign out?");
        builder.setPositiveButton("Ok", (dialog, id) -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Log_in.class);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> {
            return;
        });
        dialog = builder.create();
    }
}