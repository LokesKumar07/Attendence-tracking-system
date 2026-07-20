package com.smartattend.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSubmitRequest {
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotEmpty(message = "Attendance records list cannot be empty")
    private List<AttendanceRecordRequest> records;
    
    private boolean isDraft; // If true, sets session to DRAFT. If false, SUBMITTED.
}
