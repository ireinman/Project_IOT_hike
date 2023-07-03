package com.example.iotProject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import org.apache.commons.lang3.text.WordUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class AchievementsScreen extends AppCompatActivity {
    private DatabaseReference dataBase;
    private FirebaseUser currentUser;
    private LinearLayout cardsLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acheivements);
        cardsLayout = findViewById(R.id.card_layout);
        Button goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
            startActivity(intent);
            finish();
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                startActivity(intent);
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dataBase = FirebaseDatabase.getInstance("https://iot-project-e6e76-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        if(currentUser != null){
            updateStreaks();
            DatabaseReference userTrainingReference = dataBase.child("achievements");
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String achievementName = childSnapshot.getKey();
                        int progress = childSnapshot.child("users/"+currentUser.getUid()).getValue(Integer.class);
                        int cap = childSnapshot.child("cap").getValue(Integer.class);
                        addCard(achievementName, progress, cap);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("Firebase", "loadPost:onCancelled", databaseError.toException());
                }
            };
            userTrainingReference.addListenerForSingleValueEvent(valueEventListener);
        }
    }
    private void updateStreaks(){
        ArrayList<TrainingSession> trainingSessions = AdvancedStatistics.getTrainings();
        if(currentUser!=null) {
            String uid = currentUser.getUid();
            int bestStreak = calculateHighestStreak(trainingSessions);
            updateReference(dataBase.child("achievements/7-day-streak/users/"+uid), bestStreak);
            updateReference(dataBase.child("achievements/30-day-streak/"+uid), bestStreak);
        }
    }

    private void updateReference(DatabaseReference dataBase, int maxStreak){
        dataBase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // Get the current value
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    // Value doesn't exist, set it to 1
                    mutableData.setValue(0);
                } else {
                    if(currentValue<maxStreak){
                        mutableData.setValue(maxStreak);
                    }
                }

                // Indicate that the transaction completed successfully
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    // Handle the error
                    Log.d("Firebase", "Transaction failed. Error: " + databaseError.getMessage());
                } else {
                    // Transaction completed successfully
                    Log.d("Firebase","Value incremented successfully");
                }
            }
        });
    }

    @SuppressLint("NewApi")
    public int calculateHighestStreak(ArrayList<TrainingSession> trainingSessions) {
        // Sort the trainingSessions by date
        trainingSessions.sort(Comparator.comparing(TrainingSession::reverseDateObject));

        int highestStreak = 0;
        int currentStreak = 0;

        Date previousDate = null;

        for (TrainingSession session : trainingSessions) {
            Date currentDate = session.reverseDateObject();

            if (previousDate == null || isConsecutiveDays(previousDate, currentDate)) {
                // The current session is part of the streak
                currentStreak++;
            } else {
                // The streak is broken, check if it's the highest streak so far
                highestStreak = Math.max(highestStreak, currentStreak);
                currentStreak = 1;
            }

            previousDate = currentDate;
        }

        // Check if the last streak is the highest streak
        highestStreak = Math.max(highestStreak, currentStreak);

        return highestStreak;
    }
    private boolean isConsecutiveDays(Date previousDate, Date currentDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String previousDateString = dateFormat.format(previousDate);
        String currentDateString = dateFormat.format(currentDate);

        long dayDifference = getDayDifference(previousDateString, currentDateString);

        return dayDifference == 1;
    }
    private long getDayDifference(String startDate, String endDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            long difference = end.getTime() - start.getTime();
            return difference / (24 * 60 * 60 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    private void addCard(String name, int progress, int cap) {
        final View view = getLayoutInflater().inflate(R.layout.achievement_card, null);

        TextView nameView = view.findViewById(R.id.achievementName);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView progressText = view.findViewById(R.id.progressText);
        nameView.setText(WordUtils.capitalize(name.replace("-", " ")));
        progressBar.setMax(cap);
        progressBar.setProgress(progress);
        String text = progress +"/"+cap;
        progressText.setText(text);
        cardsLayout.addView(view);
    }
}
