package com.footballwhispers.catalog.competition;

import java.util.List;

public record CompetitionDetails(
        String id,
        String name,
        String displayName,
        String countryName,
        String confederation,
        String competitionType,
        String tier,
        Integer dataSeason,
        long clubCount,
        List<Integer> availableSeasons,
        String logoUrl
) {
    public static CompetitionDetails from(Competition competition, String displayName,
                                          Integer dataSeason, long clubCount,
                                          List<Integer> availableSeasons) {
        String logoUrl = competition.getLogoData() == null || competition.getLogoData().length == 0
                ? null : "/competitions/" + competition.getCompetition_id() + "/logo";
        return new CompetitionDetails(
                competition.getCompetition_id(),
                competition.getName(),
                displayName,
                competition.getCountry_name(),
                competition.getConfederation(),
                competition.getType(),
                competition.getSub_type(),
                dataSeason,
                clubCount,
                availableSeasons,
                logoUrl
        );
    }
}
