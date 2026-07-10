package com.centresportifets.athlets_backend.user;

public enum UserType {
    INVALID(0),
    ADMIN(1),
    COACH(2),
    ATHLETE(3);

    private final int permissionId;

    private UserType(int permissionId) {
        this.permissionId = permissionId;
    }

    public int getPermissionLevel() {
        return this.permissionId;
    }
}
