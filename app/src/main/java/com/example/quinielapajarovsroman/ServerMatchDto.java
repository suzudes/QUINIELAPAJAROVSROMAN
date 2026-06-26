package com.example.quinielapajarovsroman;

import java.util.List;

public class ServerMatchDto {
    public int id;
    public String kickoffUtc;
    public String status;
    public String homeTeam;
    public String awayTeam;
    public Integer homeScore;
    public Integer awayScore;
    public String stage;
    public PredictionEntity myPrediction;
    public PredictionEntity rivalPrediction;
    public List<PredictionEntity> predictions; // Lista cruda que manda prisma
}
