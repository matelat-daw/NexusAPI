package com.futureprograms.NexusAPI.service;

import com.futureprograms.NexusAPI.model.User;
import com.futureprograms.NexusAPI.interfaces.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Service
public class UserTokenService {

    private final UserRepository userRepository;
    private final String jwtSecret; // Cambia esto por tu clave secreta

    public UserTokenService(UserRepository userRepository, @Value("${jwt.secret}") String jwtSecret) {
        this.userRepository = userRepository;
        this.jwtSecret = jwtSecret;
    }

    public User getUserFromToken(HttpServletRequest request) {
        String token = getTokenFromCookie(request, "jwt");
        if (token == null) return null;

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject();
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .findFirst()
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);
    }
}