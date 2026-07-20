package com.smartattend.mapper;

import com.smartattend.dto.timetable.TimetableEntryResponse;
import com.smartattend.dto.timetable.TimetableResponse;
import com.smartattend.entity.Timetable;
import com.smartattend.entity.TimetableEntry;

public class TimetableMapper {

    public static TimetableResponse toResponse(Timetable t) {
        if (t == null) return null;
        return TimetableResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .academicYearId(t.getAcademicYear().getId())
                .academicYearName(t.getAcademicYear().getName())
                .semesterId(t.getSemester().getId())
                .semesterName(t.getSemester().getName())
                .isActive(t.getIsActive())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    public static TimetableEntryResponse toEntryResponse(TimetableEntry e) {
        if (e == null) return null;
        return TimetableEntryResponse.builder()
                .id(e.getId())
                .timetableId(e.getTimetable().getId())
                .dayOrder(e.getDayOrder())
                .periodNumber(e.getPeriodNumber())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .subjectId(e.getSubject() != null ? e.getSubject().getId() : null)
                .subjectCode(e.getSubject() != null ? e.getSubject().getCode() : null)
                .subjectName(e.getSubject() != null ? e.getSubject().getName() : null)
                .entryType(e.getEntryType())
                .build();
    }
}
