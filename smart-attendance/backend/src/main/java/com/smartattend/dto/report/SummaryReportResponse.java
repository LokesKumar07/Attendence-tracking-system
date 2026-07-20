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
public class SummaryReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private String subjectName;
    private List<StudentReportRow> rows;
}
