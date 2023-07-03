package com.example.iotProject;

import static android.content.ContentValues.TAG;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
        // Check if user is signed in (non-null) and update UI accordingly.
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.previous_workouts_activity);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getApplicationContext(), TrainSettings.class);
                startActivity(intent);
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dataBase = FirebaseDatabase.getInstance("https://iot-project-e6e76-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        goBackButton = findViewById(R.id.goBackButton);
        cardsLayout = findViewById(R.id.card_layout);
        goBackButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TrainSettings.class);
            startActivity(intent);
            finish();
        });
        if(currentUser != null){
            DatabaseReference userTrainingReference = dataBase.child("training_plans/"+currentUser.getUid());
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String trainingName = childSnapshot.getKey();
                        // Access the training data fields
                        int setsAmount = childSnapshot.child("setsAmount").getValue(Integer.class);
                        int reps = childSnapshot.child("reps").getValue(Integer.class);

                        // Create a TrainingPlan object using the retrieved data
                        TrainingPlan trainingPlan = new TrainingPlan(trainingName, setsAmount, reps);

                        // Perform any necessary operations with the created TrainingPlan object
                        addCard(trainingPlan);
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
    private void addCard(TrainingPlan trainingPlan) {
        final View view = getLayoutInflater().inflate(R.layout.training_card, null);

        TextView nameView = view.findViewById(R.id.workout_name);
        Button delete = view.findViewById(R.id.delete);
        TextView numSetsText = view.findViewById(R.id.num_sets_text_view);
        TextView numRepsText = view.findViewById(R.id.repetitions_per_set);
        CardView card = view.findViewById(R.id.card_view);
        card.setOnClickListener(view1 -> {
            Intent intent = new Intent(getApplicationContext(), DeviceActivity.class);
            intent.putExtra("trainingPlan", trainingPlan);
            intent.putExtra("type", 0);
            startActivity(intent);
            finish();
            });

        nameView.setText(trainingPlan.getTrainingName());
        String text = "Repetitions per set: " + trainingPlan.setsAmount;
        numSetsText.setText(text);
        text = "Number of sets: " + trainingPlan.reps;
        numRepsText.setText(text);


        delete.setOnClickListener(v -> {
            cardsLayout.removeView(view);
            if (currentUser != null) {
                dataBase.child("training_plans/" + currentUser.getUid() + "/" +
                        trainingPlan.getTrainingName()).removeValue();
            }
        });
        cardsLayout.addView(view);
    }
}
