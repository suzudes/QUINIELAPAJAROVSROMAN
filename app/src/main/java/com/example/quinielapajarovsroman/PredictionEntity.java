package com.example.quinielapajarovsroman;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "predictions")
public class PredictionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String userId; // "PAJARO" o "ROMAN"
    public int matchId;
    public Integer predHome;
    public Integer predAway;
    public String state; // "LOCKED", "CANCELLED", "SCORED"
    public int points;
    public Long scoredAt;
}
