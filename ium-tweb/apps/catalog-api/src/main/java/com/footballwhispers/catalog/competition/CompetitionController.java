package com.footballwhispers.catalog.competition;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@RestController
public class CompetitionController {
    private final CompetitionService competitionService;

    @Autowired
    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    /**
     * <li>Path to obtain the list of national championships</li>
     *
     * @return List of competitions, e.g. Serie A and Premier League
     */
    @GetMapping("/list_competitions")
    public ResponseEntity<?> ListCompetitions() throws JSONException {
        List<JSONObject> championships =  competitionService.listCompetitions();
        return ResponseEntity.ok(championships.toString());
    }

    /**
     * <li>Path to obtain the stored PNG logo of a competition</li>
     * @param id Identifier of the competition
     * @return PNG image with public cache headers; 404 when no logo is available
     */
    @GetMapping(value = "/competitions/{id}/logo", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> logo(@PathVariable String id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(competitionService.getLogo(id));
    }

    /**
     * <li>Path to record a completed selection of a championship</li>
     * @param request JSON object containing the competition code
     * @return 204 without a response body; 400 for an unknown code
     */
    @PostMapping("/competitions/selections")
    public ResponseEntity<Void> recordSelection(@RequestBody Map<String, String> request) {
        competitionService.recordSelection(request.get("competition"));
        return ResponseEntity.noContent().build();
    }

    /**
     * <li>Path to obtain the details and available seasons of a championship</li>
     * @param competition Code of the requested championship
     * @return Championship details; 404 when the code is unknown
     */
    @PostMapping("/info_competition")
    public CompetitionDetails InfoCompetition(@RequestParam String competition) {
        return competitionService.infoCompetition(competition);
    }

}
