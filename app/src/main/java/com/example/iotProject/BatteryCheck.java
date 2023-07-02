package com.example.iotProject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BatteryCheck extends BroadcastReceiver {
    static boolean notify = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        int level = intent.getIntExtra("level", 0);

        if(level <= 95 && notify)
        {
            Toast.makeText(context, "Battery Low", Toast.LENGTH_LONG).show();
            notify = false;
        }
    }

}

