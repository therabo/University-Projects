package com.footballwhispers.catalog.club;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "clubs")
public class Club {

    @Id
    @Column(name = "club_id")
    private Long club_id;

    @Column(name = "club_code")
    private String club_code;

    @Column(name = "name")
    private String name;

    @Column(name = "domestic_competition_id")
    private String domestic_competition_id;

    @Column(name = "total_market_value")
    private BigDecimal total_market_value;

    @Column(name = "squad_size")
    private Integer squad_size;

    @Column(name = "average_age")
    private Double average_age;

    @Column(name = "foreigners_number")
    private Integer foreigners_number;

    @Column(name = "foreigners_percentage")
    private Double foreigners_percentage;

    @Column(name = "national_team_players")
    private Integer national_team_players;

    @Column(name = "stadium_name")
    private String stadium_name;

    @Column(name = "stadium_seats")
    private Integer stadium_seats;

    @Column(name = "net_transfer_record")
    private String net_transfer_record;

    @Column(name = "coach_name")
    private String coach_name;

    @Column(name = "last_season")
    private Integer last_season;

    @Column(name = "url")
    private String url;

    @Column(name = "crest_url")
    private String crest_url;

    @Column(name = "crest_data", columnDefinition = "bytea")
    private byte[] crestData;

    public Club() {
    }

    public Long getClub_id() {
        return club_id;
    }

    public void setClub_id(Long club_id) {
        this.club_id = club_id;
    }

    public String getClub_code() {
        return club_code;
    }

    public void setClub_code(String club_code) {
        this.club_code = club_code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomestic_competition_id() {
        return domestic_competition_id;
    }

    public void setDomestic_competition_id(String domestic_competition_id) {
        this.domestic_competition_id = domestic_competition_id;
    }

    public BigDecimal getTotal_market_value() {
        return total_market_value;
    }

    public void setTotal_market_value(BigDecimal total_market_value) {
        this.total_market_value = total_market_value;
    }

    public Integer getSquad_size() {
        return squad_size;
    }

    public void setSquad_size(Integer squad_size) {
        this.squad_size = squad_size;
    }

    public Double getAverage_age() {
        return average_age;
    }

    public void setAverage_age(Double average_age) {
        this.average_age = average_age;
    }

    public Integer getForeigners_number() {
        return foreigners_number;
    }

    public void setForeigners_number(Integer foreigners_number) {
        this.foreigners_number = foreigners_number;
    }

    public Double getForeigners_percentage() {
        return foreigners_percentage;
    }

    public void setForeigners_percentage(Double foreigners_percentage) {
        this.foreigners_percentage = foreigners_percentage;
    }

    public Integer getNational_team_players() {
        return national_team_players;
    }

    public void setNational_team_players(Integer national_team_players) {
        this.national_team_players = national_team_players;
    }

    public String getStadium_name() {
        return stadium_name;
    }

    public void setStadium_name(String stadium_name) {
        this.stadium_name = stadium_name;
    }

    public Integer getStadium_seats() {
        return stadium_seats;
    }

    public void setStadium_seats(Integer stadium_seats) {
        this.stadium_seats = stadium_seats;
    }

    public String getNet_transfer_record() {
        return net_transfer_record;
    }

    public void setNet_transfer_record(String net_transfer_record) {
        this.net_transfer_record = net_transfer_record;
    }

    public String getCoach_name() {
        return coach_name;
    }

    public void setCoach_name(String coach_name) {
        this.coach_name = coach_name;
    }

    public Integer getLast_season() {
        return last_season;
    }

    public void setLast_season(Integer last_season) {
        this.last_season = last_season;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCrest_url() {
        return crest_url;
    }

    public void setCrest_url(String crest_url) {
        this.crest_url = crest_url;
    }

    @JsonIgnore
    public byte[] getCrestData() {
        return crestData;
    }

    public void setCrestData(byte[] crestData) {
        this.crestData = crestData;
    }
}
