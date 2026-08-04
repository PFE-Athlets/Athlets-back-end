package com.centresportifets.athlets_backend.result.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultValueSubmissionDTO {
    private Long resultTypeId;
    private BigDecimal value;
}