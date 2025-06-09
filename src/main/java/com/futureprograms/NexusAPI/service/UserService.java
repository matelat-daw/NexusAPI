package com.futureprograms.NexusAPI.service;

import com.futureprograms.NexusAPI.model.User;
import com.futureprograms.NexusAPI.interfaces.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(User user) {
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User updateUser(String id, User userDetails) {
        User user = getUserById(id);
        user.setEmail(userDetails.getEmail());
        user.setPassword(userDetails.getPassword());
        user.setRoles(userDetails.getRoles());
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}