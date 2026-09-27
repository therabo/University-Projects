package com.footballwhispers.catalog;

import com.footballwhispers.catalog.player.Player;
import com.footballwhispers.catalog.player.PlayerListItem;
import com.footballwhispers.catalog.player.PlayerRosterItem;
import com.footballwhispers.catalog.player.PlayerRepository;
import com.footballwhispers.catalog.player.PlayerService;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PlayerImageUrlTests {
    @Autowired private PlayerRepository playerRepository;
    @Autowired private PlayerService playerService;

    @Test
    void competitionListIsBoundedOrderedAndUsesStoredImageUrls() throws MalformedURLException {
        String sourceUrl = "https://img.a.transfermarkt.technology/portrait/header/900008.jpg?lm=1";
        playerRepository.saveAllAndFlush(List.of(
                player(900009L, "Second", "IT1", null),
                player(900008L, "First", "IT1", URI.create(sourceUrl).toURL()),
                player(900007L, "Other", "GB1", null)));

        assertThat(playerService.compPlayers("IT1")).containsExactly(
                new PlayerListItem(900008L, "First", "Player", sourceUrl),
                new PlayerListItem(900009L, "Second", "Player", null));
    }

    @Test
    void rosterLookupUsesPlayerIdsAndCataloguedNamesAndPortraits() throws MalformedURLException {
        String sourceUrl = "https://img.a.transfermarkt.technology/portrait/header/900012.jpg?lm=1";
        playerRepository.saveAllAndFlush(List.of(
                player(900012L, "Andrea", "IT1", URI.create(sourceUrl).toURL()),
                player(900013L, "Marco", "GB1", null)));

        assertThat(playerService.rosterPlayersByIds(List.of(900012L, 900013L, 999999L)))
                .containsExactlyInAnyOrder(
                        new PlayerRosterItem(900012L, "Andrea Player", sourceUrl),
                        new PlayerRosterItem(900013L, "Marco Player", null));
    }

    @Test
    void profileLookupUsesCanonicalId() throws JSONException {
        playerRepository.saveAndFlush(player(900014L, "Marta", "IT1", null));
        assertThat(playerService.infoPlayerById(900014L).getLong("PlayerId")).isEqualTo(900014L);
        assertThat(playerService.infoPlayerById(900014L).getString("Name")).isEqualTo("Marta Player");
        assertThat(playerService.infoPlayerById(999999L).length()).isZero();
    }

    private Player player(long id, String firstName, String competitionId, URL imageUrl) {
        Player player = new Player();
        player.setPlayer_id(id);
        player.setFirst_name(firstName);
        player.setLast_name("Player");
        player.setName(firstName + " Player");
        player.setCurrent_club_domestic_competition_id(competitionId);
        player.setImage_url(imageUrl);
        return player;
    }
}
