package com.centresportifets.athlets_backend.result.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.centresportifets.athlets_backend.result.Result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestData {
    private Long id;
    private Long athleteId;
    private String athleteFirstName;
    private String athleteLastName;
    private String proof;
    private String status;
    private String commentText;
    private LocalDate testDate;
    private Long physicalTestId;
    private String physicalTestName;
    private String protocol;
    private boolean proofRequired;
    private List<ResultValueDTO> resultValues;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultValueDTO {
        private Long resultTypeId;
        private String resultTypeName;
        private BigDecimal value;
        private String unitName;
        private String unitSymbol;
    }

    public TestData(Result result) {
        this.id = result.getId();
        
        if (result.getAthlete() != null) {
            this.athleteId = result.getAthlete().getId();
            this.athleteFirstName = result.getAthlete().getFirstName();
            this.athleteLastName = result.getAthlete().getLastName();
        }

        this.proof = result.getProof();
        this.status = result.getStatus();
        this.commentText = result.getCommentText();
        this.testDate = result.getTestDate();

        if (result.getTest() != null) {
            this.physicalTestId = result.getTest().getId();
            this.physicalTestName = result.getTest().getName();
            this.protocol = result.getTest().getProtocol();
            this.proofRequired = result.getTest().isProofRequired();
        }

        if (result.getResultValues() != null) {
            this.resultValues = result.getResultValues().stream()
                .map(rv -> new ResultValueDTO(
                    rv.getResultType().getId(),
                    rv.getResultType().getName(),
                    rv.getValue(),
                    rv.getResultType().getUnitMeasure() != null ? rv.getResultType().getUnitMeasure().getName() : null,
                    rv.getResultType().getUnitMeasure() != null ? rv.getResultType().getUnitMeasure().getSymbol() : null
                ))
                .toList();
        } else {
            this.resultValues = List.of();
        }
    }
}