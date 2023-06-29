package com.example.iotProject;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MyUser {
    private String Uid;
    public String userName;
    public String email;
    public Boolean rememberMe;
    public MyUser(){}
    public MyUser(String userName, String email, Boolean rememberMe, String Uid){
        this.userName = userName;
        this.email = email;
        this.rememberMe = rememberMe;
        this.Uid = Uid;
    }
}
