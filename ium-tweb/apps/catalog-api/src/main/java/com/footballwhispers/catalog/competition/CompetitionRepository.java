package com.footballwhispers.catalog.competition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
@Repository
public interface CompetitionRepository extends JpaRepository<Competition, String> {
    @Query("SELECT new com.footballwhispers.catalog.competition.CompetitionListItem(c.competition_id, c.name, CASE WHEN c.logoData IS NOT NULL THEN true ELSE false END) FROM Competition c WHERE c.sub_type = 'first_tier' ORDER BY c.name, c.competition_id")
    List<CompetitionListItem> listCompetitions();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Competition c WHERE c.name = :code AND c.sub_type = 'first_tier' ORDER BY c.competition_id")
    List<Competition> lockFirstTierByCode(String code);

    @Query("SELECT c FROM Competition c WHERE c.name = :competition AND c.sub_type = 'first_tier' ORDER BY c.competition_id")
    List<Competition> findFirstTierByCode(String competition);
}
