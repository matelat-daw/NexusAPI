package com.futureprograms.NexusAPI.controllers;

import com.futureprograms.NexusAPI.models.Favorite;
import com.futureprograms.NexusAPI.service.UserTokenService;
import com.futureprograms.NexusAPI.models.User;
import com.futureprograms.NexusAPI.interfaces.FavoriteRepository;
import com.futureprograms.NexusAPI.interfaces.ConstellationRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/Favorites")
public class FavoritesController {

    private final UserTokenService userTokenService;
    private final FavoriteRepository favoriteRepository;
    private final ConstellationRepository constellationRepository;

    public FavoritesController(UserTokenService userTokenService,
                               FavoriteRepository favoriteRepository,
                               ConstellationRepository constellationRepository) {
        this.userTokenService = userTokenService;
        this.favoriteRepository = favoriteRepository;
        this.constellationRepository = constellationRepository;
    }

    @GetMapping
    public ResponseEntity<?> getFavorites(@CookieValue("token") String token) {
        User user = userTokenService.getUserFromToken(token);
        if (user == null) {
            return ResponseEntity.status(404).body("ERROR: Ese Usuario no Existe.");
        }

        List<Integer> favoriteIds = favoriteRepository.findByUserId(user.getId())
                .stream()
                .map(Favorite::getConstellationId)
                .collect(Collectors.toList());

        List<Map<String, Object>> favoriteConstellations = constellationRepository.findByIdIn(favoriteIds)
                .stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("Nombre", c.getLatinName());
                    map.put("Mitologia", c.getMythology());
                    map.put("Imagen", c.getImageUrl());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(favoriteConstellations);
    }
}