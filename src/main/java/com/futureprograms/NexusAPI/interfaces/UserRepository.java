package com.futureprograms.NexusAPI.interfaces;

import com.futureprograms.NexusAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    boolean existsByNick(String nick);
    boolean existsById(String id);
}