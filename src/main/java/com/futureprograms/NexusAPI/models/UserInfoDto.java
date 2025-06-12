package com.futureprograms.NexusAPI.models;

import java.time.LocalDate;
import java.util.List;

public class UserInfoDto {

    public UserInfoDto(User user) {
    }

    public String nick;
    public String name;
    public String surname1;
    public String surname2;
    public String email;
    public String phoneNumber;
    public String profileImage;
    public LocalDate bday;
    public String about;
    public String userLocation;
    public Boolean publicProfile;
    public List<?> favorites;
    public List<?> comments;
}