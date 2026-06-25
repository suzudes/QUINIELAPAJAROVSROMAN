package com.example.quinielapajarovsroman;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

public class ClosedMatchWithPredictions {
    @Embedded
    public MatchEntity match;

    @Relation(
        parentColumn = "id",
        entityColumn = "matchId"
    )
    public List<PredictionEntity> predictions;
}
