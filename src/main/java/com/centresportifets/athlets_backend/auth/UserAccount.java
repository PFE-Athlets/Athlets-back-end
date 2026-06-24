package com.centresportifets.athlets_backend.auth;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "user_account")
@Inheritance(strategy = InheritanceType.JOINED)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "account_status", nullable = false, length = 10)
    private String accountStatus = "Active";

    @Column(name = "account_creation_date", nullable = false)
    private LocalDate accountCreationDate = LocalDate.now();

    @Column(name = "access_level", nullable = false)
    private int accessLevel;
}