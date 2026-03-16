package com.futureprograms.NexusAPI.service;

import com.futureprograms.NexusAPI.models.User;
import com.futureprograms.NexusAPI.interfaces.UserRepository;
import org.springframework.stereotype.Service;

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
}