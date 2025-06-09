package com.futureprograms.NexusAPI.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.futureprograms.NexusAPI.model.User;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final String issuer;
    private final String audience;
    private final int expirationMinutes;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.audience}") String audience,
            @Value("${jwt.expiration-minutes}") int expirationMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(User user, List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            roles = List.of("Basic");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("userId", user.getId());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (long) expirationMinutes * 60 * 1000))
                .setId(UUID.randomUUID().toString())
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserId(String token) {
        var claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object userId = claims.get("userId");
        return userId != null ? userId.toString() : null;
    }
}