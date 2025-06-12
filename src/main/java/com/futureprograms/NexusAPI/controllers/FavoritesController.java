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
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/Account/Favorites")
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<?> isFavorite(@PathVariable int id, @CookieValue("token") String token) {
        User user = userTokenService.getUserFromToken(token);
        if (user == null) {
            return ResponseEntity.status(404).body("ERROR: Ese Usuario no Existe.");
        }

        boolean favorite = favoriteRepository.existsByUserIdAndConstellationId(user.getId(), id);
        return ResponseEntity.ok(favorite);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}")
    public ResponseEntity<?> addToFavorites(@PathVariable int id, @CookieValue("token") String token) {
        User user = userTokenService.getUserFromToken(token);
        if (user == null) {
            return ResponseEntity.status(404).body("ERROR: Ese Usuario no Existe.");
        }

        boolean exists = favoriteRepository.existsByUserIdAndConstellationId(user.getId(), id);
        if (exists) {
            return ResponseEntity.badRequest().body("La constelación ya está en tus favoritos.");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(user.getId());
        favorite.setConstellationId(id);
        favoriteRepository.save(favorite);

        return ResponseEntity.ok("Constelación Agregada a Favoritos.");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFavorite(@PathVariable int id, @CookieValue("token") String token) {
        User user = userTokenService.getUserFromToken(token);
        if (user == null) {
            return ResponseEntity.status(404).body("ERROR: Ese Usuario no Existe.");
        }

        Favorite favoriteToRemove = favoriteRepository.findByUserIdAndConstellationId(user.getId(), id);
        if (favoriteToRemove == null) {
            return ResponseEntity.status(404).body("No se encontró el favorito para eliminar.");
        }

        favoriteRepository.delete(favoriteToRemove);
        return ResponseEntity.ok("Constelación Eliminada de Favoritos.");
    }
}