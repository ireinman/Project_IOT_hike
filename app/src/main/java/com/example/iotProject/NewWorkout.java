package com.example.iotProject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class NewWorkout extends AppCompatActivity {
    private AlertDialog dialog;
    private TrainingPlan tp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_workout_activity);
        Button goBack = findViewById(R.id.goBackButton);
        buildDialog();
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TrainSettings.class);
                startActivity(intent);
                finish();
            }
        });
        CardView beginnerCard = findViewById(R.id.begginerWorkoutButton);
        beginnerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tp = new TrainingPlan("Beginner Workout", 3, 10);
                Intent intent = new Intent(getApplicationContext(), In_Training.class);
                intent.putExtra("trainingPlan", tp);
                startActivity(intent);
                finish();
            }
        });
        CardView intermediateCard = findViewById(R.id.intermediateWorkoutButton);
        intermediateCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tp = new TrainingPlan("intermediate Workout", 5, 15);
                Intent intent = new Intent(getApplicationContext(), In_Training.class);
                intent.putExtra("trainingPlan", tp);
                startActivity(intent);
                finish();
            }
        });
        CardView advancedCard = findViewById(R.id.advancedWorkoutButton);
        advancedCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tp = new TrainingPlan("advanced Workout", 5, 15);
                Intent intent = new Intent(getApplicationContext(), In_Training.class);
                intent.putExtra("trainingPlan", tp);
                startActivity(intent);
                finish();
            }
        });

        CardView customCard = findViewById(R.id.customWorkoutButton);
        customCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                Intent intent = new Intent(getApplicationContext(), In_Training.class);
                intent.putExtra("trainingPlan", tp);
                startActivity(intent);
                finish();
            }
        });
    }
    private void buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.custom_training_dialog, null);

        final EditText setsText = view.findViewById(R.id.setsEdit);
        final EditText repsText = view.findViewById(R.id.repsEdit);
        final EditText name = view.findViewById(R.id.nameEdit);

        builder.setView(view);
        builder.setTitle("Custom Training")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tp = new TrainingPlan(name.getText().toString(),
                                Integer.parseInt(setsText.getText().toString()),
                                Integer.parseInt(repsText.getText().toString()));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        dialog = builder.create();
    }
    private void writeTPToDataBase(){

    }
}
