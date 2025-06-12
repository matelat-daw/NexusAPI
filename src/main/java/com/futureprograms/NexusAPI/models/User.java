
package com.futureprograms.NexusAPI.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "AspNetUsers")
public class User {
    @Id
    @Column(name = "Id")
    private String id;

    @Column(name = "Nick")
    private String nick;

    @Column(name = "Name")
    private String name;

    @Column(name = "Surname1")
    private String surname1;

    @Column(name = "Surname2", nullable = true)
    private String surname2;

    @Column(name = "PhoneNumber", nullable = true)
    private String phone;

    @Column(name = "Bday")
    private LocalDate bday;

    @Column(name = "UserLocation", nullable = true)
    private String userLocation;

    @Column(name = "ProfileImage", nullable = true)
    private String profileImage;

    @Column(name = "PublicProfile")
    private Boolean publicProfile;

    @Column(name = "About", nullable = true)
    private String about;

    @Column(name = "UserName")
    private String userName;

    @Column(name = "Email")
    private String email;

    @Column(name = "PasswordHash")
    private String password;

    @Column(name = "AccessFailedCount", nullable = false)
    private Integer accessFailedCount = 0;

    @Column(name = "EmailConfirmed", nullable = false)
    private Boolean emailConfirmed = false;

    @Column(name = "PhoneNumberConfirmed", nullable = false)
    private Boolean phoneNumberConfirmed = false;

    @Column(name = "TwoFactorEnabled", nullable = false)
    private Boolean twoFactorEnabled = false;

    @Column(name = "LockoutEnabled", nullable = false)
    private Boolean lockoutEnabled = false;

    @Column(name = "LockoutEnd", nullable = true)
    private LocalDate lockoutEnd;

    @Column(name = "SecurityStamp", nullable = true)
    private String securityStamp = java.util.UUID.randomUUID().toString();

    @Column(name = "ConcurrencyStamp", nullable = true)
    private String concurrencyStamp = java.util.UUID.randomUUID().toString();

    @OneToMany(mappedBy = "user")
    private Set<Favorite> favorites;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "AspNetUserRoles",
            joinColumns = @JoinColumn(name = "UserId"),
            inverseJoinColumns = @JoinColumn(name = "RoleId")
    )
    private Set<Role> roles;
}