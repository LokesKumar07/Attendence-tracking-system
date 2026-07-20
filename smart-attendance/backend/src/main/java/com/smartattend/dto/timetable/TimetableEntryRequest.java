package com.smartattend.dto.timetable;

import com.smartattend.entity.TimetableEntryType;
import jakarta.validation.constraints.NotNull;
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
public class TimetableEntryRequest {
    @NotNull(message = "Timetable ID is required")
    private Long timetableId;

    @NotNull(message = "Day order is required")
    @jakarta.validation.constraints.Min(value = 1, message = "Day order must be at least 1")
    @jakarta.validation.constraints.Max(value = 6, message = "Day order must be at most 6")
    private Integer dayOrder;

    @NotNull(message = "Period number is required")
    private Integer periodNumber;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private Long subjectId; // null if BREAK/LUNCH/FREE

    @NotNull(message = "Entry type is required")
    private TimetableEntryType entryType;
}
