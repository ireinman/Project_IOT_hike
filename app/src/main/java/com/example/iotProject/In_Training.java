package com.example.iotProject;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class In_Training extends AppCompatActivity {
    private int progress;
    private ProgressBar progressBar;
    private TextView progressTextView;
    private String progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_training);

        ImageView imageView = findViewById(R.id.imageView);
        // Change this value to "pushdown" or "pushup" depends on current mode
        String mode = "pushup";

        progressTextView = findViewById(R.id.progressTextView);
        progressBar = findViewById(R.id.progressBar);
        progress = progressBar.getProgress();
        progressText = "Set " + progress + " out of " + progressBar.getMax();
        progressTextView.setText(progressText);

//         Here's how we will update progress during sessions
//         update_progress();

        if (mode.equals("pushup")) {
            imageView.setImageResource(R.drawable.pushup);
        } else if (mode.equals("pushdown")) {
            imageView.setImageResource(R.drawable.pushdown);
        }
    }

    private void update_progress(){
        progress += 1;
        progressBar.setProgress(progress);
        progressText = "Set " + progress + " out of " + progressBar.getMax();
        progressTextView.setText(progressText);
    }

}