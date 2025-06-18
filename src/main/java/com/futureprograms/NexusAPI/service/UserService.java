package com.futureprograms.NexusAPI.service;

import com.futureprograms.NexusAPI.controllers.UserController;
import com.futureprograms.NexusAPI.models.Role;
import com.futureprograms.NexusAPI.models.User;
import com.futureprograms.NexusAPI.models.RegisterRequest;
import com.futureprograms.NexusAPI.interfaces.UserRepository;
import com.futureprograms.NexusAPI.interfaces.RoleRepository;
import com.futureprograms.NexusAPI.security.AspNetIdentityPasswordVerifier;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDate;
import java.io.IOException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public void register(User user) {
        userRepository.save(user);
    }

    public User verifyUser(String email, String name, String picture) {
        User user = userRepository.findByEmail(email);
        if (user != null) return user;

        String baseNick = email.split("@")[0];
        String nick = baseNick;
        int counter = 1;
        while (userRepository.existsByNick(nick)) {
            nick = baseNick + counter++;
        }

        user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setNick(nick);
        user.setEmail(email);
        user.setName(name);
        user.setSurname1("");
        user.setBday(LocalDate.of(1970,1,1)); // Fecha por defecto
        user.setPhone("");
        user.setEmailConfirmed(true);
        user.setProfileImage(picture);
        user.setPublicProfile(false);

        // Asignar rol "Basic"
        Role basicRole = roleRepository.findByName("Basic");
        if (basicRole != null) {
            user.setRoles(Set.of(basicRole));
        }

        // Guardar usuario
        userRepository.save(user);
        return user;
    }

    public User createUserFromRegisterRequest(RegisterRequest model) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setNick(model.getNick());
        user.setName(model.getName());
        user.setSurname1(model.getSurname1());
        user.setSurname2(nullIfEmpty(model.getSurname2()));
        user.setUserName(model.getEmail());
        user.setEmail(model.getEmail());
        user.setPhone(nullIfEmpty(model.getPhoneNumber()));
        String profileImagePath;
        try {
            profileImagePath = UserController.saveProfileImage(model.getProfileImage(), model.getNick());
        } catch (IOException e) {
            profileImagePath = "/imgs/default-profile.jpg";
        }
        user.setProfileImage(profileImagePath);
        user.setBday(model.getBday() != null ? model.getBday() : LocalDate.now());
        user.setAbout(nullIfEmpty(model.getAbout()));
        user.setUserLocation(nullIfEmpty(model.getUserLocation()));
        user.setPublicProfile(model.getPublicProfile() != null && model.getPublicProfile().equals("1"));
        user.setEmailConfirmed(false);
        try {
            user.setPassword(AspNetIdentityPasswordVerifier.hashPassword(model.getPassword()));
        } catch (Exception e) {
            throw new RuntimeException("ERROR: No se pudo procesar la contraseña.");
        }
        Role basicRole = roleRepository.findByName("Basic");
        user.setRoles(Set.of(basicRole));

        return user;
    }

    private String nullIfEmpty(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}