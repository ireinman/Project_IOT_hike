package com.example.iotProject;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;

import java.text.ParseException;
import java.util.Date;

public class TrainingSession {
//    private static final long BASE = 31; // A prime number
//    private static final long MOD = 1000000007; // A large prime number
    @SuppressLint("NewApi")
    private static  final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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
    public TrainingSession(){}

    @SuppressLint("NewApi")
    public TrainingSession(int totalPushUps, String trainingName, int totalSets, float explosiveness, float avgPushUpTime, Date date) {
        this.totalPushUps = totalPushUps;
        this.trainingName = trainingName;
        this.totalSets = totalSets;
        this.explosiveness = explosiveness;
        this.avgPushUpTime = avgPushUpTime;
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

    public int totalPushUps;
    public String trainingName;
    public int totalSets;
    public double explosiveness;
    public double avgPushUpTime;
    private String date;

    @Override
    @SuppressLint("NewApi")
    public String toString() {
        return  "Workout Name: " + trainingName + "\n" +
                "Date: " + date + "\n" +
                "Total Push Ups: " + totalPushUps + "\n" +
                "Total sets: " + totalSets + "\n" +
                "Explosiveness: " + ((float)Math.round(explosiveness * 100)) / 100 + "\n" +
                "Average Push Up Time: " + ((float)Math.round(avgPushUpTime * 100)) / 100;
    }
}
