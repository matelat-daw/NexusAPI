package com.futureprograms.NexusAPI.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @Size(min = 3, max = 50, message = "Nick must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "Nick can only contain letters, numbers, underscore and hyphen")
    private String nick;
    
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @Size(min = 2, max = 100, message = "First surname must be between 2 and 100 characters")
    private String surname1;
    
    @Size(max = 100, message = "Second surname must not exceed 100 characters")
    private String surname2;
    
    @Email(message = "Email should be valid")
    private String email;
    
    // Teléfono es opcional
    private String phoneNumber;
    
    // Aceptar fecha como String para mayor flexibilidad (opcional)
    private String bday;
    
    @Size(max = 200, message = "User location must not exceed 200 characters")
    private String userLocation;
    
    // Imagen de perfil es completamente opcional
    private MultipartFile profileImage;
    
    // Perfil público (opcional)
    private String publicProfile;
    
    @Size(max = 1000, message = "About must not exceed 1000 characters")
    private String about;
}
