package com.footballwhispers.catalog.club;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@Service
public class ClubService {
    private final ClubRepository clubRepository;

    @Autowired
    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public List<Integer> availableSeasons(String competitionId) {
        return clubRepository.listAvailableSeasons(competitionId);
    }

    public List<ClubCrest> listTeamsByCompetitionId(String competitionId) {
        return listTeamsByCompetitionId(competitionId, null);
    }

    public List<ClubCrest> listTeamsByCompetitionId(String competitionId, Integer requestedSeason) {
        Integer season = resolveSeason(competitionId, requestedSeason);
        return season == null ? List.of() : clubRepository.listTeamsByCompetitionId(competitionId, season);
    }

    public long countTeamsByCompetitionId(String competitionId, Integer season) {
        return season == null ? 0 : clubRepository.countTeamsByCompetitionId(competitionId, season);
    }

    private Integer resolveSeason(String competitionId, Integer requestedSeason) {
        List<Integer> seasons = availableSeasons(competitionId);
        if (requestedSeason == null) {
            return seasons.isEmpty() ? null : seasons.get(0);
        }
        if (!seasons.contains(requestedSeason)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unavailable club season");
        }
        return requestedSeason;
    }

    public List<String> listTeams() {
        return clubRepository.listTeams();
    }

    public List<Club> squadInfoList(String squadName) {
        return clubRepository.squadInfoList(squadName);
    }

    public List<ClubCrest> listClubCrests(List<Long> clubIds) {
        if (clubIds == null || clubIds.isEmpty()) {
            return List.of();
        }

        List<Long> uniqueClubIds = clubIds.stream().distinct().toList();
        if (uniqueClubIds.size() > 100) {
            throw new IllegalArgumentException("A maximum of 100 club IDs is allowed");
        }

        return clubRepository.findCrestsByClubIds(uniqueClubIds);
    }

    public byte[] getCrest(long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));
        byte[] crest = club.getCrestData();
        if (crest == null || crest.length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Club crest not found");
        }
        return crest;
    }
}
