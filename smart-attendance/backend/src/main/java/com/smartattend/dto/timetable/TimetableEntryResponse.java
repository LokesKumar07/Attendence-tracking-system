package com.smartattend.dto.timetable;

import com.smartattend.entity.TimetableEntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableEntryResponse {
    private Long id;
    private Long timetableId;
    private Integer dayOrder;
    private Integer periodNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private TimetableEntryType entryType;
}
