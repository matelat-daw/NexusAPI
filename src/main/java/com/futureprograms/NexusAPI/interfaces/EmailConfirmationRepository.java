package com.futureprograms.NexusAPI.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import com.futureprograms.NexusAPI.models.EmailConfirmation;

public interface EmailConfirmationRepository extends JpaRepository<EmailConfirmation, String> {
    EmailConfirmation findByToken(String token);
    EmailConfirmation findByUser_Id(String userId);
}