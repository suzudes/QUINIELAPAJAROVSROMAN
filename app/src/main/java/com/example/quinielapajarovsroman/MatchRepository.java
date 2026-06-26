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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchRepository {
    private final MatchDao matchDao;
    private final ApiService apiService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final ZoneId CDMX_ZONE = ZoneId.of("America/Mexico_City");

    public MatchRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        matchDao = db.matchDao();
        apiService = ApiClient.getService(context);
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

    public void savePrediction(int matchId, int home, int away, final OnSaveListener listener) {
        apiService.submitPrediction(new PredictionRequest(matchId, home, away)).enqueue(new Callback<PredictionEntity>() {
            @Override
            public void onResponse(Call<PredictionEntity> call, Response<PredictionEntity> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> matchDao.insertPrediction(response.body()));
                    if (listener != null) listener.onSuccess();
                } else {
                    if (listener != null) listener.onError(response.code());
                }
            }
            @Override
            public void onFailure(Call<PredictionEntity> call, Throwable t) {
                if (listener != null) listener.onError(-1);
            }
        });
    }

    public void refreshMatches() {
        apiService.getMatches().enqueue(new Callback<List<ServerMatchDto>>() {
            @Override
            public void onResponse(Call<List<ServerMatchDto>> call, Response<List<ServerMatchDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executor.execute(() -> {
                        List<MatchEntity> entities = new ArrayList<>();
                        for (ServerMatchDto dto : response.body()) {
                            MatchEntity match = new MatchEntity();
                            match.id = dto.id;
                            match.utcDate = dto.kickoffUtc;
                            match.status = dto.status;
                            match.homeTeam = dto.homeTeam;
                            match.awayTeam = dto.awayTeam;
                            match.homeScore = dto.homeScore;
                            match.awayScore = dto.awayScore;
                            match.stage = dto.stage;

                            // Calcular matchDay CDMX
                            try {
                                ZonedDateTime utcDateTime = ZonedDateTime.parse(dto.kickoffUtc);
                                ZonedDateTime cdmxDateTime = utcDateTime.withZoneSameInstant(CDMX_ZONE);
                                match.matchDay = cdmxDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
                            } catch (Exception e) {
                                match.matchDay = "TBD";
                            }
                            
                            entities.add(match);
                            
                            // Guardar predicciones si vienen
                            if (dto.myPrediction != null) matchDao.insertPrediction(dto.myPrediction);
                            if (dto.rivalPrediction != null) matchDao.insertPrediction(dto.rivalPrediction);
                        }
                        matchDao.insertMatches(entities);
                    });
                }
            }
            @Override
            public void onFailure(Call<List<ServerMatchDto>> call, Throwable t) {
                Log.e("MatchRepository", "Refresh failed", t);
            }
        });
    }

    public interface OnSaveListener {
        void onSuccess();
        void onError(int code);
    }
}
