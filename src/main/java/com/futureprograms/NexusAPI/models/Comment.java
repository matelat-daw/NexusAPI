package com.futureprograms.NexusAPI.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String userNick;
    private String constellationName;
    private String comment;
    private String userId;
    private Integer constellationId;

    @ManyToOne
    @JoinColumn(name = "UserId", referencedColumnName = "Id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;
}