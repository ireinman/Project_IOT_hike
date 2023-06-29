package com.example.iotProject;

public class TrainingPlan {
    private String trainingName;
    public int setsAmount;
    public int reps;
    public TrainingPlan(){}

    public TrainingPlan(String trainingName, int setsAmount, int reps) {
        this.trainingName = trainingName;
        this.setsAmount = setsAmount;
        this.reps = reps;
    }

    public String getTrainingName(){
        return trainingName;
    }
}
