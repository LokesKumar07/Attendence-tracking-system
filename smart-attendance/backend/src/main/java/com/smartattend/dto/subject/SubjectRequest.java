package com.smartattend.dto.subject;

import com.smartattend.entity.SubjectType;
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
public class SubjectRequest {
    @NotBlank(message = "Subject code is required")
    private String code;

    @NotBlank(message = "Subject name is required")
    private String name;

    @NotNull(message = "Subject type (THEORY, LABORATORY) is required")
    private SubjectType type;

    @NotNull(message = "Semester ID is required")
    private Long semesterId;

    private Boolean isActive;
}
