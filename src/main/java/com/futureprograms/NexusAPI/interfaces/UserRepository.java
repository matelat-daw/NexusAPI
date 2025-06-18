package com.futureprograms.NexusAPI.interfaces;

import com.futureprograms.NexusAPI.models.User;
import com.futureprograms.NexusAPI.models.UserBasicDto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    User findByNick(String nick);
    boolean existsByNick(String nick);
    boolean existsById(String id);

    @Query("SELECT new com.futureprograms.NexusAPI.models.UserBasicDto(u.id, u.email, u.nick, u.name, u.surname1, u.surname2, u.phone, u.profileImage, u.emailConfirmed) FROM User u")
    List<UserBasicDto> findAllBasicUsers();
}