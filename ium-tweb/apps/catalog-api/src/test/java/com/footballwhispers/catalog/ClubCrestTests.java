package com.footballwhispers.catalog;

import com.footballwhispers.catalog.club.Club;
import com.footballwhispers.catalog.club.ClubCrest;
import com.footballwhispers.catalog.club.ClubRepository;
import com.footballwhispers.catalog.club.ClubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClubCrestTests {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubService clubService;

    @Test
    void returnsDeduplicatedCrestMetadataForRequestedClubs() throws IOException {
        Club arsenal = new Club();
        arsenal.setClub_id(11L);
        arsenal.setName("Arsenal Football Club");
        arsenal.setCrest_url("https://tmssl.akamaized.net/images/wappen/head/11.png");
        byte[] crestData = new ClassPathResource("data/club-crests/11.png")
                .getInputStream().readAllBytes();
        arsenal.setCrestData(crestData);
        clubRepository.save(arsenal);

        List<ClubCrest> crests = clubService.listClubCrests(List.of(11L, 11L));

        assertThat(crests).containsExactly(new ClubCrest(11L, "Arsenal Football Club"));
        assertThat(clubService.getCrest(11L)).isEqualTo(crestData);
    }

}
