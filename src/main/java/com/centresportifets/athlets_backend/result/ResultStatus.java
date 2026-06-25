package com.centresportifets.athlets_backend.result;

public enum ResultStatus {
    ASSIGNED("Assigned"),
    PENDING("Pending approval"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String status;

    private ResultStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}