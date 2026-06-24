package com.centresportifets.athlets_backend.auth.userTypes;

public enum UserType {
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
