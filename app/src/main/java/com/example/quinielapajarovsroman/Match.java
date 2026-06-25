package com.example.quinielapajarovsroman;

public class Match {
    public String id;
    public String homeTeam;
    public String awayTeam;
    public String kickoffUtc;
    public String status; // SCHEDULED, IN_PLAY, FINISHED
    public Integer homeScore;
    public Integer awayScore;
    public String stage;
    
    // Virtual fields for prediction status in UI
    public Prediction userPrediction;
    public Prediction rivalPrediction;

    public boolean isLocked() {
        // Business rule: Locked if status is not SCHEDULED or handled by backend
        return !"SCHEDULED".equals(status);
    }
}
