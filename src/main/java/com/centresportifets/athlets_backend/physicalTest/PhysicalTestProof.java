package com.centresportifets.athlets_backend.physicalTest;

public enum PhysicalTestProof {
    NONE("None"),
    PHOTO("Photo"),
    VIDEO("Video"),
    BOTH("Both");

    private final String proof;

    private PhysicalTestProof(String proof) {
        this.proof = proof;
    }

    public String getProof() {
        return this.proof;
    }
}