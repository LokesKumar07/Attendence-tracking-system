package com.smartattend.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCorrectionResponse {
    private Long id;
    private Long recordId;
    private String studentName;
    private String registerNumber;
    private String subjectName;
    private String previousStatus;
    private String newStatus;
    private String correctionReason;
    private String changedByTeacher;
    private LocalDateTime changedAt;
}
