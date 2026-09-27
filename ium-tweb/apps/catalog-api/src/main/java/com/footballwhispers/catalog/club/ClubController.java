package com.footballwhispers.catalog.club;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class ClubController {
    private final ClubService clubService;

    @Autowired
    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    /**
     * <li>Path to obtain the teams recorded for a championship and season</li>
     * @param competitionId Identifier of the championship
     * @param season Optional recorded season; the latest is used when omitted
     * @return Teams with their identifiers and crest URLs
     */
    @PostMapping("/list_teamsbycompetition")
    public List<ClubCrest> listTeamsByCompetition(@RequestBody String competitionId,
                                                 @RequestParam(name = "season", required = false) Integer season) {
        return clubService.listTeamsByCompetitionId(competitionId, season);
    }

    /**
     * <li>Path to obtain all the teams present in the database</li>
     * @return List of teams, e.g. Juventus FC, Arsenal, and Barcelona FC
     */
    @GetMapping("/all_teams")
    public List<String> ListTeams() {
        return clubService.listTeams();
    }

    /**
     * <li>Path to obtain the info of a specific team</li>
     * @param squadName Team name, e.g. Palermo FC
     * @return Information of the specified team
     */
    @PostMapping("/list_info_squad")
    public List<Club> SquadInfoList(@RequestParam String squadName) {
        return clubService.squadInfoList(squadName);
    }

    /**
     * <li>Path to obtain crest metadata for multiple clubs</li>
     * @param clubIds Club identifiers supplied in the ids query parameter
     * @return Club identifiers, names and crest URLs; 400 for invalid identifiers
     */
    @GetMapping("/clubs/crests")
    public List<ClubCrest> listClubCrests(@RequestParam(name = "ids") List<Long> clubIds) {
        try {
            return clubService.listClubCrests(clubIds);
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error.getMessage(), error);
        }
    }

    /**
     * <li>Path to obtain the stored PNG crest of a club</li>
     * @param id Identifier of the club
     * @return PNG image with public cache headers; 404 when no crest is available
     */
    @GetMapping(value = "/clubs/{id}/crest", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> crest(@PathVariable long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(clubService.getCrest(id));
    }
}
