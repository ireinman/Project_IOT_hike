package com.example.iotProject;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;

import java.text.ParseException;
import java.util.Date;

public class BSUSession {
    @SuppressLint("NewApi")
    private static  final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

//    private static final long BASE = 31; // A prime number
//    private static final long MOD = 1000000007; // A large prime number
//    public static long hash(Date date) {
//        long timestamp = date.getTime(); // Get the timestamp value of the date
//
//        long hashValue = 0;
//        while (timestamp != 0) {
//            hashValue = (hashValue * BASE + (timestamp % 10)) % MOD;
//            timestamp /= 10;
//        }
//
//        return hashValue;
//    }

//    public static Date reverseHash(long hashValue) {
//        long timestamp = 0;
//        long multiplier = 1;
//
//        while (hashValue != 0) {
//            timestamp = (timestamp + (hashValue % BASE) * multiplier) % MOD;
//            hashValue /= BASE;
//            multiplier *= 10;
//        }
//
//        return new Date(timestamp);
//    }
    public BSUSession(){}

    @SuppressLint("NewApi")
    public BSUSession(double totalTime, double explosiveness, Date date) {
        this.totalTime = totalTime;
        this.explosiveness = explosiveness;
        this.date = dateFormat.format(date);
    }

    public String returnDate(){
        return this.date;
    }
    @SuppressLint("NewApi")
    public Date reverseDateObject() {
        try {
            return dateFormat.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }
    @SuppressLint("NewApi")
    public static Date reverseHash(String str){
        try {
            return dateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }
    public void setDate(String date) { this.date = date; }

    public double totalTime;
    public double explosiveness;
    private String date;

    @Override
    @SuppressLint("NewApi")
    public String toString() {
        return  "Date: " + date + "\n" +
                "Total Time: " + ((float)Math.round(totalTime * 100)) / 100 + "\n" +
                "Explosiveness: " + ((float)Math.round(explosiveness * 100)) / 100 + "\n";
    }
}
