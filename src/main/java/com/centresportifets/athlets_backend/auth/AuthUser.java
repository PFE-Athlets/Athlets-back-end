package com.centresportifets.athlets_backend.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDate;

@Data
@Table(name = "user_account")
@Entity
public class AuthUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(unique = true, nullable = false, length = 254)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "account_status", nullable = false, length = 10)
    private String accountStatus = "Active";

    @Column(name = "account_creation_date", nullable = false)
    private LocalDate accountCreationDate = LocalDate.now();

    @Column(name = "access_level", nullable = false)
    private Integer accessLevel;
}