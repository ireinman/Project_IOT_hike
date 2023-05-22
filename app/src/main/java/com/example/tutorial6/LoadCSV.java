package com.example.tutorial6;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_csv);
        Button BackButton = (Button) findViewById(R.id.button_back);
        LineChart lineChart = (LineChart) findViewById(R.id.line_chart);

        ArrayList<String[]> csvData = new ArrayList<>();

        csvData = CsvRead("/sdcard/csv_dir/hike_data.csv");

        LineDataSet lineDataSet1 =  new LineDataSet(DataValues(csvData, 1),"X Acceleration");
        LineDataSet lineDataSet2 =  new LineDataSet(DataValues(csvData, 2),"Y Acceleration");
        LineDataSet lineDataSet3 =  new LineDataSet(DataValues(csvData, 3),"Z Acceleration");
        lineDataSet1.setColor(Color.RED);
        lineDataSet1.setColor(Color.BLUE);
        lineDataSet1.setColor(Color.GREEN);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet1);
        dataSets.add(lineDataSet2);
        dataSets.add(lineDataSet3);
        LineData data = new LineData(dataSets);
        lineChart.setData(data);
        lineChart.invalidate();




        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickBack();
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

}