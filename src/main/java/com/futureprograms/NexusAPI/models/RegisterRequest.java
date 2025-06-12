package com.futureprograms.NexusAPI.models;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    public String nick;
    public String name;
    public String surname1;
    public String surname2;
    public String email;
    public String password;
    public String phoneNumber;
    public LocalDate bday;
    public String userLocation;
    public MultipartFile profileImage;
    public String publicProfile;
    public String about;
}