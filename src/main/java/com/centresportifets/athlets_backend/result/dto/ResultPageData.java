package com.centresportifets.athlets_backend.result.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultPageData {
    private List<ResultRowData> results;
    private FilterOptions filters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterOptions {
        private LocalDate minDate;
        private LocalDate maxDate;
        private List<NamedOption> athletes;
        private List<NamedOption> tests;
        private List<NamedOption> teams;
        private List<NamedOption> batteries;
        private List<StatusOption> statuses;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NamedOption {
        private Long id;
        private String label;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusOption {
        private String code;
        private String label;
    }
}
