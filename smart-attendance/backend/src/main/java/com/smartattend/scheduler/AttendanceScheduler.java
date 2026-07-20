package com.smartattend.scheduler;

import com.smartattend.entity.*;
import com.smartattend.repository.*;
import com.smartattend.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class AttendanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(AttendanceScheduler.class);

    private final TimetableEntryRepository entryRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final HolidayRepository holidayRepository;
    private final PeriodRepository periodRepository;
    private final TeacherRepository teacherRepository;

    public AttendanceScheduler(TimetableEntryRepository entryRepository,
                               AttendanceSessionRepository sessionRepository,
                               HolidayRepository holidayRepository,
                               PeriodRepository periodRepository,
                               TeacherRepository teacherRepository) {
        this.entryRepository = entryRepository;
        this.sessionRepository = sessionRepository;
        this.holidayRepository = holidayRepository;
        this.periodRepository = periodRepository;
        this.teacherRepository = teacherRepository;
    }

    // Cron runs at 00:00 every day to pre-schedule sessions based on timetable
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleDailySessions() {
        LocalDate today = LocalDate.now();
        
        // Skip holidays
        if (holidayRepository.existsByHolidayDate(today)) {
            log.info("Today ({}) is a holiday. Skipping session pre-scheduling.", today);
            return;
        }

        java.time.DayOfWeek dayOfWeek = today.getDayOfWeek();
        if (dayOfWeek == java.time.DayOfWeek.SUNDAY) {
            log.info("Today ({}) is Sunday. Skipping session pre-scheduling.", today);
            return;
        }
        int dayOrder = dayOfWeek.getValue(); // Monday is 1, Saturday is 6
        List<TimetableEntry> entries = entryRepository.findByTimetableIsActiveTrueAndDayOrder(dayOrder);

        if (entries.isEmpty()) {
            log.info("No active classes scheduled in timetable for {}", today);
            return;
        }

        Teacher defaultTeacher = teacherRepository.findAll().stream().findFirst().orElse(null);
        if (defaultTeacher == null) {
            log.warn("No active teachers in system database. Skipping pre-scheduling.");
            return;
        }

        for (TimetableEntry entry : entries) {
            if (entry.getEntryType() == TimetableEntryType.CLASS || entry.getEntryType() == TimetableEntryType.LAB) {
                
                // Get corresponding Period Config
                Period period = periodRepository.findByPeriodNumber(entry.getPeriodNumber())
                        .orElse(null);

                if (period == null || entry.getSubject() == null) continue;

                boolean exists = sessionRepository.findByAttendanceDateAndSubjectIdAndPeriodId(
                        today, entry.getSubject().getId(), period.getId()).isPresent();

                if (!exists) {
                    AttendanceSession session = AttendanceSession.builder()
                            .attendanceDate(today)
                            .subject(entry.getSubject())
                            .period(period)
                            .status(AttendanceSessionStatus.SCHEDULED)
                            .createdBy(defaultTeacher)
                            .build();
                    sessionRepository.save(session);
                    log.info("Scheduled attendance session for date: {}, subject: {}, period: {}", 
                            today, entry.getSubject().getName(), entry.getPeriodNumber());
                }
            }
        }
    }
}
