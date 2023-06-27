package com.example.iotProject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class AdvancedStatistics extends AppCompatActivity {

    private int mode = 0;

    LineChart amountLineChart;
    LineDataSet amountDataSet;
    ArrayList<ILineDataSet> amountDataSets = new ArrayList<>();
    LineData amountData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_statistics);

        SwitchCompat switchMode = findViewById(R.id.switchMode);
        switchMode.setChecked(true);
        switchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mode = 1;
                // Switch is in the "on" or "checked" position
                // Perform actions for the "on" state
            } else {
                mode = 0;
                // Switch is in the "off" or "unchecked" position
                // Perform actions for the "off" state
            }
        });
    }

    private void setAmountPushUp (){

    }
}