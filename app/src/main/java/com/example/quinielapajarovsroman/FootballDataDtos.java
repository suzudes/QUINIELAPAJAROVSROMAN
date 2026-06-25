package com.example.quinielapajarovsroman;

import java.util.List;

public class FootballDataDtos {
    public static class Response {
        public List<MatchDto> matches;
    }

    public static class MatchDto {
        public int id;
        public String utcDate;
        public String status;
        public String stage;
        public String group;
        public TeamDto homeTeam;
        public TeamDto awayTeam;
        public ScoreDto score;
    }

    public static class TeamDto {
        public String name;
    }

    public static class ScoreDto {
        public FullTimeDto fullTime;
    }

    public static class FullTimeDto {
        public Integer home;
        public Integer away;
    }
}
