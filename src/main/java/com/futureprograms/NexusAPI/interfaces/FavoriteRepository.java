package com.futureprograms.NexusAPI.interfaces;

import com.futureprograms.NexusAPI.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    List<Favorite> findByUserId(String userId);
}