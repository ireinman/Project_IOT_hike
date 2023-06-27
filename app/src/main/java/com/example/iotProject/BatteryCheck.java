package com.example.iotProject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

//מחלקת עזר בשביל ההתראה על סוללה חלשה
public class BatteryCheck extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int level = intent.getIntExtra("level", 0);//קבלה של האחוזים בסוללה

        if(level < 95)
        {
            Toast.makeText(context, "Battery Low", Toast.LENGTH_LONG).show();//הודעה על סוללה חלשה
        }
    }

}

