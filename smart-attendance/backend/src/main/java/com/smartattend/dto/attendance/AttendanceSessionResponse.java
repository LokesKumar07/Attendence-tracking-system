package com.smartattend.dto.attendance;

import com.smartattend.entity.AttendanceSessionStatus;
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
public class AttendanceSessionResponse {
    private Long id;
    private LocalDate attendanceDate;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Long periodId;
    private Integer periodNumber;
    private String periodTimeRange;
    private AttendanceSessionStatus status;
    private String createdByTeacher;
    private List<RecordResponse> records;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordResponse {
        private Long recordId;
        private Long studentId;
        private String registerNumber;
        private String rollNumber;
        private String name;
        private String status; // PRESENT, ABSENT, LATE, ON_DUTY, UNMARKED
        private String remarks;
    }
}
