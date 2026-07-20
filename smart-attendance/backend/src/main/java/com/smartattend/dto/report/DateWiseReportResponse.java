package com.smartattend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateWiseReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<LocalDate> dates; // columns
    private List<StudentDateRow> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentDateRow {
        private String registerNumber;
        private String rollNumber;
        private String studentName;
        private List<String> statuses; // Ordered same as dates, contains P, A, L, OD, -
    }
}
