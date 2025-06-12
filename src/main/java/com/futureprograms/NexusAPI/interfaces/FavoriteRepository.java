package com.futureprograms.NexusAPI.interfaces;

import com.futureprograms.NexusAPI.models.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    List<Favorite> findByUserId(String userId);
    boolean existsByUserIdAndConstellationId(String userId, Integer constellationId);
    Favorite findByUserIdAndConstellationId(String userId, Integer constellationId);
}