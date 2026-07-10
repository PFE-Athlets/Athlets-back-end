package com.centresportifets.athlets_backend.result.dto;

import java.time.LocalDate;

import com.centresportifets.athlets_backend.result.Result;

import lombok.Data;

@Data
public class TestData {
    private Long id;
    private Long athleteId;
    private String resultValue;
    private String videoProof;
    private String photoProof;
    private String status;
    private String commentText;
    private LocalDate testDate;
    private Long physicalTestId;
    private String physicalTestName;
    private String unit;
    private String protocol;
    private String proofNeeded;

    public TestData(Result result) {
        this.id = result.getId();
        this.athleteId = result.getAthlete().getId();
        this.resultValue = result.getResultValue();
        this.videoProof = result.getVideoProof();
        this.photoProof = result.getPhotoProof();
        this.status = result.getStatus();
        this.commentText = result.getCommentText();
        this.testDate = result.getTestDate();
        this.physicalTestId = result.getTest().getId();
        this.physicalTestName = result.getTest().getName();
        this.unit = result.getTest().getUnit();
        this.protocol = result.getTest().getProtocol();
        this.proofNeeded = result.getTest().getProof();
    }
}
