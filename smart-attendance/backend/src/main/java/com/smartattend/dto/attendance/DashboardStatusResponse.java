package com.smartattend.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatusResponse {
    private LocalDate currentDate;
    private String currentDay;
    private LocalTime currentTime;
    
    private PeriodInfo currentPeriod;
    private PeriodInfo previousPeriod;
    private PeriodInfo nextPeriod;

    private int totalStudents;
    private int completedSessions;
    private int pendingSessions;
    
    private TodaySummary todaySummary;
    private List<RecentActivity> recentActivities;
    private List<SubjectStats> subjectStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectStats {
        private Long subjectId;
        private String subjectName;
        private String subjectCode;
        private int totalStudents;
        private int completedSessions;
        private int pendingSessions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodInfo {
        private Long periodId;
        private Integer periodNumber;
        private String startTime;
        private String endTime;
        private String subjectName;
        private String subjectCode;
        private String classSection;
        private String status; // PENDING, OPEN, SUBMITTED, FREE
        private boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodaySummary {
        private int presentCount;
        private int absentCount;
        private int lateCount;
        private int onDutyCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private String timeAgo;
        private String description;
        private String type; // SUCCESS, WARNING, INFO
    }
}
