// src/main/java/com/futureprograms/NexusAPI/interfaces/RoleRepository.java
package com.futureprograms.NexusAPI.interfaces;

import com.futureprograms.NexusAPI.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByName(String name);
}