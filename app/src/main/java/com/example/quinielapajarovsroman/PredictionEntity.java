package com.example.quinielapajarovsroman;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "predictions")
public class PredictionEntity {
    @PrimaryKey
    @NonNull
    public String id; // Cambiado a String para aceptar UUID del servidor
    public String userId; 
    public int matchId;
    public Integer predHome;
    public Integer predAway;
    public String state;
    public int points;
    public Long scoredAt;
}
