package com.smartattend.dto.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
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
public class StudentRequest {
    @NotBlank(message = "Register number is required")
    private String registerNumber;

    @NotBlank(message = "Roll number is required")
    private String rollNumber;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Academic Year ID is required")
    private Long academicYearId;

    @NotNull(message = "Semester ID is required")
    private Long semesterId;

    @NotNull(message = "Class Section ID is required")
    private Long classSectionId;

    @NotNull(message = "Year of study is required")
    @Min(value = 1, message = "Year must be at least 1")
    private Integer yearOfStudy;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;
    private String photographUrl;
    private Boolean isActive;
    private java.util.List<Long> subjectIds;
}
