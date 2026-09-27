package com.footballwhispers.catalog.club;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    @Query("SELECT new com.footballwhispers.catalog.club.ClubCrest(c.club_id, c.name) FROM Club c WHERE c.domestic_competition_id = :competitionId AND c.last_season = :season ORDER BY c.name ASC")
    List<ClubCrest> listTeamsByCompetitionId(@Param("competitionId") String competitionId, @Param("season") int season);

    @Query("SELECT DISTINCT c.last_season FROM Club c WHERE c.domestic_competition_id = :competitionId AND c.last_season IS NOT NULL ORDER BY c.last_season DESC")
    List<Integer> listAvailableSeasons(@Param("competitionId") String competitionId);

    @Query("SELECT COUNT(c) FROM Club c WHERE c.domestic_competition_id = :competitionId AND c.last_season = :season")
    long countTeamsByCompetitionId(@Param("competitionId") String competitionId, @Param("season") int season);


    @Query(value = "SELECT DISTINCT c.name FROM Club c ORDER BY c.name ASC")
    List<String> listTeams();

    @Query("SELECT c FROM Club c WHERE c.name = :squadName")
    List<Club> squadInfoList(String squadName);

    @Query("SELECT new com.footballwhispers.catalog.club.ClubCrest(c.club_id, c.name) FROM Club c WHERE c.club_id IN :clubIds ORDER BY c.name ASC")
    List<ClubCrest> findCrestsByClubIds(@Param("clubIds") Collection<Long> clubIds);
}
