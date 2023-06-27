package com.example.iotProject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class Sign_up extends AppCompatActivity {
    private DatabaseReference dataBase;
    private Button createAccountButton;
    private Button backToLogInButton;
    private EditText passwordEditText;
    private EditText emailEditText;
    private FirebaseAuth mAuth;
    private EditText userNameEditText;
    private CheckBox rememberMeCheckBox;

    private void writeNewUser(String userId, String name, String email, Boolean rememberMe) {
        MyUser user = new MyUser(name, email, rememberMe, userId);
        dataBase.child("achievements").child("10-push-ups").child(userId).setValue(0);
        dataBase.child("achievements").child("50-push-ups").child(userId).setValue(0);
        dataBase.child("achievements").child("100-push-ups").child(userId).setValue(0);
        dataBase.child("achievements").child("7-day-streak").child(userId).setValue(0);
        dataBase.child("achievements").child("30-day-streak").child(userId).setValue(0);
        dataBase.child("achievements").child("first_training_session").child(userId).setValue(0);
        dataBase.child("users").child(userId).setValue(user);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        createAccountButton = findViewById(R.id.createAccountButton);
        rememberMeCheckBox = findViewById(R.id.rememberMe);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailEditText = findViewById(R.id.emailEditText);
        backToLogInButton = findViewById(R.id.goLoginButton);
        userNameEditText = findViewById(R.id.userNameEditText);
        dataBase = FirebaseDatabase.getInstance("https://iot-project-e6e76-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        backToLogInButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Log_in.class);
            startActivity(intent);
        });
        createAccountButton.setOnClickListener(v -> {
            String email, password, userName;
            email = String.valueOf(emailEditText.getText());
            password = String.valueOf(passwordEditText.getText());
            userName = String.valueOf(userNameEditText.getText());
            Boolean rememberMe = rememberMeCheckBox.isChecked();
            if(TextUtils.isEmpty(email)){
                Toast.makeText(Sign_up.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(password)){
                Toast.makeText(Sign_up.this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(Sign_up.this, "Account created.",
                                    Toast.LENGTH_SHORT).show();
                            writeNewUser(Objects.requireNonNull(task.getResult().getUser()).getUid(), userName, email, rememberMe);
                            Intent intent = new Intent(getApplicationContext(), Log_in.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            FirebaseAuthException e = (FirebaseAuthException )task.getException();
                            Log.e("LoginActivity", "Failed Registration", e);
                            Toast.makeText(Sign_up.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}