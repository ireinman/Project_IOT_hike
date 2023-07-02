package com.example.iotProject;

import java.util.Date;

public class BSUSession {
    private static final long BASE = 31; // A prime number
    private static final long MOD = 1000000007; // A large prime number
    public static long hash(Date date) {
        long timestamp = date.getTime(); // Get the timestamp value of the date

        long hashValue = 0;
        while (timestamp != 0) {
            hashValue = (hashValue * BASE + (timestamp % 10)) % MOD;
            timestamp /= 10;
        }

        return hashValue;
    }

    public static Date reverseHash(long hashValue) {
        long timestamp = 0;
        long multiplier = 1;

        while (hashValue != 0) {
            timestamp = (timestamp + (hashValue % BASE) * multiplier) % MOD;
            hashValue /= BASE;
            multiplier *= 10;
        }

        return new Date(timestamp);
    }
    public BSUSession(){}

    public BSUSession(int totalPushUps, TrainingPlan trainingPlan, int totalSets, float explosiveness, float avgPushUpTime, float rangeOfMotion, Date date) {
        this.totalTime = totalSets;
        this.explosiveness = explosiveness;
        this.rangeOfMotion = rangeOfMotion;
        this.date = hash(date);
    }

    public Long getDate(){
        return this.date;
    }

    public float totalTime;
    public float explosiveness;
    public float rangeOfMotion;
    private Long date;
}