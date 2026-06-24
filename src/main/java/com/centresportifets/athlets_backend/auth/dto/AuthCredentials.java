package com.centresportifets.athlets_backend.auth.dto;

import lombok.Data;

@Data
public class AuthCredentials {
    private String username;
    private String password;

    public AuthCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
}