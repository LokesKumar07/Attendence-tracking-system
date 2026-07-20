package com.smartattend.service;

import com.smartattend.dto.attendance.*;
import com.smartattend.entity.*;
import com.smartattend.exception.*;
import com.smartattend.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceCorrectionRepository correctionRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final PeriodRepository periodRepository;
    private final TeacherRepository teacherRepository;
    private final HolidayRepository holidayRepository;
    private final TimetableEntryRepository entryRepository;
    private final SettingService settingService;
    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;

    public AttendanceService(AttendanceSessionRepository sessionRepository,
                             AttendanceRecordRepository recordRepository,
                             AttendanceCorrectionRepository correctionRepository,
                             StudentRepository studentRepository,
                             SubjectRepository subjectRepository,
                             PeriodRepository periodRepository,
                             TeacherRepository teacherRepository,
                             HolidayRepository holidayRepository,
                             TimetableEntryRepository entryRepository,
                             SettingService settingService,
                             AuditService auditService,
                             SimpMessagingTemplate messagingTemplate) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.correctionRepository = correctionRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.periodRepository = periodRepository;
        this.teacherRepository = teacherRepository;
        this.holidayRepository = holidayRepository;
        this.entryRepository = entryRepository;
        this.settingService = settingService;
        this.auditService = auditService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<AttendanceSessionResponse> getSessionsByDate(LocalDate date) {
        return sessionRepository.findByAttendanceDate(date)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AttendanceSessionResponse getSessionById(Long id) {
        AttendanceSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));
        return mapToResponse(session);
    }

    @Transactional
    public AttendanceSessionResponse openSession(LocalDate date, Long subjectId, Long periodId, String username, String ipAddress) {
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher context not found"));

        if (holidayRepository.existsByHolidayDate(date)) {
            throw new InvalidRequestException("Cannot open attendance session on a configured holiday: " + date);
        }

        // Prevent duplicates using unique constraints logic
        Optional<AttendanceSession> existing = sessionRepository.findByAttendanceDateAndSubjectIdAndPeriodId(date, subjectId, periodId);
        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        Period period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException("Period not found"));

        AttendanceSession session = AttendanceSession.builder()
                .attendanceDate(date)
                .subject(subject)
                .period(period)
                .status(AttendanceSessionStatus.OPEN)
                .createdBy(teacher)
                .build();

        AttendanceSession saved = sessionRepository.save(savedSessionWithRollback(session));
        
        auditService.log(teacher, "OPEN_SESSION", "Opened session ID: " + saved.getId() + " for subject: " + subject.getName(), ipAddress);
        notifyWebSocketUpdate("SESSION_OPENED");

        return mapToResponse(saved);
    }

    private AttendanceSession savedSessionWithRollback(AttendanceSession session) {
        try {
            return sessionRepository.saveAndFlush(session);
        } catch (Exception ex) {
            throw new DuplicateResourceException("An attendance session for this date, subject, and period already exists.");
        }
    }

    @Transactional
    public AttendanceSessionResponse submitAttendance(Long sessionId, AttendanceSubmitRequest req, String username, String ipAddress) {
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher context not found"));

        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));

        // If the session is LOCKED or CANCELLED, editing is blocked
        if (session.getStatus() == AttendanceSessionStatus.LOCKED) {
            throw new InvalidRequestException("This attendance session is locked and cannot be modified.");
        }
        if (session.getStatus() == AttendanceSessionStatus.CANCELLED) {
            throw new InvalidRequestException("This attendance session is cancelled.");
        }

        // Process records in single transaction. Rollback on failure.
        try {
            List<AttendanceRecord> newRecords = new ArrayList<>();
            for (AttendanceRecordRequest recReq : req.getRecords()) {
                Student student = studentRepository.findById(recReq.getStudentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student ID " + recReq.getStudentId() + " not found"));

                Optional<AttendanceRecord> existingRec = recordRepository.findByAttendanceSessionIdAndStudentId(sessionId, student.getId());
                
                AttendanceRecord record;
                if (existingRec.isPresent()) {
                    record = existingRec.get();
                    // Overwrite is allowed during original Draft/Submission but tracked if status flips after final Submission.
                    // If session is already submitted and teacher is editing, it should go through correction request.
                    record.setStatus(recReq.getStatus());
                    record.setRemarks(recReq.getRemarks());
                } else {
                    record = AttendanceRecord.builder()
                            .attendanceSession(session)
                            .student(student)
                            .status(recReq.getStatus())
                            .remarks(recReq.getRemarks())
                            .build();
                }
                newRecords.add(record);
            }
            recordRepository.saveAll(newRecords);
            
            session.setStatus(req.isDraft() ? AttendanceSessionStatus.DRAFT : AttendanceSessionStatus.SUBMITTED);
            sessionRepository.save(session);
            
            auditService.log(teacher, req.isDraft() ? "SAVE_DRAFT" : "SUBMIT_ATTENDANCE", 
                    "Submitted attendance for session ID: " + sessionId, ipAddress);

            notifyWebSocketUpdate("ATTENDANCE_SUBMITTED");
            return mapToResponse(session);

        } catch (Exception ex) {
            throw new InvalidRequestException("Failed to save attendance: " + ex.getMessage());
        }
    }

    @Transactional
    public AttendanceCorrectionResponse applyCorrection(AttendanceCorrectionRequest req, String username, String ipAddress) {
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher context not found"));

        AttendanceRecord record = recordRepository.findById(req.getRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        AttendanceRecordStatus previousStatus = record.getStatus();
        
        // Correct status
        record.setStatus(req.getNewStatus());
        recordRepository.save(record);

        // Append to correction log
        AttendanceCorrection correction = AttendanceCorrection.builder()
                .attendanceRecord(record)
                .previousStatus(previousStatus)
                .newStatus(req.getNewStatus())
                .correctionReason(req.getReason())
                .changedBy(teacher)
                .build();
        AttendanceCorrection saved = correctionRepository.save(correction);

        auditService.log(teacher, "CORRECT_ATTENDANCE", 
                "Corrected Student " + record.getStudent().getName() + " status from " + previousStatus + " to " + req.getNewStatus(), ipAddress);

        notifyWebSocketUpdate("ATTENDANCE_CORRECTED");

        return AttendanceCorrectionResponse.builder()
                .id(saved.getId())
                .recordId(record.getId())
                .studentName(record.getStudent().getName())
                .registerNumber(record.getStudent().getRegisterNumber())
                .subjectName(record.getAttendanceSession().getSubject().getName())
                .previousStatus(previousStatus.name())
                .newStatus(req.getNewStatus().name())
                .correctionReason(req.getReason())
                .changedByTeacher(teacher.getFullName())
                .changedAt(saved.getChangedAt())
                .build();
    }

    public List<AttendanceCorrectionResponse> getCorrectionHistory() {
        return correctionRepository.findAllByOrderByChangedAtDesc()
                .stream()
                .map(c -> AttendanceCorrectionResponse.builder()
                        .id(c.getId())
                        .recordId(c.getAttendanceRecord().getId())
                        .studentName(c.getAttendanceRecord().getStudent().getName())
                        .registerNumber(c.getAttendanceRecord().getStudent().getRegisterNumber())
                        .subjectName(c.getAttendanceRecord().getAttendanceSession().getSubject().getName())
                        .previousStatus(c.getPreviousStatus().name())
                        .newStatus(c.getNewStatus().name())
                        .correctionReason(c.getCorrectionReason())
                        .changedByTeacher(c.getChangedBy().getFullName())
                        .changedAt(c.getChangedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public DashboardStatusResponse getDashboardStatus(LocalDate date, Integer dayOrder, LocalTime time) {
        String dayStr = "Day Order " + getRomanDayOrder(dayOrder);
        List<TimetableEntry> todayEntries = entryRepository.findByTimetableIsActiveTrueAndDayOrder(dayOrder);

        // Sort by start time
        todayEntries.sort(Comparator.comparing(TimetableEntry::getStartTime));

        TimetableEntry current = null;
        TimetableEntry previous = null;
        TimetableEntry next = null;

        for (int i = 0; i < todayEntries.size(); i++) {
            TimetableEntry e = todayEntries.get(i);
            if ((time.isAfter(e.getStartTime()) || time.equals(e.getStartTime())) && time.isBefore(e.getEndTime())) {
                current = e;
                if (i > 0) previous = todayEntries.get(i - 1);
                if (i < todayEntries.size() - 1) next = todayEntries.get(i + 1);
                break;
            }
        }

        // If no match for current active, locate next/previous based on time
        if (current == null) {
            for (int i = 0; i < todayEntries.size(); i++) {
                TimetableEntry e = todayEntries.get(i);
                if (time.isBefore(e.getStartTime())) {
                    next = e;
                    if (i > 0) previous = todayEntries.get(i - 1);
                    break;
                }
            }
            // If still no next, it means we are past all classes today
            if (next == null && !todayEntries.isEmpty()) {
                previous = todayEntries.get(todayEntries.size() - 1);
            }
        }

        int totalStuds = (int) studentRepository.count();
        List<AttendanceSession> todaySessions = sessionRepository.findByAttendanceDate(date);
        int completed = (int) todaySessions.stream().filter(s -> s.getStatus() == AttendanceSessionStatus.SUBMITTED).count();
        int pending = (int) todaySessions.stream().filter(s -> s.getStatus() == AttendanceSessionStatus.OPEN || s.getStatus() == AttendanceSessionStatus.DRAFT).count();

        // Calculate live marking sums for today
        int present = 0, absent = 0, late = 0, od = 0;
        for (AttendanceSession s : todaySessions) {
            List<AttendanceRecord> recs = recordRepository.findByAttendanceSessionId(s.getId());
            for (AttendanceRecord r : recs) {
                if (r.getStatus() == AttendanceRecordStatus.PRESENT) present++;
                else if (r.getStatus() == AttendanceRecordStatus.ABSENT) absent++;
                else if (r.getStatus() == AttendanceRecordStatus.LATE) late++;
                else if (r.getStatus() == AttendanceRecordStatus.ON_DUTY) od++;
            }
        }

        // Calculate subject-wise statistics
        List<Subject> activeSubjects = subjectRepository.findByIsActive(true);
        List<DashboardStatusResponse.SubjectStats> subjectStatsList = activeSubjects.stream().map(sub -> {
            int studsCount = studentRepository.findByEnrolledSubjectsIdAndIsActiveTrue(sub.getId()).size();
            int subCompleted = (int) todaySessions.stream()
                    .filter(s -> s.getSubject().getId().equals(sub.getId()) && s.getStatus() == AttendanceSessionStatus.SUBMITTED)
                    .count();
            int subPending = (int) todaySessions.stream()
                    .filter(s -> s.getSubject().getId().equals(sub.getId()) && (s.getStatus() == AttendanceSessionStatus.OPEN || s.getStatus() == AttendanceSessionStatus.DRAFT))
                    .count();
            return DashboardStatusResponse.SubjectStats.builder()
                    .subjectId(sub.getId())
                    .subjectName(sub.getName())
                    .subjectCode(sub.getCode())
                    .totalStudents(studsCount)
                    .completedSessions(subCompleted)
                    .pendingSessions(subPending)
                    .build();
        }).collect(Collectors.toList());

        // Recent Audit activities
        List<DashboardStatusResponse.RecentActivity> recent = auditService.getAllLogs().stream()
                .limit(5)
                .map(log -> DashboardStatusResponse.RecentActivity.builder()
                        .timeAgo(log.getCreatedAt().toLocalTime().toString())
                        .description(log.getAction() + ": " + log.getDetails())
                        .type(log.getAction().contains("ERROR") ? "WARNING" : "INFO")
                        .build())
                .collect(Collectors.toList());

        return DashboardStatusResponse.builder()
                .currentDate(date)
                .currentDay(dayStr)
                .currentTime(time)
                .currentPeriod(mapPeriodInfo(current, date, time))
                .previousPeriod(mapPeriodInfo(previous, date, time))
                .nextPeriod(mapPeriodInfo(next, date, time))
                .totalStudents(totalStuds)
                .completedSessions(completed)
                .pendingSessions(pending)
                .subjectStats(subjectStatsList)
                .todaySummary(DashboardStatusResponse.TodaySummary.builder()
                        .presentCount(present)
                        .absentCount(absent)
                        .lateCount(late)
                        .onDutyCount(od)
                        .build())
                .recentActivities(recent)
                .build();
    }

    private DashboardStatusResponse.PeriodInfo mapPeriodInfo(TimetableEntry e, LocalDate date, LocalTime time) {
        if (e == null) return null;
        
        String status = "FREE";
        Long actualPeriodId = (long) e.getPeriodNumber();
        
        if (e.getEntryType() == TimetableEntryType.CLASS || e.getEntryType() == TimetableEntryType.LAB) {
            status = "PENDING";
            // Check database to see if session is already created
            if (e.getSubject() != null) {
                Optional<AttendanceSession> s = sessionRepository.findByAttendanceDateAndSubjectIdAndPeriodId(date, e.getSubject().getId(), actualPeriodId);
                if (s.isPresent()) {
                    status = s.get().getStatus().name();
                }
            }
        } else {
            status = e.getEntryType().name(); // BREAK, LUNCH
        }

        boolean active = (time.isAfter(e.getStartTime()) || time.equals(e.getStartTime())) && time.isBefore(e.getEndTime());

        return DashboardStatusResponse.PeriodInfo.builder()
                .periodId(e.getId())
                .periodNumber(e.getPeriodNumber())
                .startTime(e.getStartTime().toString())
                .endTime(e.getEndTime().toString())
                .subjectName(e.getSubject() != null ? e.getSubject().getName() : "-")
                .subjectCode(e.getSubject() != null ? e.getSubject().getCode() : "-")
                .classSection(e.getSubject() != null ? e.getSubject().getSemester().getName() : "-")
                .status(status)
                .isActive(active)
                .build();
    }

    private AttendanceSessionResponse mapToResponse(AttendanceSession session) {
        List<AttendanceSessionResponse.RecordResponse> records = recordRepository.findByAttendanceSessionId(session.getId())
                .stream()
                .map(r -> AttendanceSessionResponse.RecordResponse.builder()
                        .recordId(r.getId())
                        .studentId(r.getStudent().getId())
                        .registerNumber(r.getStudent().getRegisterNumber())
                        .rollNumber(r.getStudent().getRollNumber())
                        .name(r.getStudent().getName())
                        .status(r.getStatus().name())
                        .remarks(r.getRemarks())
                        .build())
                .collect(Collectors.toList());

        // If no records in database yet, map all active students as UNMARKED so UI has the full list of students
        if (records.isEmpty()) {
            records = studentRepository.findByEnrolledSubjectsIdAndIsActiveTrue(session.getSubject().getId()).stream()
                    .map(s -> AttendanceSessionResponse.RecordResponse.builder()
                            .recordId(null)
                            .studentId(s.getId())
                            .registerNumber(s.getRegisterNumber())
                            .rollNumber(s.getRollNumber())
                            .name(s.getName())
                            .status("UNMARKED")
                            .remarks("")
                            .build())
                    .collect(Collectors.toList());
        }

        return AttendanceSessionResponse.builder()
                .id(session.getId())
                .attendanceDate(session.getAttendanceDate())
                .subjectId(session.getSubject().getId())
                .subjectCode(session.getSubject().getCode())
                .subjectName(session.getSubject().getName())
                .periodId(session.getPeriod().getId())
                .periodNumber(session.getPeriod().getPeriodNumber())
                .periodTimeRange(session.getPeriod().getStartTime() + " - " + session.getPeriod().getEndTime())
                .status(session.getStatus())
                .createdByTeacher(session.getCreatedBy().getFullName())
                .records(records)
                .build();
    }

    private void notifyWebSocketUpdate(String event) {
        try {
            messagingTemplate.convertAndSend("/topic/attendance-updates", Map.of("event", event, "timestamp", LocalDateTime.now().toString()));
        } catch (Exception ex) {
            // Suppress fallback if WS is not active/available
        }
    }

    private String getRomanDayOrder(Integer dayOrder) {
        if (dayOrder == null) return "I";
        switch (dayOrder) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            default: return String.valueOf(dayOrder);
        }
    }
}
