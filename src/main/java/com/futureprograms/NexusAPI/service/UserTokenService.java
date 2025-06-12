package com.futureprograms.NexusAPI.service;

import com.futureprograms.NexusAPI.models.User;
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
    private final JwtService jwtService; // Inyecta JwtService

    public UserTokenService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public User getUserFromToken(String token) {
        String userId = jwtService.getUserId(token);
        if (userId == null) return null;
        return userRepository.findById(userId).orElse(null);
    }

    /*private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .findFirst()
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);
    }*/
}