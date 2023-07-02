package com.example.iotProject;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LogIn extends AppCompatActivity {

    private DatabaseReference dataBase;
    FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox rememberMeCheckBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();
        registerReceiver(new BatteryCheck(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        Button loginButton = findViewById(R.id.logInButton);
        Button signUpButton = findViewById(R.id.signUpButton);

        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUp.class);
            startActivity(intent);
            finish();
        });
        loginButton.setOnClickListener(v -> {
            String email, password;
            email = String.valueOf(emailEditText.getText());
            password = String.valueOf(passwordEditText.getText());
            if(TextUtils.isEmpty(email)){
                Toast.makeText(LogIn.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(password)){
                Toast.makeText(LogIn.this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), " Login Successful", Toast.LENGTH_SHORT).show();
                            currentUser = Objects.requireNonNull(task.getResult().getUser());
                            dataBase.child("users/" + currentUser.getUid() + "/rememberMe").setValue(rememberMeCheckBox.isChecked());
                            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LogIn.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {}};
        this.getOnBackPressedDispatcher().addCallback(this, callback);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        dataBase = FirebaseDatabase.getInstance("https://iot-project-e6e76-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        if(currentUser != null){
            dataBase.child("users").child(currentUser.getUid()).child("rememberMe").get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    if((Boolean)(Objects.requireNonNull(task.getResult().getValue()))){
                        Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
    }


}