package com.example.tutorial6;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import java.util.List;


public class LoadCSV extends AppCompatActivity {

    LineChart lineChart;
    Spinner mySpinner;
    TextView activityText, timeText, nameText, stepsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_csv);
        Button BackButton = (Button) findViewById(R.id.button_back);
        Button LoadButton = (Button) findViewById(R.id.load_csv);
        lineChart = (LineChart) findViewById(R.id.line_chart);
        nameText = (TextView) findViewById(R.id.fileNameText);
        timeText = (TextView) findViewById(R.id.experimentTimeText);
        activityText = (TextView) findViewById(R.id.activityTypeText);
        stepsText = (TextView) findViewById(R.id.countStepsText);

        File folder = new File("/sdcard/csv_dir/");
        File[] listOfFiles = folder.listFiles();
        String[] files_names = new String[listOfFiles.length];
        for (int i=0;i<listOfFiles.length;i++){
            files_names[i] = listOfFiles[i].getName();
        }
        mySpinner = (Spinner) findViewById(R.id.spinner2);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,R.layout.spinner_item,files_names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(adapter);




        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickBack();
            }
        });

        LoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }

    private void ClickBack(){
        finish();

    }

    private ArrayList<String[]> CsvRead(String path){
        ArrayList<String[]> CsvData = new ArrayList<>();
        try {
            File file = new File(path);
            CSVReader reader = new CSVReader(new FileReader(file));

            String[] newline = reader.readNext();
            String text = "File Name: " + newline[1];
            nameText.setText(text);
            newline = reader.readNext();
            text = "Experiment Time: " + newline[1];
            timeText.setText(text);
            newline = reader.readNext();
            text = "Activity Type: " + newline[1];
            activityText.setText(text);
            newline = reader.readNext();
            text = "Steps Count: " + newline[1];
            stepsText.setText(text);
            reader.readNext();
            reader.readNext();
            newline = reader.readNext();
            while(newline != null){
                CsvData.add(newline);
                newline = reader.readNext();
            }

        }catch (Exception e){}
        return CsvData;
    }

    private ArrayList<Entry> DataValues(ArrayList<String[]> csvData, int index){
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        for (int i = 0; i < csvData.size(); i++){

            dataVals.add(new Entry(Float.parseFloat(csvData.get(i)[0]),
                    Float.parseFloat(csvData.get(i)[index])));


        }

        return dataVals;
    }

    private void loadData(){
        ArrayList<String[]> csvData = new ArrayList<>();

        csvData = CsvRead("/sdcard/csv_dir/" + mySpinner.getSelectedItem().toString());

        LineDataSet lineDataSet1 =  new LineDataSet(DataValues(csvData, 1),"X Acceleration");
        LineDataSet lineDataSet2 =  new LineDataSet(DataValues(csvData, 2),"Y Acceleration");
        LineDataSet lineDataSet3 =  new LineDataSet(DataValues(csvData, 3),"Z Acceleration");
        lineDataSet1.setColor(Color.RED);
        lineDataSet2.setColor(Color.BLUE);
        lineDataSet3.setColor(Color.GREEN);
        lineDataSet1.setCircleColor(Color.RED);
        lineDataSet2.setCircleColor(Color.BLUE);
        lineDataSet3.setCircleColor(Color.GREEN);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet1);
        dataSets.add(lineDataSet2);
        dataSets.add(lineDataSet3);
        LineData data = new LineData(dataSets);
        lineChart.getDescription().setEnabled(false);
        lineChart.setData(data);
        lineChart.invalidate();
    }

}