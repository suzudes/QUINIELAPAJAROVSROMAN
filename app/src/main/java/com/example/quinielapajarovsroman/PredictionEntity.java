package com.example.quinielapajarovsroman;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "predictions")
public class PredictionEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String userId; 
    public String userName; // NUEVO: Para saber de quién es (PAJARO o ROMAN)
    public int matchId;
    public Integer predHome;
    public Integer predAway;
    public String state;
    public int points;
    public Long scoredAt;
}
