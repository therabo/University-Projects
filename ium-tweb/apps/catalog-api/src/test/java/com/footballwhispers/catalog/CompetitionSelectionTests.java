package com.footballwhispers.catalog;

import com.footballwhispers.catalog.competition.Competition;
import com.footballwhispers.catalog.competition.CompetitionRepository;
import com.footballwhispers.catalog.competition.CompetitionSelectionStatRepository;
import com.footballwhispers.catalog.competition.CompetitionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CompetitionSelectionTests {
    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CompetitionSelectionStatRepository selectionStatRepository;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reportsStoredLogoAndRealSelectionCountWithoutSeededPopularity() throws Exception {
        Competition league = new Competition();
        league.setCompetition_id("TEST1");
        league.setName("premier-league");
        league.setSub_type("first_tier");
        byte[] image = new ClassPathResource("data/competition-logos/gb1.png")
                .getInputStream().readAllBytes();
        league.setLogoData(image);
        competitionRepository.saveAndFlush(league);

        JSONObject before = findLeague("premier-league");
        assertThat(before.optLong("SelectionCount")).isZero();
        assertThat(before.optString("LogoUrl")).isEqualTo("/competitions/TEST1/logo");

        competitionService.recordSelection("premier-league");
        mockMvc.perform(post("/competitions/selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"competition\":\"premier-league\"}"))
                .andExpect(status().isNoContent());

        assertThat(selectionStatRepository.findById("premier-league").orElseThrow().getSelectionCount())
                .isEqualTo(2);
        assertThat(findLeague("premier-league").optLong("SelectionCount")).isEqualTo(2);
    }

    @Test
    void servesStoredPngAndReturnsNotFoundForMissingLogo() throws Exception {
        byte[] image = new ClassPathResource("data/competition-logos/gb1.png")
                .getInputStream().readAllBytes();
        Competition league = new Competition();
        league.setCompetition_id("TEST2");
        league.setName("test-league");
        league.setSub_type("first_tier");
        league.setLogoData(image);
        competitionRepository.saveAndFlush(league);

        assertThat(mockMvc.perform(get("/competitions/TEST2/logo"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray()).isEqualTo(image);
        assertThat(mockMvc.perform(get("/competitions/NOTFOUND/logo"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsByteArray()).isEmpty();

        Competition withoutImage = new Competition();
        withoutImage.setCompetition_id("TEST3");
        withoutImage.setName("test-league-without-logo");
        withoutImage.setSub_type("first_tier");
        competitionRepository.saveAndFlush(withoutImage);
        assertThat(findLeague("test-league-without-logo").isNull("LogoUrl")).isTrue();
        mockMvc.perform(get("/competitions/TEST3/logo"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsUnknownLeagueWithoutCreatingAStat() throws Exception {
        assertThatThrownBy(() -> competitionService.recordSelection("not-a-league"))
                .isInstanceOf(ResponseStatusException.class);
        mockMvc.perform(post("/competitions/selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"competition\":\"not-a-league\"}"))
                .andExpect(status().isBadRequest());
        assertThat(selectionStatRepository.findById("not-a-league")).isEmpty();
    }

    private JSONObject findLeague(String code) throws JSONException {
        List<JSONObject> leagues = competitionService.listCompetitions();
        return leagues.stream()
                .filter(league -> code.equals(league.optString("Value")))
                .findFirst()
                .orElseThrow();
    }
}
