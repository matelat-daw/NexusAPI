package com.futureprograms.NexusAPI.interfaces;

import com.futureprograms.NexusAPI.model.Star;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StarRepository extends JpaRepository<Star, Integer> {
    Optional<Star> findById(Integer id);
}