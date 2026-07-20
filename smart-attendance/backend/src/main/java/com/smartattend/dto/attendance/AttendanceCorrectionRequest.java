package com.smartattend.dto.attendance;

import com.smartattend.entity.AttendanceRecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCorrectionRequest {
    @NotNull(message = "Attendance Record ID is required")
    private Long recordId;

    @NotNull(message = "New status is required")
    private AttendanceRecordStatus newStatus;

    @NotBlank(message = "Correction reason is required")
    private String reason;
}
