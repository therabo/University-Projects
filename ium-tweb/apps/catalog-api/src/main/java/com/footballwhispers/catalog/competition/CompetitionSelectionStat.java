package com.footballwhispers.catalog.competition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "competition_search_stats")
public class CompetitionSelectionStat {
    @Id
    @Column(name = "competition_code", length = 100)
    private String competitionCode;

    @Column(name = "search_count", nullable = false)
    private long selectionCount;

    protected CompetitionSelectionStat() {
    }

    public CompetitionSelectionStat(String competitionCode) {
        this.competitionCode = competitionCode;
    }

    public String getCompetitionCode() {
        return competitionCode;
    }

    public long getSelectionCount() {
        return selectionCount;
    }

    public void increment() {
        selectionCount++;
    }
}
