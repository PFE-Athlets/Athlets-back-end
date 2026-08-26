package com.centresportifets.athlets_backend.result;

public enum ResultStatus {
    ASSIGNED("Assigned"),
    PENDING("Pending approval"),
    APPROVED("Accepted"),
    REJECTED("Rejected");

    private final String status;

    private ResultStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public static ResultStatus fromStatus(String status) {
        if (status == null || status.isBlank()) {
            return ASSIGNED;
        }

        for (ResultStatus value : values()) {
            if (value.status.equalsIgnoreCase(status)) {
                return value;
            }
        }

        if ("Approved".equalsIgnoreCase(status)) {
            return APPROVED;
        }

        return ASSIGNED;
    }
}
