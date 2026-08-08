package com.centresportifets.athlets_backend.result.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultRowData {
    private Long id;
    private LocalDate testDate;
    private String statusCode;
    private String statusLabel;
    private String commentText;
    private String proof;
    private AthleteSummary athlete;
    private NamedEntitySummary test;
    private NamedEntitySummary team;
    private NamedEntitySummary battery;
    private IntervenantSummary intervenant;
    private List<ValueSummary> resultValues;
    private String resultValueSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AthleteSummary {
        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        private String displayName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NamedEntitySummary {
        private Long id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntervenantSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String displayName;
        private String role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueSummary {
        private Long resultTypeId;
        private String resultTypeName;
        private String formattedValue;
        private String unitSymbol;
    }
}
