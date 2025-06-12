package com.futureprograms.NexusAPI.controllers;

import com.futureprograms.NexusAPI.interfaces.UserRepository;
import com.futureprograms.NexusAPI.models.Comment;
import com.futureprograms.NexusAPI.interfaces.CommentsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/Account")
public class CommentsController {

    private final CommentsRepository commentsRepository;
    private final UserRepository userRepository;

    public CommentsController(CommentsRepository commentsRepository, UserRepository userRepository) {
        this.commentsRepository = commentsRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/GetComments/{id}")
    public List<Comment> getComments(@PathVariable int id) {
        return commentsRepository.findByConstellationId(id);
    }

    @GetMapping("/GetUserComments/{id}")
    public ResponseEntity<?> getUserComments(@PathVariable String id) {
        boolean userExists = userRepository.existsById(id);
        if (!userExists) {
            return ResponseEntity.status(404).body("ERROR: Ese Usuario no Existe.");
        }

        List<Comment> userComments = commentsRepository.findByUserId(id);
        // Si quieres devolver solo ciertos campos, puedes mapearlos a un DTO:
        List<Map<String, Object>> result = userComments.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("userId", c.getUserId());
            map.put("comment", c.getComment());
            map.put("constellationId", c.getConstellationId());
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    }
}