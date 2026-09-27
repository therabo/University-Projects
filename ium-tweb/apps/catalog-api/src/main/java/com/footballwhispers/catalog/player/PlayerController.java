package com.footballwhispers.catalog.player;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@RestController
public class PlayerController {
    private final PlayerService playerService;

    public static class AdvancedSearchDTO{
        @JsonProperty("season")
        Integer season;
        @JsonProperty("country")
        String country;
        @JsonProperty("competition")
        String competition;
        @JsonProperty("year_birth")
        Integer year_birth;
        @JsonProperty("team")
        String team;
        @JsonProperty("role")
        String role;

        public Integer getSeason() {
            return season;
        }

        public void setSeason(Integer season) {
            this.season = season;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCompetition() {
            return competition;
        }

        public void setCompetition(String competition) {
            this.competition = competition;
        }

        public Integer getYear_birth() {
            return year_birth;
        }

        public void setYear_birth(Integer year_birth) {
            this.year_birth = year_birth;
        }

        public String getTeam() {
            return team;
        }

        public void setTeam(String team) {
            this.team = team;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * <li>Path to obtain the list of all positions on the field</li>
     * @return List of positions, e.g. Left Winger, Goalkeeper, and Right Midfield
     */
    @GetMapping("/get_role")
    public List<String> getRole() {
        return playerService.getRole();
    }

    /**
     *<li>Path to obtain information relating to a player</li>
     * @param Name Player name, e.g. Bukayo Saka
     * @return Player information such as name, age, and position
     * @throws JSONException Handle errors
     */
    @PostMapping("/info_player")
    public ResponseEntity<?> InfoPlayer(@RequestParam String Name) throws JSONException {
        JSONObject playerJson = playerService.InfoPlayer(Name);
        return ResponseEntity.ok(playerJson.toString());
    }

    /**
     * <li>Path to obtain a player profile by its database identifier</li>
     * @param playerId Positive identifier of the player
     * @return Player profile; 400 for an invalid identifier or 404 when absent
     * @throws JSONException If the profile cannot be serialized
     */
    @GetMapping("/players/{playerId}/profile")
    public ResponseEntity<?> playerProfile(@PathVariable long playerId) throws JSONException {
        if (playerId <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid player ID");
        JSONObject profile = playerService.infoPlayerById(playerId);
        if (profile.length() == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
        return ResponseEntity.ok(profile.toString());
    }

    /**
     * <li>Path to get all player nationalities</li>
     * @return List of countries, e.g. Italy and Brazil
     */
    @GetMapping("/country")
    public List<String> GetCountry() {
        return playerService.getCountry();
    }

    /**
     * <li>Path to get seasons years</li>
     * @return List of season years, e.g. 2003, 2004, and 2005
     */
    @GetMapping("/seasons")
    public List<Integer> GetSeasons() {
        return playerService.getSeasons();
    }

    /**
     *<li>Path to get a specific player list based on following fields.</li>
     * @param searchDTO Search criteria including season, country, competition, birth year, team, and position
     * @return List of matching players with their names and teams
     * @throws JSONException  Handle errors
     */
    @PostMapping("/advanced_search")
    public ResponseEntity<?> AdvancedSearch(@RequestBody AdvancedSearchDTO searchDTO) throws JSONException {
        List<JSONObject> results = playerService.advancedSearch(
                searchDTO.getSeason(),
                searchDTO.getCountry(),
                searchDTO.getCompetition(),
                searchDTO.getYear_birth(),
                searchDTO.getTeam(),
                searchDTO.getRole()
        );
        return ResponseEntity.ok(results.toString());
    }

    /**
     *Route that queries the database for players' birthday years
     * @return List of birth years, e.g. 1974, 1978, and 1979
     */
    @GetMapping("/get_birth_years")
    public List<Integer> YearsBirth(){
        return playerService.yearsBirth();
    }

    /**
     * <li>Path to obtain profile and portrait data for a list of player IDs</li>
     * @param playerIds List of up to 300 positive player identifiers
     * @return Available player records; 400 when the list is invalid
     */
    @PostMapping("/players/by-ids")
    public List<PlayerRosterItem> rosterPlayersByIds(@RequestBody List<Long> playerIds) {
        if (playerIds == null || playerIds.size() > 300
                || playerIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid player IDs");
        }
        return playerService.rosterPlayersByIds(playerIds);
    }

    /**
     * <li>Path to obtain a list of player of specific competition</li>
     * @param compId Competition code, e.g. IT1
     * @return List of the player about the competition
     */
    @PostMapping("/comp_players")
    public List<PlayerListItem> compPlayers(@RequestBody String compId) {
        return playerService.compPlayers(compId);
    }

}
