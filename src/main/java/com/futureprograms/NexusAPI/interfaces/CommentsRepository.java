package com.futureprograms.NexusAPI.interfaces;

import com.futureprograms.NexusAPI.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentsRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByConstellationId(int constellationId);
    List<Comment> findByUserId(String userId);
}