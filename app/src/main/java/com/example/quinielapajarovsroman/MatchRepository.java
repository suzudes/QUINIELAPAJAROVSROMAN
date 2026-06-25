package com.example.quinielapajarovsroman;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MatchRepository {
    private final MatchDao matchDao;
    private final FootballApiService apiService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String API_TOKEN = "d8122a219a394bbaaee2b7883acb5372";
    private static final ZoneId CDMX_ZONE = ZoneId.of("America/Mexico_City");

    public MatchRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        matchDao = db.matchDao();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.football-data.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(FootballApiService.class);
    }

    public LiveData<List<ClosedMatchWithPredictions>> getActiveMatches() {
        return matchDao.getActiveMatchesWithPredictions();
    }

    public LiveData<List<ClosedMatchWithPredictions>> getClosedMatches() {
        return matchDao.getClosedMatchesWithPredictions();
    }

    public LiveData<List<User>> getStandings() {
        return matchDao.getStandings();
    }

    public void savePrediction(PredictionEntity prediction) {
        executor.execute(() -> matchDao.insertPrediction(prediction));
    }

    public void refreshMatches() {
        executor.execute(() -> {
            try {
                Response<FootballDataDtos.Response> response = apiService.getWorldCupMatches(API_TOKEN).execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<MatchEntity> entities = new ArrayList<>();
                    for (FootballDataDtos.MatchDto dto : response.body().matches) {
                        entities.add(mapToEntity(dto));
                    }
                    matchDao.insertMatches(entities);
                    evaluarPartidosFinalizados();
                }
            } catch (Exception e) {
                Log.e("MatchRepository", "Error refreshing matches", e);
            }
        });
    }

    private void evaluarPartidosFinalizados() {
        List<PredictionEntity> pending = matchDao.getUnscoredPredictions();
        for (PredictionEntity pred : pending) {
            MatchEntity match = matchDao.getMatchById(pred.matchId);
            if (match != null && "FINISHED".equals(match.status)) {
                if ("CANCELLED".equals(pred.state)) {
                    pred.points = 0;
                } else {
                    if (pred.predHome != null && pred.predAway != null &&
                        pred.predHome.equals(match.homeScore) && 
                        pred.predAway.equals(match.awayScore)) {
                        pred.points = 1;
                    } else {
                        pred.points = 0;
                    }
                }
                pred.state = "SCORED";
                pred.scoredAt = System.currentTimeMillis();
                matchDao.updatePrediction(pred);
            }
        }
    }

    private MatchEntity mapToEntity(FootballDataDtos.MatchDto dto) {
        MatchEntity entity = new MatchEntity();
        entity.id = dto.id;
        entity.utcDate = dto.utcDate;
        entity.status = dto.status;
        entity.stage = dto.stage;
        entity.group = dto.group;
        entity.homeTeam = dto.homeTeam.name;
        entity.awayTeam = dto.awayTeam.name;
        if (dto.score != null && dto.score.fullTime != null) {
            entity.homeScore = dto.score.fullTime.home;
            entity.awayScore = dto.score.fullTime.away;
        }
        ZonedDateTime utcDateTime = ZonedDateTime.parse(dto.utcDate);
        ZonedDateTime cdmxDateTime = utcDateTime.withZoneSameInstant(CDMX_ZONE);
        entity.matchDay = cdmxDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
        return entity;
    }
}
