// src/main/java/com/futureprograms/NexusAPI/repository/UserRepository.java
package com.futureprograms.NexusAPI.interfaces;

import com.futureprograms.NexusAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    boolean existsByNick(String nick);
}