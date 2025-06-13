package com.futureprograms.NexusAPI.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExternalLoginRequest {
    private String token;

    public ExternalLoginRequest() {}

}