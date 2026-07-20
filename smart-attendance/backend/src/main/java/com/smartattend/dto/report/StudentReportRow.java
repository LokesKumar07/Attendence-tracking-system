package com.smartattend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentReportRow {
    private String registerNumber;
    private String rollNumber;
    private String studentName;
    private int totalConductedClasses;
    private int presentCount;
    private int absentCount;
    private int lateCount;
    private int onDutyCount;
    private int eligibleAttendanceCount; // present + late + onDuty
    private double attendancePercentage;
}
