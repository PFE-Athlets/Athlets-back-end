package com.centresportifets.athlets_backend.auth.dto;

import com.centresportifets.athlets_backend.user.UserAccount;

import lombok.Data;

@Data
public class AuthUser {
    private long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private long accessLevel;
    private String accountStatus;

    public AuthUser(UserAccount userAccount) {
        this.id = userAccount.getId();
        this.username = userAccount.getUsername();
        this.lastName = userAccount.getLastName();
        this.firstName = userAccount.getFirstName();
        this.email = userAccount.getEmail();
        this.phone = userAccount.getPhone();
        this.accessLevel = userAccount.getAccessLevel();
        this.accountStatus = userAccount.getAccountStatus();
    }
}
