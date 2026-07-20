package com.smartattend.dto.timetable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableResponse {
    private Long id;
    private String name;
    private Long academicYearId;
    private String academicYearName;
    private Long semesterId;
    private String semesterName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
