package com.futureprograms.NexusAPI.controllers;

import com.futureprograms.NexusAPI.model.Constellation;
import com.futureprograms.NexusAPI.interfaces.ConstellationRepository;
import com.futureprograms.NexusAPI.model.Star;
import com.futureprograms.NexusAPI.model.StarDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/Constellations")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ConstellationController {

    private final ConstellationRepository constellationRepository;

    public ConstellationController(ConstellationRepository constellationRepository) {
        this.constellationRepository = constellationRepository;
    }

    @GetMapping
    public List<Constellation> getAll() {
        return constellationRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Constellation> getById(@PathVariable Integer id) {
        return constellationRepository.findById(id);
    }

    /*@GetMapping("/GetStars/{id}")
    public List<Star> getStars(@PathVariable Integer id) {
        return constellationRepository.findById(id)
                .map(constellation -> List.copyOf(constellation.getStars()))
                .orElse(List.of());
    }*/

    @GetMapping("/GetStars/{id}")
    public ResponseEntity<?> getStars(@PathVariable Integer id) {
        Optional<Constellation> constellation = constellationRepository.findById(id);
        if (constellation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Esa Constelación no Existe.");
        }

        // Si quieres devolver un DTO personalizado de estrellas, crea una clase StarDTO y haz el mapeo aquí.
        Set<Star> stars = new HashSet<>(constellation.get().getStars());

        List<StarDTO> dtoList = stars.stream()
                .map(s -> new StarDTO(
                        s.getId(),
                        s.getX(),
                        s.getY(),
                        s.getZ(),
                        s.getRa(),
                        s.getDec(),
                        s.getMag(),
                        s.getProper(),
                        s.getAz(),
                        s.getAlt(),
                        s.getSpect()
                ))
                .toList();



        // Si solo necesitas los datos actuales de Star:
        // return ResponseEntity.ok(dtoList);
        return ResponseEntity.ok(stars.size());

    }

    @PostMapping
    public Constellation create(@RequestBody Constellation constellation) {
        return constellationRepository.save(constellation);
    }

    @PutMapping("/{id}")
    public Constellation update(@PathVariable Integer id, @RequestBody Constellation constellation) {
        constellation.setId(id);
        return constellationRepository.save(constellation);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        constellationRepository.deleteById(id);
    }
}