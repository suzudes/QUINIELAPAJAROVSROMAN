package com.example.quinielapajarovsroman;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface FootballApiService {
    @GET("v4/competitions/WC/matches")
    Call<FootballDataDtos.Response> getWorldCupMatches(
            @Header("X-Auth-Token") String token
    );
}
