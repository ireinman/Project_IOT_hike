package com.example.iotProject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class DeviceActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    public static String deviceAddress;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (ActivityCompat.checkSelfPermission(DeviceActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DeviceActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 0);
        }
        if (ActivityCompat.checkSelfPermission(DeviceActivity.this, android.Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DeviceActivity.this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 0);
        }
        if (ActivityCompat.checkSelfPermission(DeviceActivity.this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DeviceActivity.this, new String[]{Manifest.permission.BLUETOOTH}, 0);
        }
        if (ActivityCompat.checkSelfPermission(DeviceActivity.this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DeviceActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 0);
        }

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putInt("type", getIntent().getIntExtra("type", 0));
            args.putSerializable("trainingPlan", getIntent().getSerializableExtra("trainingPlan"));
            Fragment fragment = new DevicesFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, fragment, "devices").commit();
        }
        else
            onBackStackChanged();

    }

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
