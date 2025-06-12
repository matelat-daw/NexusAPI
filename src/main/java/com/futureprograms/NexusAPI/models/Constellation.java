package com.futureprograms.NexusAPI.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "constellations")
public class Constellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;
    @Column(name = "latin_name")
    @JsonProperty("latin_name")
    private String latinName;
    private String english_name;
    private String spanish_name;
    private String mythology;
    private Double area_degrees;
    private String declination;
    private String celestial_zone;
    private String ecliptic_zone;
    private String brightest_star;
    private String discovery;
    private String image_name;
    @Column(name = "image_url")
    @JsonProperty("image_url")
    private String imageUrl;

    @ManyToMany(mappedBy = "constellations")
    @JsonIgnore
    private Set<Star> stars;
}