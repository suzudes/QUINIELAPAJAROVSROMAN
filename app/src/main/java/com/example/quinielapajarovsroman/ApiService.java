package com.example.quinielapajarovsroman;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ApiService {
    // Petición limpia como indica la documentación en la captura
    @GET("v4/matches")
    Call<FootballDataResponse> getMatchesExternal(
        @Header("X-Auth-Token") String token
    );
}
