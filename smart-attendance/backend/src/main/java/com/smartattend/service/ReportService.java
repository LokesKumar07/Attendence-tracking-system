package com.smartattend.service;

import com.smartattend.dto.report.DateWiseReportResponse;
import com.smartattend.dto.report.StudentReportRow;
import com.smartattend.dto.report.SubjectWiseReportResponse;
import com.smartattend.dto.report.SummaryReportResponse;
import com.smartattend.entity.*;
import com.smartattend.exception.InvalidRequestException;
import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.repository.*;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;

    public ReportService(StudentRepository studentRepository,
                         SubjectRepository subjectRepository,
                         AttendanceSessionRepository sessionRepository,
                         AttendanceRecordRepository recordRepository) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
    }

    public SummaryReportResponse getSummaryReport(LocalDate start, LocalDate end, Long subjectId) {
        validateDates(start, end);
        
        List<Student> students = studentRepository.searchActiveStudents(null);
        students.sort(Comparator.comparing(Student::getRegisterNumber));

        // Get matching sessions
        List<AttendanceSession> sessions;
        if (subjectId != null) {
            sessions = sessionRepository.findByAttendanceDateBetweenAndSubjectId(start, end, subjectId);
        } else {
            sessions = sessionRepository.findByAttendanceDateBetween(start, end);
        }
        
        // Only count SUBMITTED or LOCKED sessions as official conducted classes
        List<AttendanceSession> conducted = sessions.stream()
                .filter(s -> s.getStatus() == AttendanceSessionStatus.SUBMITTED || s.getStatus() == AttendanceSessionStatus.LOCKED)
                .collect(Collectors.toList());

        int totalConducted = conducted.size();
        String subName = (subjectId != null) ? subjectRepository.findById(subjectId).map(Subject::getName).orElse("All Subjects") : "All Subjects";

        List<StudentReportRow> rows = new ArrayList<>();
        for (Student s : students) {
            int present = 0, absent = 0, late = 0, od = 0;
            
            for (AttendanceSession session : conducted) {
                Optional<AttendanceRecord> rec = recordRepository.findByAttendanceSessionIdAndStudentId(session.getId(), s.getId());
                if (rec.isPresent()) {
                    AttendanceRecordStatus st = rec.get().getStatus();
                    if (st == AttendanceRecordStatus.PRESENT) present++;
                    else if (st == AttendanceRecordStatus.ABSENT) absent++;
                    else if (st == AttendanceRecordStatus.LATE) late++;
                    else if (st == AttendanceRecordStatus.ON_DUTY) od++;
                }
            }

            int eligible = present + late + od;
            double percentage = totalConducted > 0 ? ((double) eligible / totalConducted) * 100.0 : 100.0;

            rows.add(StudentReportRow.builder()
                    .registerNumber(s.getRegisterNumber())
                    .rollNumber(s.getRollNumber())
                    .studentName(s.getName())
                    .totalConductedClasses(totalConducted)
                    .presentCount(present)
                    .absentCount(absent)
                    .lateCount(late)
                    .onDutyCount(od)
                    .eligibleAttendanceCount(eligible)
                    .attendancePercentage(Math.round(percentage * 100.0) / 100.0)
                    .build());
        }

        return SummaryReportResponse.builder()
                .startDate(start)
                .endDate(end)
                .subjectName(subName)
                .rows(rows)
                .build();
    }

    public DateWiseReportResponse getDateWiseReport(LocalDate start, LocalDate end, Long subjectId) {
        validateDates(start, end);

        List<Student> students = studentRepository.searchActiveStudents(null);
        students.sort(Comparator.comparing(Student::getRegisterNumber));

        List<AttendanceSession> sessions;
        if (subjectId != null) {
            sessions = sessionRepository.findByAttendanceDateBetweenAndSubjectId(start, end, subjectId);
        } else {
            sessions = sessionRepository.findByAttendanceDateBetween(start, end);
        }

        List<AttendanceSession> conducted = sessions.stream()
                .filter(s -> s.getStatus() == AttendanceSessionStatus.SUBMITTED || s.getStatus() == AttendanceSessionStatus.LOCKED)
                .sorted(Comparator.comparing(AttendanceSession::getAttendanceDate).thenComparing(s -> s.getPeriod().getPeriodNumber()))
                .collect(Collectors.toList());

        List<LocalDate> dates = conducted.stream()
                .map(AttendanceSession::getAttendanceDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<DateWiseReportResponse.StudentDateRow> rows = new ArrayList<>();
        for (Student s : students) {
            List<String> statuses = new ArrayList<>();
            for (LocalDate date : dates) {
                // Find all sessions on this date
                List<AttendanceSession> daySessions = conducted.stream()
                        .filter(cs -> cs.getAttendanceDate().equals(date))
                        .collect(Collectors.toList());

                if (daySessions.isEmpty()) {
                    statuses.add("-");
                } else {
                    // Combine or take first for simple visual (P if all P, else look at first)
                    // If multiple periods in a day, report the primary status or average
                    AttendanceRecordStatus primary = null;
                    for (AttendanceSession ds : daySessions) {
                        Optional<AttendanceRecord> rec = recordRepository.findByAttendanceSessionIdAndStudentId(ds.getId(), s.getId());
                        if (rec.isPresent()) {
                            primary = rec.get().getStatus();
                            break;
                        }
                    }
                    if (primary == null) {
                        statuses.add("-");
                    } else {
                        switch (primary) {
                            case PRESENT: statuses.add("P"); break;
                            case ABSENT: statuses.add("A"); break;
                            case LATE: statuses.add("L"); break;
                            case ON_DUTY: statuses.add("OD"); break;
                            default: statuses.add("-");
                        }
                    }
                }
            }

            rows.add(DateWiseReportResponse.StudentDateRow.builder()
                    .registerNumber(s.getRegisterNumber())
                    .rollNumber(s.getRollNumber())
                    .studentName(s.getName())
                    .statuses(statuses)
                    .build());
        }

        return DateWiseReportResponse.builder()
                .startDate(start)
                .endDate(end)
                .dates(dates)
                .rows(rows)
                .build();
    }

    public SubjectWiseReportResponse getSubjectWiseReport(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Subject> subjects = subjectRepository.findByIsActive(true);
        List<SubjectWiseReportResponse.SubjectSummaryRow> rows = new ArrayList<>();

        for (Subject sub : subjects) {
            List<AttendanceSession> sessions = sessionRepository.findByAttendanceDateBetweenAndSubjectId(
                    LocalDate.now().minusYears(1), LocalDate.now().plusDays(1), sub.getId())
                    .stream()
                    .filter(s -> s.getStatus() == AttendanceSessionStatus.SUBMITTED || s.getStatus() == AttendanceSessionStatus.LOCKED)
                    .collect(Collectors.toList());

            int conducted = sessions.size();
            int present = 0, absent = 0, late = 0, od = 0;

            for (AttendanceSession s : sessions) {
                Optional<AttendanceRecord> rec = recordRepository.findByAttendanceSessionIdAndStudentId(s.getId(), student.getId());
                if (rec.isPresent()) {
                    switch (rec.get().getStatus()) {
                        case PRESENT: present++; break;
                        case ABSENT: absent++; break;
                        case LATE: late++; break;
                        case ON_DUTY: od++; break;
                    }
                }
            }

            int eligible = present + late + od;
            double percentage = conducted > 0 ? ((double) eligible / conducted) * 100.0 : 100.0;

            rows.add(SubjectWiseReportResponse.SubjectSummaryRow.builder()
                    .subjectCode(sub.getCode())
                    .subjectName(sub.getName())
                    .conducted(conducted)
                    .present(present)
                    .absent(absent)
                    .late(late)
                    .onDuty(od)
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build());
        }

        return SubjectWiseReportResponse.builder()
                .registerNumber(student.getRegisterNumber())
                .studentName(student.getName())
                .rows(rows)
                .build();
    }

    public byte[] exportExcel(LocalDate start, LocalDate end, Long subjectId, String teacherName) throws Exception {
        SummaryReportResponse rep = getSummaryReport(start, end, subjectId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Attendance Report");
            sheet.setDisplayGridlines(true);

            // Title Style
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.VIOLET.getIndex());
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Meta Style
            CellStyle metaStyle = workbook.createCellStyle();
            Font metaFont = workbook.createFont();
            metaFont.setItalic(true);
            metaStyle.setFont(metaFont);

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Title Row
            Row r0 = sheet.createRow(0);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("PPG COLLEGE OF ARTS AND SCIENCE — SMARTATTEND REPORT");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            // Metadata rows
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Teacher: " + teacherName);
            r2.getCell(0).setCellStyle(metaStyle);
            
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue("Subject: " + rep.getSubjectName());
            r3.getCell(0).setCellStyle(metaStyle);

            Row r4 = sheet.createRow(4);
            r4.createCell(0).setCellValue("Duration: " + start + " to " + end);
            r4.getCell(0).setCellStyle(metaStyle);

            // Headers
            String[] headers = {"Register Number", "Roll Number", "Student Name", "Conducted", "Present", "Absent", "Late", "On-Duty", "Percentage"};
            Row r6 = sheet.createRow(6);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = r6.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 7;
            for (StudentReportRow rowData : rep.getRows()) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(rowData.getRegisterNumber());
                r.createCell(1).setCellValue(rowData.getRollNumber());
                r.createCell(2).setCellValue(rowData.getStudentName());
                r.createCell(3).setCellValue(rowData.getTotalConductedClasses());
                r.createCell(4).setCellValue(rowData.getPresentCount());
                r.createCell(5).setCellValue(rowData.getAbsentCount());
                r.createCell(6).setCellValue(rowData.getLateCount());
                r.createCell(7).setCellValue(rowData.getOnDutyCount());
                
                Cell cPct = r.createCell(8);
                cPct.setCellValue(rowData.getAttendancePercentage() / 100.0);
                
                // Percent formatting
                CellStyle pctStyle = workbook.createCellStyle();
                pctStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
                cPct.setCellStyle(pctStyle);
            }

            // Auto size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportPdf(LocalDate start, LocalDate end, Long subjectId, String teacherName) throws Exception {
        SummaryReportResponse rep = getSummaryReport(start, end, subjectId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD, java.awt.Color.decode("#6D28D9"));
            com.lowagie.text.Font metaFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.ITALIC);
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD, java.awt.Color.WHITE);

            // Title
            Paragraph title = new Paragraph("PPG College of Arts & Science - SmartAttend", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Meta Info
            document.add(new Paragraph("Teacher: " + teacherName, metaFont));
            document.add(new Paragraph("Subject: " + rep.getSubjectName(), metaFont));
            document.add(new Paragraph("Period: " + start + " to " + end, metaFont));
            document.add(new Paragraph("Generated At: " + LocalDate.now(), metaFont));
            document.add(new Paragraph("\n"));

            // Table
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            String[] headers = {"Reg No", "Roll No", "Student Name", "Cond", "Pres", "Abs", "Late", "OD", "%"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(java.awt.Color.decode("#6D28D9"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (StudentReportRow rowData : rep.getRows()) {
                table.addCell(new Phrase(rowData.getRegisterNumber()));
                table.addCell(new Phrase(rowData.getRollNumber()));
                table.addCell(new Phrase(rowData.getStudentName()));
                table.addCell(new Phrase(String.valueOf(rowData.getTotalConductedClasses())));
                table.addCell(new Phrase(String.valueOf(rowData.getPresentCount())));
                table.addCell(new Phrase(String.valueOf(rowData.getAbsentCount())));
                table.addCell(new Phrase(String.valueOf(rowData.getLateCount())));
                table.addCell(new Phrase(String.valueOf(rowData.getOnDutyCount())));
                table.addCell(new Phrase(rowData.getAttendancePercentage() + "%"));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    public byte[] exportCsv(LocalDate start, LocalDate end, Long subjectId) throws Exception {
        SummaryReportResponse rep = getSummaryReport(start, end, subjectId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                     "Register Number", "Roll Number", "Student Name", "Conducted", "Present", "Absent", "Late", "On-Duty", "Percentage"))) {

            for (StudentReportRow row : rep.getRows()) {
                // Prevent CSV formula injection by prepending single quote to formulas
                String reg = sanitizeCsv(row.getRegisterNumber());
                String roll = sanitizeCsv(row.getRollNumber());
                String name = sanitizeCsv(row.getStudentName());

                printer.printRecord(
                        reg,
                        roll,
                        name,
                        row.getTotalConductedClasses(),
                        row.getPresentCount(),
                        row.getAbsentCount(),
                        row.getLateCount(),
                        row.getOnDutyCount(),
                        row.getAttendancePercentage() + "%"
                );
            }
            printer.flush();
            writer.flush();
            return out.toByteArray();
        }
    }

    private String sanitizeCsv(String val) {
        if (val == null) return "";
        if (val.startsWith("=") || val.startsWith("+") || val.startsWith("-") || val.startsWith("@")) {
            return "'" + val;
        }
        return val;
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new InvalidRequestException("Start Date and End Date are mandatory");
        }
        if (end.isBefore(start)) {
            throw new InvalidRequestException("End Date cannot be earlier than Start Date");
        }
    }
}
