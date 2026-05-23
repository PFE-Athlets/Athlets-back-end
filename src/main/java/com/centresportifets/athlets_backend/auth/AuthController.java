package com.centresportifets.athlets_backend.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authentication controller", description = "Handles basic user authentication flow and account creation")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static class AuthCredentials{
        private String userName;
        private String password;
    }

    @PostMapping("/login")
    public String loginUser(@RequestBody AuthCredentials credentials){

        return credentials.userName;
    } 
}
