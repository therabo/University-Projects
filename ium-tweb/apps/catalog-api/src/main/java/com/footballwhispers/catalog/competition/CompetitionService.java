package com.footballwhispers.catalog.competition;

import com.footballwhispers.catalog.club.ClubService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CompetitionService {
    private static final Map<String, String> DISPLAY_NAMES = Map.ofEntries(
            Map.entry("bundesliga", "Bundesliga"),
            Map.entry("eredivisie", "Eredivisie"),
            Map.entry("jupiler-pro-league", "Jupiler Pro League"),
            Map.entry("laliga", "LaLiga"),
            Map.entry("liga-portugal-bwin", "Liga Portugal"),
            Map.entry("ligue-1", "Ligue 1"),
            Map.entry("premier-league", "Premier League"),
            Map.entry("premier-liga", "Premier Liga"),
            Map.entry("serie-a", "Serie A"),
            Map.entry("super-league-1", "Souper Ligka Ellada"),
            Map.entry("super-lig", "Süper Lig"),
            Map.entry("superligaen", "Superligaen"),
            Map.entry("scottish-premiership", "Scottish Premiership")
    );
    private final CompetitionRepository competitionRepository;
    private final CompetitionSelectionStatRepository selectionStatRepository;
    private final ClubService clubService;

    @Autowired
    public CompetitionService(CompetitionRepository competitionRepository,
                              CompetitionSelectionStatRepository selectionStatRepository,
                              ClubService clubService) {
        this.competitionRepository = competitionRepository;
        this.selectionStatRepository = selectionStatRepository;
        this.clubService = clubService;
    }

    public List<JSONObject> listCompetitions() throws JSONException {
        List<CompetitionListItem> championships = competitionRepository.listCompetitions();
        Map<String, Long> selectionCounts = new HashMap<>();
        selectionStatRepository.findAll().forEach(stat ->
                selectionCounts.put(stat.getCompetitionCode(), stat.getSelectionCount()));
        List<JSONObject> result = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();
        for (CompetitionListItem competition : championships) {
            String championship = competition.code();
            if (championship == null || !seenCodes.add(championship)) {
                continue;
            }
            JSONObject championshipJson = new JSONObject();
            championshipJson.put("Value", championship);
            championshipJson.put("Name", DISPLAY_NAMES.getOrDefault(championship,
                    championship.replace('-', ' ')));
            championshipJson.put("LogoUrl", competition.hasLogo()
                    ? "/competitions/" + competition.id() + "/logo" : JSONObject.NULL);
            championshipJson.put("SelectionCount", selectionCounts.getOrDefault(championship, 0L));
            result.add(championshipJson);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public byte[] getLogo(String id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (competition.getLogoData() == null || competition.getLogoData().length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return competition.getLogoData();
    }

    @Transactional
    public void recordSelection(String code) {
        if (code == null || code.isBlank() || competitionRepository.lockFirstTierByCode(code).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown league code");
        }

        CompetitionSelectionStat stat = selectionStatRepository.findById(code)
                .orElseGet(() -> new CompetitionSelectionStat(code));
        stat.increment();
        selectionStatRepository.save(stat);
    }

    @Transactional(readOnly = true)
    public CompetitionDetails infoCompetition(String competition) {
        return competitionRepository.findFirstTierByCode(competition).stream()
                .findFirst()
                .map(found -> {
                    List<Integer> seasons = clubService.availableSeasons(found.getCompetition_id());
                    Integer selectedSeason = seasons.isEmpty() ? null : seasons.get(0);
                    return CompetitionDetails.from(
                            found,
                            DISPLAY_NAMES.getOrDefault(found.getName(), found.getName()),
                            selectedSeason,
                            clubService.countTeamsByCompetitionId(found.getCompetition_id(), selectedSeason),
                            seasons);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown league code"));
    }
}
