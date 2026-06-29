package com.centresportifets.athlets_backend.auth.dto;

import com.centresportifets.athlets_backend.auth.UserAccount;

import lombok.Data;

@Data
public class AuthUser {
    private long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private long accessLevel;

    public AuthUser(UserAccount userAccount) {
        this.id = userAccount.getId();
        this.username = userAccount.getUsername();
        this.lastName = userAccount.getLastName();
        this.firstName = userAccount.getFirstName();
        this.email = userAccount.getEmail();
        this.accessLevel = userAccount.getAccessLevel();
    }
}
