package com.futureprograms.NexusAPI.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "EmailConfirmations")
public class EmailConfirmation {
    @Id
    @Column(name = "Id", length = 36)  // UUID estilo ASP.NET
    private String id = UUID.randomUUID().toString();

    @Column(name = "Token", nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserId", referencedColumnName = "Id")
    private User user;

    @Column(name = "ExpiryDate")
    private LocalDateTime expiryDate;
}