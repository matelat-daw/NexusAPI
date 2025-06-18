package com.futureprograms.NexusAPI.models;

public class UserBasicDto {
    public String id;
    public String email;
    public String nick;
    public String name;
    public String surname1;
    public String surname2;
    public String phone;
    public String profileImage;
    public boolean emailConfirmed;

    public UserBasicDto(String id, String email, String nick, String name, String surname1, String surname2, String phone, String profileImage, boolean emailConfirmed) {
        this.id = id;
        this.email = email;
        this.nick = nick;
        this.name = name;
        this.surname1 = surname1;
        this.surname2 = surname2;
        this.phone = phone;
        this.profileImage = profileImage;
        this.emailConfirmed = emailConfirmed;
    }
}