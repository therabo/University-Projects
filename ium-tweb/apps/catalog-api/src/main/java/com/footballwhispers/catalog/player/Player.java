package com.footballwhispers.catalog.player;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
@Entity
@Table(name = "players")
public class Player {

    @Id
    @Column(name = "player_id")
    private Long player_id;

    @Column(name = "first_name")
    private String first_name;

    @Column(name = "last_name")
    private String last_name;

    @Column(name = "name")
    private String name;

    @Column(name = "last_season")
    private Integer last_season;

    @Column(name = "current_club_id")
    private Integer current_club_id;

    @Column(name = "player_code")
    private String player_code;

    @Column(name = "country_of_birth")
    private String country_of_birth;

    @Column(name = "city_of_birth")
    private String city_of_birth;

    @Column(name = "country_of_citizenship")
    private String country_of_citizenship;

    @Column(name = "date_of_birth")
    private LocalDate date_of_birth;

    @Column(name = "sub_position")
    private String sub_position;

    @Column(name = "position")
    private String position;

    @Column(name = "foot")
    private String foot;

    @Column(name = "height_in_cm")
    private BigDecimal height_in_cm;

    @Column(name = "market_value_in_eur")
    private BigDecimal market_value_in_eur;

    @Column(name = "highest_market_value_in_eur")
    private BigDecimal highest_market_value_in_eur;

    @Column(name = "contract_expiration_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private String contract_expiration_date;

    @Column(name = "agent_name")
    private String agent_name;

    @Column(name = "image_url")
    private URL image_url;

    @Column(name = "url")
    private URL url;

    @Column(name = "current_club_domestic_competition_id")
    private String current_club_domestic_competition_id;

    @Column(name = "current_club_name")
    private String current_club_name;

    public Player() {
    }

    public long getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(Long player_id) {
        this.player_id = player_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLast_season() {
        return last_season;
    }

    public void setLast_season(Integer last_season) {
        this.last_season = last_season;
    }

    public Integer getCurrent_club_id() {
        return current_club_id;
    }

    public void setCurrent_club_id(Integer current_club_id) {
        this.current_club_id = current_club_id;
    }

    public String getPlayer_code() {
        return player_code;
    }

    public void setPlayer_code(String player_code) {
        this.player_code = player_code;
    }

    public String getCountry_of_birth() {
        return country_of_birth;
    }

    public void setCountry_of_birth(String country_of_birth) {
        this.country_of_birth = country_of_birth;
    }

    public String getCity_of_birth() {
        return city_of_birth;
    }

    public void setCity_of_birth(String city_of_birth) {
        this.city_of_birth = city_of_birth;
    }

    public String getCountry_of_citizenship() {
        return country_of_citizenship;
    }

    public void setCountry_of_citizenship(String country_of_citizenship) {
        this.country_of_citizenship = country_of_citizenship;
    }

    public LocalDate getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(LocalDate date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getSub_position() {
        return sub_position;
    }

    public void setSub_position(String sub_position) {
        this.sub_position = sub_position;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getFoot() {
        return foot;
    }

    public void setFoot(String foot) {
        this.foot = foot;
    }

    public BigDecimal getHeight_in_cm() {
        return height_in_cm;
    }

    public void setHeight_in_cm(BigDecimal height_in_cm) {
        this.height_in_cm = height_in_cm;
    }

    public BigDecimal getMarket_value_in_eur() {
        return market_value_in_eur;
    }

    public void setMarket_value_in_eur(BigDecimal market_value_in_eur) {
        this.market_value_in_eur = market_value_in_eur;
    }

    public BigDecimal getHighest_market_value_in_eur() {
        return highest_market_value_in_eur;
    }

    public void setHighest_market_value_in_eur(BigDecimal highest_market_value_in_eur) {
        this.highest_market_value_in_eur = highest_market_value_in_eur;
    }

    public String getContract_expiration_date() {
        return contract_expiration_date;
    }

    public void setContract_expiration_date(String contract_expiration_date) {
        this.contract_expiration_date = contract_expiration_date;
    }

    public String getAgent_name() {
        return agent_name;
    }

    public void setAgent_name(String agent_name) {
        this.agent_name = agent_name;
    }

    public URL getImage_url() {
        return image_url;
    }

    public void setImage_url(URL image_url) {
        this.image_url = image_url;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getCurrent_club_domestic_competition_id() {
        return current_club_domestic_competition_id;
    }

    public void setCurrent_club_domestic_competition_id(String current_club_domestic_competition_id) {
        this.current_club_domestic_competition_id = current_club_domestic_competition_id;
    }

    public String getCurrent_club_name() {
        return current_club_name;
    }

    public void setCurrent_club_name(String current_club_name) {
        this.current_club_name = current_club_name;
    }

}
