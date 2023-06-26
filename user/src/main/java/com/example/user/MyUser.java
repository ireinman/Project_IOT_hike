package com.example.user;

public class MyUser {
    private String userName;
    private String email;
    private Boolean rememberMe;

    public MyUser(){}
    public MyUser(String userName, String email, Boolean rememberMe){
        this.userName = userName;
        this.email = email;
        this.rememberMe = rememberMe;
    }
}