package com.futureprograms.NexusAPI.models;

import java.time.LocalDate;
import java.util.List;

public class UserInfoDto {

    public UserInfoDto(User user) {
        this.nick = user.getNick();
        this.email = user.getEmail();
        this.name = user.getName();
        this.surname1 = user.getSurname1();
        this.surname2 = user.getSurname2();
        this.phoneNumber = user.getPhone();
        this.bday = user.getBday();
        this.userLocation = user.getUserLocation();
        this.profileImage = user.getProfileImage();
        this.publicProfile = user.getPublicProfile();
        this.about = user.getAbout();
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