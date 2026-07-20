package com.smartattend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectWiseReportResponse {
    private String registerNumber;
    private String studentName;
    private List<SubjectSummaryRow> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectSummaryRow {
        private String subjectCode;
        private String subjectName;
        private int conducted;
        private int present;
        private int absent;
        private int late;
        private int onDuty;
        private double percentage;
    }
}
