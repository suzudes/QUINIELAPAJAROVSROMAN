package com.example.quinielapajarovsroman;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class MatchViewModel extends AndroidViewModel {
    private final MatchRepository repository;
    private final LiveData<List<ClosedMatchWithPredictions>> activeMatches;
    private final LiveData<List<ClosedMatchWithPredictions>> closedMatches;
    private final LiveData<List<User>> standings;

    public MatchViewModel(@NonNull Application application) {
        super(application);
        repository = new MatchRepository(application);
        activeMatches = repository.getActiveMatches();
        closedMatches = repository.getClosedMatches();
        standings = repository.getStandings();
    }

    public LiveData<List<ClosedMatchWithPredictions>> getActiveMatches() { return activeMatches; }
    public LiveData<List<ClosedMatchWithPredictions>> getClosedMatches() { return closedMatches; }
    public LiveData<List<User>> getStandings() { return standings; }
    public void refresh() { repository.refreshMatches(); }

    public void savePrediction(int matchId, int home, int away, MatchRepository.OnSaveListener listener) {
        repository.savePrediction(matchId, home, away, listener);
    }
}
