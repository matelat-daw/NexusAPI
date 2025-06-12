package com.futureprograms.NexusAPI.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Favorites")
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String userId;
    private Integer constellationId;

    @ManyToOne
    @JoinColumn(name = "UserId", referencedColumnName = "Id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;
}