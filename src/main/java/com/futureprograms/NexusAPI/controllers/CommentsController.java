package com.futureprograms.NexusAPI.controllers;

import com.futureprograms.NexusAPI.interfaces.ConstellationRepository;
import com.futureprograms.NexusAPI.interfaces.UserRepository;
import com.futureprograms.NexusAPI.models.Constellation;
import com.futureprograms.NexusAPI.models.Comment;
import com.futureprograms.NexusAPI.models.User;
import com.futureprograms.NexusAPI.interfaces.CommentsRepository;
import com.futureprograms.NexusAPI.service.UserTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;

@RestController
@RequestMapping("/api/Account")
public class CommentsController {
    private final UserTokenService userTokenService;
    private final CommentsRepository commentsRepository;
    private final UserRepository userRepository;
    private final ConstellationRepository constellationRepository;

    public CommentsController(CommentsRepository commentsRepository, UserRepository userRepository, UserTokenService userTokenService, ConstellationRepository constellationRepository) {
        this.userTokenService = userTokenService;
        this.commentsRepository = commentsRepository;
        this.userRepository = userRepository;
        this.constellationRepository = constellationRepository;
    }

    @GetMapping("/ById/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCommentById(@PathVariable int id) {
        Comment comment = commentsRepository.findById(id).orElse(null);
        return comment == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(comment);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/GetComments/{id}")
    public List<Comment> getComments(@PathVariable int id) {
        return commentsRepository.findByConstellationId(id);
    }

    @PreAuthorize("isAuthenticated()")
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

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postComment(@RequestBody Comment comment, @CookieValue("token") String token) {
        User user = userTokenService.getUserFromToken(token);
        if (user == null) {
            return ResponseEntity.status(404).body("ERROR: Ese Usuario no Existe.");
        }

        Constellation constellation = constellationRepository.findById(comment.getConstellationId()).orElse(null);
        if (constellation == null) {
            return ResponseEntity.status(404).body("ERROR: La constelación no existe.");
        }

        comment.setUserId(user.getId());
        comment.setConstellationName(constellation.getLatinName());
        Comment savedComment = commentsRepository.save(comment);

        return ResponseEntity
                .created(URI.create("/api/Account/ById/" + savedComment.getId()))
                .body(savedComment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(@PathVariable int id, @CookieValue("token") String token) {
        User user = userTokenService.getUserFromToken(token);
        if (user == null) {
            return ResponseEntity.status(404).body("ERROR: Ese Usuario no Existe.");
        }

        Comment comment = commentsRepository.findById(id).orElse(null);
        if (comment == null) {
            return ResponseEntity.notFound().build();
        }

        commentsRepository.delete(comment);
        return ResponseEntity.noContent().build();
    }
}