package com.example.quinielapajarovsroman;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMatches(List<MatchEntity> matches);

    // Partidos ACTIVOS con predicciones para ver si ya bloqueamos
    @Transaction
    @Query("SELECT * FROM matches WHERE status != 'FINISHED' ORDER BY utcDate ASC")
    LiveData<List<ClosedMatchWithPredictions>> getActiveMatchesWithPredictions();

    @Transaction
    @Query("SELECT * FROM matches WHERE status = 'FINISHED' ORDER BY utcDate DESC")
    LiveData<List<ClosedMatchWithPredictions>> getClosedMatchesWithPredictions();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPrediction(PredictionEntity prediction);

    @Query("SELECT * FROM predictions WHERE state = 'LOCKED' OR state = 'CANCELLED'")
    List<PredictionEntity> getUnscoredPredictions();

    @Update
    void updatePrediction(PredictionEntity prediction);

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    MatchEntity getMatchById(int matchId);

    // Consulta para la TABLA DE POSICIONES
    @Query("SELECT userId as name, SUM(points) as points, COUNT(CASE WHEN points = 1 THEN 1 END) as exactAciertos " +
           "FROM predictions WHERE state = 'SCORED' GROUP BY userId ORDER BY points DESC, exactAciertos DESC")
    LiveData<List<User>> getStandings();
}
