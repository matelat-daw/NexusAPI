package com.futureprograms.NexusAPI.interfaces;

import com.futureprograms.NexusAPI.models.Constellation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ConstellationRepository extends JpaRepository<Constellation, Integer> {
    Optional<Constellation> findById(Integer id);
    List<Constellation> findByIdIn(List<Integer> ids);
}