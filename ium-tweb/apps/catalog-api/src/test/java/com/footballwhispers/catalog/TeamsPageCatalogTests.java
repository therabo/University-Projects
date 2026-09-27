package com.footballwhispers.catalog;

import com.footballwhispers.catalog.club.Club;
import com.footballwhispers.catalog.club.ClubCrest;
import com.footballwhispers.catalog.club.ClubRepository;
import com.footballwhispers.catalog.club.ClubService;
import com.footballwhispers.catalog.competition.Competition;
import com.footballwhispers.catalog.competition.CompetitionDetails;
import com.footballwhispers.catalog.competition.CompetitionRepository;
import com.footballwhispers.catalog.competition.CompetitionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TeamsPageCatalogTests {
    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubService clubService;

    @Test
    void leagueDetailsAndClubCrestsUseTheSameCanonicalCompetitionId() throws IOException {
        Competition primary = new Competition();
        primary.setCompetition_id("RU1");
        primary.setName("premier-liga");
        primary.setSub_type("first_tier");
        primary.setType("domestic_league");
        primary.setCountry_name("Russia");
        primary.setLogoData(new ClassPathResource("data/competition-logos/ru1.png")
                .getInputStream().readAllBytes());

        Competition duplicateCode = new Competition();
        duplicateCode.setCompetition_id("UKR1");
        duplicateCode.setName("premier-liga");
        duplicateCode.setSub_type("first_tier");
        duplicateCode.setCountry_name("Ukraine");
        competitionRepository.saveAllAndFlush(List.of(primary, duplicateCode));

        Club included = club(900001L, "Russian FC", "RU1", 2023);
        Club otherLeague = club(900002L, "Ukrainian FC", "UKR1", 2023);
        Club olderSeason = club(900003L, "Old FC", "RU1", 2022);
        clubRepository.saveAllAndFlush(List.of(included, otherLeague, olderSeason));

        CompetitionDetails details = competitionService.infoCompetition("premier-liga");
        assertThat(details.id()).isEqualTo("RU1");
        assertThat(details.countryName()).isEqualTo("Russia");
        assertThat(details.displayName()).isEqualTo("Premier Liga");
        assertThat(details.competitionType()).isEqualTo("domestic_league");
        assertThat(details.tier()).isEqualTo("first_tier");
        assertThat(details.dataSeason()).isEqualTo(2023);
        assertThat(details.clubCount()).isEqualTo(1);
        assertThat(details.availableSeasons()).containsExactly(2023, 2022);
        assertThat(details.logoUrl()).isEqualTo("/competitions/RU1/logo");
        assertThat(clubService.listTeamsByCompetitionId(details.id()))
                .containsExactly(new ClubCrest(900001L, "Russian FC"));
        assertThat(clubService.listTeamsByCompetitionId(details.id(), 2022))
                .containsExactly(new ClubCrest(900003L, "Old FC"));
        assertThat(clubService.countTeamsByCompetitionId(details.id(), 2022)).isEqualTo(1);
        assertThatThrownBy(() -> clubService.listTeamsByCompetitionId(details.id(), 2024))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400 BAD_REQUEST");
    }

    private Club club(long id, String name, String competitionId, int season) {
        Club club = new Club();
        club.setClub_id(id);
        club.setName(name);
        club.setDomestic_competition_id(competitionId);
        club.setLast_season(season);
        club.setCrest_url("https://tmssl.akamaized.net/images/wappen/head/" + id + ".png");
        return club;
    }
}
