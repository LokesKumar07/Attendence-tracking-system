package com.smartattend.controller;

import com.smartattend.dto.attendance.*;
import com.smartattend.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<AttendanceSessionResponse>> getSessionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getSessionsByDate(date));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<AttendanceSessionResponse> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getSessionById(id));
    }

    @PostMapping("/sessions/open")
    public ResponseEntity<AttendanceSessionResponse> openSession(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long subjectId,
            @RequestParam Long periodId,
            HttpServletRequest servletRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(attendanceService.openSession(date, subjectId, periodId, username, servletRequest.getRemoteAddr()));
    }

    @PostMapping("/sessions/{id}/submit")
    public ResponseEntity<AttendanceSessionResponse> submit(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceSubmitRequest req,
            HttpServletRequest servletRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(attendanceService.submitAttendance(id, req, username, servletRequest.getRemoteAddr()));
    }

    @PostMapping("/corrections")
    public ResponseEntity<AttendanceCorrectionResponse> correct(
            @Valid @RequestBody AttendanceCorrectionRequest req,
            HttpServletRequest servletRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(attendanceService.applyCorrection(req, username, servletRequest.getRemoteAddr()));
    }

    @GetMapping("/corrections/history")
    public ResponseEntity<List<AttendanceCorrectionResponse>> getHistory() {
        return ResponseEntity.ok(attendanceService.getCorrectionHistory());
    }

    @GetMapping("/dashboard-status")
    public ResponseEntity<DashboardStatusResponse> getDashboardStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer dayOrder,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        
        LocalDate d = (date != null) ? date : LocalDate.now();
        LocalTime t = (time != null) ? time : LocalTime.now();
        Integer dayOrd = (dayOrder != null) ? dayOrder : 1;
        
        return ResponseEntity.ok(attendanceService.getDashboardStatus(d, dayOrd, t));
    }
}
