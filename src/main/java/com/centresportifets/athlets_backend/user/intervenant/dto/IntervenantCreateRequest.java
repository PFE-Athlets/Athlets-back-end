package com.centresportifets.athlets_backend.user.intervenant.dto;

import com.centresportifets.athlets_backend.user.UserType;

public record IntervenantCreateRequest(
        String firstName, String lastName, String email, String phone, String username, UserType role) {}
