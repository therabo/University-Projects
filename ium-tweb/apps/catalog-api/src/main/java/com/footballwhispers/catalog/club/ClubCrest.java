package com.footballwhispers.catalog.club;

public record ClubCrest(Long clubId, String name, String crestUrl) {
    public ClubCrest(Long clubId, String name) {
        this(clubId, name, "/clubs/" + clubId + "/crest");
    }

    public static ClubCrest from(Club club) {
        return new ClubCrest(club.getClub_id(), club.getName());
    }
}
