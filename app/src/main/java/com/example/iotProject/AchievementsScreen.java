package com.example.iotProject;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import org.apache.commons.lang3.text.WordUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
            Intent intent = new Intent(getApplicationContext(), Progress.class);
            startActivity(intent);
            finish();
        });
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dataBase = FirebaseDatabase.getInstance("https://iot-project-e6e76-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        if(currentUser != null){
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
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                }
            };
            userTrainingReference.addListenerForSingleValueEvent(valueEventListener);
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
        progressText.setText(progress +"/"+cap);

        cardsLayout.addView(view);
    }
}
