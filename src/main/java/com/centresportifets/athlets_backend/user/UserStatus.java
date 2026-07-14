package com.centresportifets.athlets_backend.user;

public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    WAITING("Waiting");

    private final String status;

    private UserStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
