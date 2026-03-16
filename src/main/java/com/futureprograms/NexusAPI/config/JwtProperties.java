package com.futureprograms.NexusAPI.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String issuer;
    private String audience;
    private String secret;
    private Integer expirationMinutes;
}

