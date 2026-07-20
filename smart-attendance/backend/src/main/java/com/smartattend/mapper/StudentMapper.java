package com.smartattend.mapper;

import com.smartattend.dto.student.StudentResponse;
import com.smartattend.entity.Student;

public class StudentMapper {

    public static StudentResponse toResponse(Student s) {
        if (s == null) return null;
        return StudentResponse.builder()
                .id(s.getId())
                .registerNumber(s.getRegisterNumber())
                .rollNumber(s.getRollNumber())
                .name(s.getName())
                .departmentId(s.getDepartment().getId())
                .departmentName(s.getDepartment().getName())
                .departmentCode(s.getDepartment().getCode())
                .academicYearId(s.getAcademicYear().getId())
                .academicYearName(s.getAcademicYear().getName())
                .semesterId(s.getSemester().getId())
                .semesterName(s.getSemester().getName())
                .classSectionId(s.getClassSection().getId())
                .classSectionName(s.getClassSection().getName())
                .yearOfStudy(s.getYearOfStudy())
                .email(s.getEmail())
                .phoneNumber(s.getPhoneNumber())
                .photographUrl(s.getPhotographUrl())
                .isActive(s.getIsActive())
                .subjectIds(s.getEnrolledSubjects() == null ? java.util.Collections.emptyList() :
                        s.getEnrolledSubjects().stream().map(com.smartattend.entity.Subject::getId).collect(java.util.stream.Collectors.toList()))
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
