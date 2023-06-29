package com.example.iotProject;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PreviousWorkouts extends AppCompatActivity {
    private Button goBackButton;
    private DatabaseReference dataBase;
    private FirebaseUser currentUser;
    private ArrayList<TrainingPlan> trainingPlans;
    private LinearLayout cardsLayout;

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "ONSTART!!!!!!!");
        // Check if user is signed in (non-null) and update UI accordingly.
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "ONSTART!!!!!!!");
        setContentView(R.layout.prveious_workouts_activity);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dataBase = FirebaseDatabase.getInstance("https://iot-project-e6e76-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        goBackButton = findViewById(R.id.goBackButton);
        cardsLayout = findViewById(R.id.card_layout);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TrainSettings.class);
                startActivity(intent);
                finish();
            }
        });
        if(currentUser != null){
            DatabaseReference userTrainingReference = dataBase.child("training_plans/"+currentUser.getUid());
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "ONDATACHANGE1 !!!!!!!");
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String trainingName = childSnapshot.getKey();
                        Log.i(TAG, "ONDATACHANGE!!!!!!!");
                        // Access the training data fields
                        int setsAmount = childSnapshot.child("setsAmount").getValue(Integer.class);
                        int reps = childSnapshot.child("reps").getValue(Integer.class);

                        // Create a TrainingPlan object using the retrieved data
                        TrainingPlan trainingPlan = new TrainingPlan(trainingName, setsAmount, reps);

                        // Perform any necessary operations with the created TrainingPlan object
                        addCard(trainingPlan.getTrainingName(), trainingPlan.setsAmount, trainingPlan.reps);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                }
            };
            userTrainingReference.addListenerForSingleValueEvent(valueEventListener);
        }

    }
    private void addCard(String name, int setsAmount, int repsAmount) {
        final View view = getLayoutInflater().inflate(R.layout.training_card, null);

        TextView nameView = view.findViewById(R.id.workout_name);
        Button delete = view.findViewById(R.id.delete);
        TextView numSetsText = view.findViewById(R.id.sets_num_view);
        TextView numRepsText = view.findViewById(R.id.reps_num_view);

        nameView.setText(name);
        numSetsText.setText(Integer.toString(setsAmount));
        numRepsText.setText(Integer.toString(repsAmount));


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardsLayout.removeView(view);
//                if (currentUser != null) {
//                    DatabaseReference userTrainingReference = dataBase.child("training_plans/" + currentUser.getUid());
//                }
            }
        });

        cardsLayout.addView(view);
    }
}
