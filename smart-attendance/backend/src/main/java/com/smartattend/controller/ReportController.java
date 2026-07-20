package com.smartattend.controller;

import com.smartattend.dto.report.DateWiseReportResponse;
import com.smartattend.dto.report.SubjectWiseReportResponse;
import com.smartattend.dto.report.SummaryReportResponse;
import com.smartattend.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/attendance")
    public ResponseEntity<SummaryReportResponse> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long subjectId) {
        return ResponseEntity.ok(reportService.getSummaryReport(startDate, endDate, subjectId));
    }

    @GetMapping("/attendance/date-wise")
    public ResponseEntity<DateWiseReportResponse> getDateWise(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long subjectId) {
        return ResponseEntity.ok(reportService.getDateWiseReport(startDate, endDate, subjectId));
    }

    @GetMapping("/attendance/subject-wise/{studentId}")
    public ResponseEntity<SubjectWiseReportResponse> getSubjectWise(@PathVariable Long studentId) {
        return ResponseEntity.ok(reportService.getSubjectWiseReport(studentId));
    }

    @GetMapping("/attendance/export/excel")
    public ResponseEntity<byte[]> downloadExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long subjectId) throws Exception {
        
        String teacherName = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportService.exportExcel(startDate, endDate, subjectId, teacherName);

        String filename = String.format("Attendance_%s_to_%s.xlsx", startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @GetMapping("/attendance/export/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long subjectId) throws Exception {

        String teacherName = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportService.exportPdf(startDate, endDate, subjectId, teacherName);

        String filename = String.format("Attendance_%s_to_%s.pdf", startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/attendance/export/csv")
    public ResponseEntity<byte[]> downloadCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long subjectId) throws Exception {

        byte[] bytes = reportService.exportCsv(startDate, endDate, subjectId);

        String filename = String.format("Attendance_%s_to_%s.csv", startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }
}
