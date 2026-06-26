package com.example.quinielapajarovsroman;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "matches")
public class MatchEntity {
    @PrimaryKey
    public int id;
    
    @SerializedName("kickoffUtc")
    public String utcDate;
    
    public String matchDay; 
    public String status;
    public String homeTeam;
    public String awayTeam;
    public Integer homeScore;
    public Integer awayScore;
    public String stage;
}
