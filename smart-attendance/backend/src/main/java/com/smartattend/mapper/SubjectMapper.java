package com.smartattend.mapper;

import com.smartattend.dto.subject.SubjectResponse;
import com.smartattend.entity.Subject;

public class SubjectMapper {

    public static SubjectResponse toResponse(Subject s) {
        if (s == null) return null;
        return SubjectResponse.builder()
                .id(s.getId())
                .code(s.getCode())
                .name(s.getName())
                .type(s.getType())
                .semesterId(s.getSemester().getId())
                .semesterName(s.getSemester().getName())
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
