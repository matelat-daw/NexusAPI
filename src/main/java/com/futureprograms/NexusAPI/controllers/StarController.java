package com.futureprograms.NexusAPI.controllers;

import com.futureprograms.NexusAPI.models.Star;
import com.futureprograms.NexusAPI.interfaces.StarRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/Stars")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class StarController {

    private final StarRepository starRepository;

    public StarController(StarRepository starRepository) {
        this.starRepository = starRepository;
    }

    @GetMapping
    public List<Star> getAll() {
        return starRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Star> getById(@PathVariable Integer id) {
        return starRepository.findById(id);
    }

    @PostMapping
    public Star create(@RequestBody Star star) {
        return starRepository.save(star);
    }

    @PutMapping("/{id}")
    public Star update(@PathVariable Integer id, @RequestBody Star star) {
        star.setId(id);
        return starRepository.save(star);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        starRepository.deleteById(id);
    }
}