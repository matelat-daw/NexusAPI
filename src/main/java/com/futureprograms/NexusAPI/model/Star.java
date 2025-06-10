package com.futureprograms.NexusAPI.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "stars")
public class Star {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double x;
    private Double y;
    private Double z;

    private String ra;
    private String dec;
    private String mag;
    private String ci;
    private String bf;
    private String hr;
    private String proper;
    private Double az;
    private Double alt;
    private String hip;
    private String hd;
    private String gl;
    private String dist;
    private String pmra;
    private String pmdec;
    private String rv;
    private String absmag;
    private String spect;
    private String vx;
    private String vy;
    private String vz;
    private String rarad;
    private String decrad;
    private String pmrarad;
    private String pmdecrad;
    private String bayer;
    private String flam;
    private String con;
    private String comp;
    private String comp_primary;

    @Column(name = "base")
    private String base;

    private String lum;
    private String var;
    private String var_min;
    private String var_max;
    private String x_gal;
    private String y_gal;
    private String z_gal;

    @ManyToMany
    @JoinTable(
            name = "constellation_stars",
            joinColumns = @JoinColumn(name = "star_id"),
            inverseJoinColumns = @JoinColumn(name = "constellation_id")
    )
    @JsonIgnore
    private Set<Constellation> constellations;
}