package com.futureprograms.NexusAPI.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Entity
@Getter
@Setter
@ToString(exclude = "users")
@EqualsAndHashCode(of = {"id", "name"})
@Table(name = "AspNetRoles")
public class Role {
    @Id
    @Column(name = "Id")
    private String id;

    @Column(name = "Name")
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
}