package com.example.iotProject;

import java.util.Date;

public class TrainingSession {
    public String Uid;
    public int totalPushUps;
    public TrainingPlan trainingPlan; // should save id instead of whole object
    public int totalSets;
    public float explosiveness;
    public float avgPushUpTime;
    public float rangeOfMotion;
    public Date date;
}
