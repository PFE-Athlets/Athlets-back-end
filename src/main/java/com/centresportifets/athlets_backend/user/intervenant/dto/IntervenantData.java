package com.centresportifets.athlets_backend.user.intervenant.dto;

import java.time.LocalDate;
import java.util.List;
import com.centresportifets.athlets_backend.user.UserType;

public record IntervenantData(
        Long id, String firstName, String lastName, String email, String phone, String username,
        String accountStatus, LocalDate accountCreationDate, int accessLevel, UserType role,
        boolean accountActivated, List<Long> teamIds) {}
