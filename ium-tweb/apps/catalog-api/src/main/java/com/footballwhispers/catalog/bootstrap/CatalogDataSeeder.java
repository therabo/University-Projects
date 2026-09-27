package com.footballwhispers.catalog.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballwhispers.catalog.club.Club;
import com.footballwhispers.catalog.club.ClubRepository;
import com.footballwhispers.catalog.competition.Competition;
import com.footballwhispers.catalog.competition.CompetitionRepository;
import com.footballwhispers.catalog.player.Player;
import com.footballwhispers.catalog.player.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
public class CatalogDataSeeder implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(CatalogDataSeeder.class);

    private final ObjectMapper objectMapper;
    private final ClubRepository clubRepository;
    private final CompetitionRepository competitionRepository;
    private final PlayerRepository playerRepository;

    public CatalogDataSeeder(
            ObjectMapper objectMapper,
            ClubRepository clubRepository,
            CompetitionRepository competitionRepository,
            PlayerRepository playerRepository
    ) {
        this.objectMapper = objectMapper;
        this.clubRepository = clubRepository;
        this.competitionRepository = competitionRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        List<Competition> competitions = readList("data/competitions.json", Competition.class);
        if (competitionRepository.count() == 0) {
            competitionRepository.saveAll(competitions);
            logger.info("Seeded {} competitions", competitions.size());
        }

        int synchronizedLogos = 0;
        for (Competition source : competitions) {
            if (!"first_tier".equals(source.getSub_type())) {
                continue;
            }
            String id = source.getCompetition_id();
            ClassPathResource image = new ClassPathResource(
                    "data/competition-logos/" + id.toLowerCase(Locale.ROOT) + ".png");
            byte[] bytes;
            try (InputStream input = image.getInputStream()) {
                bytes = input.readAllBytes();
            }
            if (bytes.length == 0) {
                throw new IOException("Empty competition logo: " + id);
            }
            Competition stored = competitionRepository.findById(id)
                    .orElseGet(() -> competitionRepository.save(source));
            if (!Arrays.equals(stored.getLogoData(), bytes)) {
                stored.setLogoData(bytes);
                synchronizedLogos++;
            }
        }
        logger.info("Synchronized {} competition logos", synchronizedLogos);

        List<Club> clubs = readList("data/clubs.json", Club.class);
        for (Club club : clubs) {
            ClassPathResource image = new ClassPathResource("data/club-crests/" + club.getClub_id() + ".png");
            try (InputStream input = image.getInputStream()) {
                byte[] bytes = input.readAllBytes();
                if (bytes.length == 0) {
                    throw new IOException("Empty club crest: " + club.getClub_id());
                }
                club.setCrestData(bytes);
            }
        }
        clubRepository.saveAll(clubs);
        logger.info("Synchronized {} clubs with local crest artwork", clubs.size());

        if (playerRepository.count() == 0) {
            List<Player> players = readList("data/players.json", Player.class);
            playerRepository.saveAll(players);
            logger.info("Seeded {} players", players.size());
        }

    }

    private <T> List<T> readList(String path, Class<T> elementType) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream input = resource.getInputStream()) {
            return objectMapper.readValue(
                    input,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType)
            );
        }
    }
}
