package com.futureprograms.NexusAPI.interfaces;

import com.futureprograms.NexusAPI.models.User;
import com.futureprograms.NexusAPI.models.UserBasicDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    boolean existsByNick(String nick);
    User findByNick(String nick);

    @EntityGraph(attributePaths = {"favorites", "roles"})
    Optional<User> findById(String id);

    @Query("SELECT new com.futureprograms.NexusAPI.models.UserBasicDto(u.id, u.nick, u.profileImage) FROM User u")
    List<UserBasicDto> findAllBasicUsers();
}