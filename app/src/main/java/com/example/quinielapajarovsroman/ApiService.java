package com.example.quinielapajarovsroman;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("auth/{token}")
    Call<AuthResponse> login(@Path("token") String token);

    @GET("matches")
    Call<List<ServerMatchDto>> getMatches();

    @GET("standings")
    Call<List<User>> getStandings();

    @POST("predict")
    Call<PredictionEntity> submitPrediction(@Body PredictionRequest prediction);
}

class AuthResponse {
    public String status;
    public String name;
}

class PredictionRequest {
    public int matchId;
    public int predHome;
    public int predAway;

    public PredictionRequest(int matchId, int predHome, int predAway) {
        this.matchId = matchId;
        this.predHome = predHome;
        this.predAway = predAway;
    }
}
