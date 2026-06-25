package com.example.quinielapajarovsroman;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "matches")
public class MatchEntity {
    @PrimaryKey
    public int id;
    public String utcDate;
    public String matchDay; // yyyy-MM-dd in CDMX
    public String status;
    public String homeTeam;
    public String awayTeam;
    public Integer homeScore;
    public Integer awayScore;
    public String stage;
    public String group;
}
