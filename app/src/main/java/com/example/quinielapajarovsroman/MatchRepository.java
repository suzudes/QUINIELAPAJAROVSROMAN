package com.example.quinielapajarovsroman;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
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
        executor.execute(() -> {
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
        });
    }

    public void refreshMatches() {
        apiService.getMatches().enqueue(new Callback<List<ClosedMatchWithPredictions>>() {
            @Override
            public void onResponse(Call<List<ClosedMatchWithPredictions>> call, Response<List<ClosedMatchWithPredictions>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executor.execute(() -> {
                        List<MatchEntity> entities = new ArrayList<>();
                        for (ClosedMatchWithPredictions item : response.body()) {
                            entities.add(item.match);
                            if (item.predictions != null) {
                                for (PredictionEntity p : item.predictions) {
                                    matchDao.insertPrediction(p);
                                }
                            }
                        }
                        matchDao.insertMatches(entities);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ClosedMatchWithPredictions>> call, Throwable t) {
                Log.e("MatchRepository", "Refresh failed", t);
            }
        });
    }

    public interface OnSaveListener {
        void onSuccess();
        void onError(int code);
    }
}
