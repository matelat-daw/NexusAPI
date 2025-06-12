package com.futureprograms.NexusAPI.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "smtp")
public class SmtpSettings {
    private String host;
    private int port;
    private String username;
    private String password;
    private boolean enableSsl;
}