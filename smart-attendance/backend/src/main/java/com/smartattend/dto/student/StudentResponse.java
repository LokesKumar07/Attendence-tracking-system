package com.smartattend.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String registerNumber;
    private String rollNumber;
    private String name;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Long academicYearId;
    private String academicYearName;
    private Long semesterId;
    private String semesterName;
    private Long classSectionId;
    private String classSectionName;
    private Integer yearOfStudy;
    private String email;
    private String phoneNumber;
    private String photographUrl;
    private Boolean isActive;
    private java.util.List<Long> subjectIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
