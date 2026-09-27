package com.footballwhispers.catalog.player;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<String> getRole() {
        return playerRepository.getRole();
    }

    public List<Integer> yearsBirth(){
        return playerRepository.yearsBirth();
    }

    public JSONObject InfoPlayer(String Name) throws JSONException {
        return profileJson(playerRepository.infoPlayer(Name));
    }

    public JSONObject infoPlayerById(long playerId) throws JSONException {
        return profileJson(playerRepository.findById(playerId).orElse(null));
    }

    private JSONObject profileJson(Player player) throws JSONException {
        JSONObject playerJson = new JSONObject();
        if (player != null) {
            playerJson.put("PlayerId", player.getPlayer_id());
            playerJson.put("Name", player.getName());
            playerJson.put("Height", player.getHeight_in_cm());
            playerJson.put("Position", player.getSub_position());
            if (player.getDate_of_birth() != null) {
                LocalDate birthDate = player.getDate_of_birth();
                playerJson.put("DateOfBirth", birthDate.toString());
                playerJson.put("Age", Period.between(birthDate, LocalDate.now()).getYears());
            }
            playerJson.put("MarketValue", player.getMarket_value_in_eur());
            playerJson.put("Nationality", player.getCountry_of_citizenship());
            playerJson.put("Team", player.getCurrent_club_name());
            playerJson.put("ImageUrl", player.getImage_url());
            playerJson.put("Foot",player.getFoot());
            playerJson.put("lastSeason",player.getLast_season());

        }
        return playerJson;
    }

    public List<String> getCountry() {
        return playerRepository.getCountry();
    }

    public List<PlayerRosterItem> rosterPlayersByIds(List<Long> playerIds) {
        return playerRepository.findAllById(playerIds).stream().map(PlayerRosterItem::from).toList();
    }

    public List<PlayerListItem> compPlayers(String compId) {
        List<Player> players = playerRepository.compPlayers(compId, PageRequest.of(0, 30));
        return players.stream().map(PlayerListItem::from).toList();
    }

    public List<Integer> getSeasons() {
        return playerRepository.getSeasons();
    }

    public List<JSONObject> advancedSearch(Integer Season, String Country, String Competition, Integer Year_Birth, String Team, String Role) throws JSONException {
        if (Competition != null) {
            switch (Competition) {
                case "Bundesliga":
                    Competition = "bundesliga";
                    break;
                case "Eredivisie":
                    Competition = "eredivisie";
                    break;
                case "Jupiler Pro League":
                    Competition = "jupiler-pro-league";
                    break;
                case "LaLiga":
                    Competition = "laliga";
                    break;
                case "Liga Portugal":
                    Competition = "liga-portugal-bwin";
                    break;
                case "Ligue 1":
                    Competition = "ligue-1";
                    break;
                case "Premier League":
                    Competition = "premier-league";
                    break;
                case "Premier Liga":
                    Competition = "premier-liga";
                    break;
                case "Serie A":
                    Competition = "serie-a";
                    break;
                case "Souper Ligka Ellada":
                    Competition = "super-league-1";
                    break;
                case "Süper Lig":
                    Competition = "super-lig";
                    break;
                case "Superligaen":
                    Competition = "superligaen";
                    break;
                case "Scottish Premiership":
                    Competition = "scottish-premiership";
                    break;
                default:
                    break;
            }
        }
        List<Player> players =  playerRepository.advancedSearch(Season,Country,Competition,Year_Birth,Team,Role);
        List<JSONObject> ListPlayers  = new ArrayList<>();
        for(Player player : players){
            JSONObject playerJson = new JSONObject();
            if(player != null){
                playerJson.put("PlayerId", player.getPlayer_id());
                playerJson.put("Name", player.getName());
                playerJson.put("Nationality", player.getCountry_of_citizenship());
                if(player.getDate_of_birth() != null){
                    playerJson.put("Birth", player.getDate_of_birth());
                }else{
                    playerJson.put("Birth", "N/A");
                }
                playerJson.put("Position", player.getSub_position());
                playerJson.put("Team", player.getCurrent_club_name());
                ListPlayers.add(playerJson);
            }
        }
        return ListPlayers;
    }
}
