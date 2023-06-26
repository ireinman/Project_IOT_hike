package com.example.iotProject;

public class MyUser {
    public String userName;
    public String email;
    public Boolean rememberMe;

    public MyUser(){}
    public MyUser(String userName, String email, Boolean rememberMe){
        this.userName = userName;
        this.email = email;
        this.rememberMe = rememberMe;
    }
}
