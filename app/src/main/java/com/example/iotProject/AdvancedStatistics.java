package com.example.iotProject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AdvancedStatistics extends AppCompatActivity {

    private int mode = 0;
    private final TrainingSession[] trainings = getTraining();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_statistics);

        SwitchCompat switchMode = findViewById(R.id.switchMode);
        switchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mode = 1;
                switchMode.setText(R.string.push_up_training);
                // Switch is in the "on" or "checked" position
                // Perform actions for the "on" state
            } else {
                mode = 0;
                switchMode.setText(R.string.bsu_training);
                // Switch is in the "off" or "unchecked" position
                // Perform actions for the "off" state
            }
        });
        switchMode.setChecked(true);
    }

    private void setAmountPushUp(){
        LineChart amountLineChart = (LineChart) findViewById(R.id.lineChartAmount);
        ArrayList<Entry> amounts = new ArrayList<>();
        for (int i = 0; i < trainings.length; i++){
            amounts.add(new Entry(i, trainings[i].totalPushUps));
        }
        LineDataSet amountDataSet =  new LineDataSet(amounts, "Number Of Push Ups");
        amountDataSet.setColor(Color.RED);
        amountDataSet.setCircleColor(Color.RED);
        ArrayList<ILineDataSet> amountDataSets = new ArrayList<>();
        amountDataSets.add(amountDataSet);
        LineData amountData = new LineData(amountDataSets);
        amountLineChart.getDescription().setEnabled(false);
        amountLineChart.getLegend().setEnabled(false);
        amountLineChart.setData(amountData);
        amountLineChart.invalidate();
    }

    private void setTrainingsTime(){
        BarChart trainingTimeChart = (BarChart) findViewById(R.id.barChartTrainingsTime);
        ArrayList<BarEntry> hours = new ArrayList<>();
        Date temp;
        for (int i = 0; i < trainings.length; i++){
            temp = trainings[i].date;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(temp);
            hours.add(new BarEntry(calendar.get(Calendar.HOUR_OF_DAY), 1));
        }
        BarDataSet hoursDataSet =  new BarDataSet(hours, "Hours of trainings");
        hoursDataSet.setColor(Color.BLUE);
//        ArrayList<ILineDataSet> hourDataSets = new ArrayList<>();
//        hourDataSets.add(hoursDataSet);
        BarData timeData = new BarData();
        timeData.addDataSet(hoursDataSet);
        trainingTimeChart.getDescription().setEnabled(false);
        trainingTimeChart.getLegend().setEnabled(false);
        trainingTimeChart.setData(timeData);
        trainingTimeChart.invalidate();
    }


    private TrainingSession[] getTraining(){
        // TODO sort by time
        return null;
    }
}
