package com.centresportifets.athlets_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AuthCredentials {
    @Schema(defaultValue = "elaforce0")
    private String username;
    @Schema(defaultValue = "admin1")
    private String password;

    public AuthCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
}