package com.centresportifets.athlets_backend.user.intervenant.dto;

public record IntervenantUpdateRequest(
        String firstName, String lastName, String email, String phone, String username) {}
