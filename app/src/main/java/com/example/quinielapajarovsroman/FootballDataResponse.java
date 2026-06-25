package com.example.quinielapajarovsroman;

import java.util.List;

public class FootballDataResponse {
    public List<ApiMatch> matches;

    public static class ApiMatch {
        public int id;
        public String utcDate;
        public String status;
        public Team homeTeam;
        public Team awayTeam;
        public Score score;
    }

    public static class Team {
        public String name;
        public String tla; // Abreviatura de 3 letras (ESP, GER, etc)
    }

    public static class Score {
        public FullTime fullTime;
    }

    public static class FullTime {
        public Integer home;
        public Integer away;
    }
}
