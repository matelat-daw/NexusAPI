package com.futureprograms.NexusAPI.model;

import lombok.Data;

@Data
public class StarDTO {
    private Integer id;
    private Double x;
    private Double y;
    private Double z;
    private String ra;
    private String dec;
    private String mag;
    private String proper;
    private Double az;
    private Double alt;
    private String spect;

    public StarDTO(Integer id, Double x, Double y, Double z, String ra, String dec, String mag,
                   String proper, Double az, Double alt, String spect) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.ra = ra;
        this.dec = dec;
        this.mag = mag;
        this.proper = proper;
        this.az = az;
        this.alt = alt;
        this.spect = spect;
    }
}