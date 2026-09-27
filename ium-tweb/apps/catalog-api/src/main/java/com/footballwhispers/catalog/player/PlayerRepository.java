package com.footballwhispers.catalog.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query(value = "SELECT DISTINCT sub_position FROM Player ORDER BY sub_position ASC")
    public List<String> getRole();


    @Query("SELECT p FROM Player p WHERE p.name = :Name")
    Player infoPlayer(String Name);

    @Query(value = "SELECT DISTINCT p.country_of_birth FROM Player p ORDER BY p.country_of_birth ASC")
    List<String> getCountry();


    @Query(value = "SELECT DISTINCT p.last_season FROM Player p ORDER BY p.last_season DESC")
    List<Integer> getSeasons();

    @Query(value = "SELECT p FROM Player p JOIN Competition c ON c.competition_id  = p.current_club_domestic_competition_id JOIN Club cl ON cl.club_id = p.current_club_id WHERE (:Season IS NULL OR p.last_season = :Season) AND (:Country IS NULL OR p.country_of_citizenship = :Country) AND (:Competition IS NULL OR c.name = :Competition) AND (:Year_Birth IS NULL OR YEAR(p.date_of_birth) = :Year_Birth) AND (:Team IS NULL OR cl.name = :Team) AND (:Role IS NULL OR p.sub_position = :Role) ORDER BY p.name ASC")
    List<Player> advancedSearch(Integer Season, String Country, String Competition, Integer Year_Birth, String Team, String Role);

    @Query(value = "SELECT DISTINCT YEAR(p.date_of_birth) FROM Player p ORDER BY YEAR(p.date_of_birth) DESC")
    List<Integer> yearsBirth();

    @Query("SELECT p FROM Player p WHERE p.current_club_domestic_competition_id = :compId ORDER BY p.player_id ASC")
    List<Player> compPlayers(String compId, Pageable pageable);
}
