package com.smartattend.service;

import com.smartattend.dto.student.StudentRequest;
import com.smartattend.dto.student.StudentResponse;
import com.smartattend.entity.*;
import com.smartattend.exception.DuplicateResourceException;
import com.smartattend.exception.InvalidRequestException;
import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.mapper.StudentMapper;
import com.smartattend.repository.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final ClassSectionRepository classSectionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final SubjectRepository subjectRepository;

    public StudentService(StudentRepository studentRepository,
                          DepartmentRepository departmentRepository,
                          AcademicYearRepository academicYearRepository,
                          SemesterRepository semesterRepository,
                          ClassSectionRepository classSectionRepository,
                          AttendanceRecordRepository attendanceRecordRepository,
                          SubjectRepository subjectRepository) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.classSectionRepository = classSectionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.subjectRepository = subjectRepository;
    }

    public List<StudentResponse> getAllStudents(Boolean isActive, Long subjectId, Long classSectionId, String search) {
        return studentRepository.filterStudents(isActive, subjectId, classSectionId, search)
                .stream()
                .map(StudentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public StudentResponse getStudentById(Long id) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return StudentMapper.toResponse(s);
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest req) {
        if (studentRepository.existsByRegisterNumber(req.getRegisterNumber())) {
            throw new DuplicateResourceException("Student with Register Number already exists");
        }
        if (studentRepository.existsByRollNumber(req.getRollNumber())) {
            throw new DuplicateResourceException("Student with Roll Number already exists");
        }

        Student student = buildStudentFromRequest(req, new Student());
        Student saved = studentRepository.save(student);
        return StudentMapper.toResponse(saved);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest req) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!existing.getRegisterNumber().equals(req.getRegisterNumber()) &&
                studentRepository.existsByRegisterNumber(req.getRegisterNumber())) {
            throw new DuplicateResourceException("Student with Register Number already exists");
        }
        if (!existing.getRollNumber().equals(req.getRollNumber()) &&
                studentRepository.existsByRollNumber(req.getRollNumber())) {
            throw new DuplicateResourceException("Student with Roll Number already exists");
        }

        buildStudentFromRequest(req, existing);
        Student saved = studentRepository.save(existing);
        return StudentMapper.toResponse(saved);
    }

    @Transactional
    public void deleteOrDeactivateStudent(Long id) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<AttendanceRecord> records = attendanceRecordRepository.findByStudentId(id);
        if (!records.isEmpty()) {
            // Cannot delete physically, soft deactivate
            existing.setIsActive(false);
            studentRepository.save(existing);
        } else {
            studentRepository.delete(existing);
        }
    }

    @Transactional
    public int importStudents(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new InvalidRequestException("Missing filename");
        }

        try {
            if (filename.endsWith(".csv")) {
                return importCsv(file);
            } else if (filename.endsWith(".xlsx")) {
                return importExcel(file);
            } else {
                throw new InvalidRequestException("Unsupported file format. Please upload .csv or .xlsx file");
            }
        } catch (Exception e) {
            throw new InvalidRequestException("Failed to parse file: " + e.getMessage());
        }
    }

    private int importCsv(MultipartFile file) throws Exception {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : parser) {
                String registerNum = record.get("Register Number");
                String rollNum = record.isMapped("Roll Number") ? record.get("Roll Number") : "";
                String name = record.get("Name");
                String deptCode = record.get("Department Code");
                String academicYearName = record.get("Academic Year");
                String semName = record.get("Semester");
                String classSecName = record.get("Class Section");
                String yearStr = record.get("Year of Study");
                String email = record.isMapped("Email") ? record.get("Email") : "";
                String phone = record.isMapped("Phone Number") ? record.get("Phone Number") : "";
                String subjectsStr = record.isMapped("Subjects") ? record.get("Subjects") : "";

                saveImportedRecord(registerNum, rollNum, name, deptCode, academicYearName, semName, classSecName, yearStr, email, phone, subjectsStr);
                count++;
            }
        }
        return count;
    }

    private int importExcel(MultipartFile file) throws Exception {
        int count = 0;
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) throw new InvalidRequestException("Empty spreadsheet");

            int regIdx = -1, rollIdx = -1, nameIdx = -1, deptIdx = -1, ayIdx = -1, semIdx = -1, classIdx = -1, yearIdx = -1, emailIdx = -1, phoneIdx = -1, subIdx = -1;
            for (Cell cell : header) {
                String title = cell.getStringCellValue().trim().toLowerCase();
                if (title.contains("register")) regIdx = cell.getColumnIndex();
                else if (title.contains("roll")) rollIdx = cell.getColumnIndex();
                else if (title.contains("name")) nameIdx = cell.getColumnIndex();
                else if (title.contains("department")) deptIdx = cell.getColumnIndex();
                else if (title.contains("academic")) ayIdx = cell.getColumnIndex();
                else if (title.contains("semester")) semIdx = cell.getColumnIndex();
                else if (title.contains("class")) classIdx = cell.getColumnIndex();
                else if (title.contains("year of study")) yearIdx = cell.getColumnIndex();
                else if (title.contains("email")) emailIdx = cell.getColumnIndex();
                else if (title.contains("phone")) phoneIdx = cell.getColumnIndex();
                else if (title.contains("subject")) subIdx = cell.getColumnIndex();
            }

            if (regIdx == -1 || nameIdx == -1) {
                throw new InvalidRequestException("Missing required headers in Excel file (Register Number, Name)");
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                String registerNum = getCellValueAsString(row.getCell(regIdx));
                String rollNum = rollIdx != -1 ? getCellValueAsString(row.getCell(rollIdx)) : "";
                String name = getCellValueAsString(row.getCell(nameIdx));
                String deptCode = deptIdx != -1 ? getCellValueAsString(row.getCell(deptIdx)) : "COM";
                String academicYearName = ayIdx != -1 ? getCellValueAsString(row.getCell(ayIdx)) : "2026-2027";
                String semName = semIdx != -1 ? getCellValueAsString(row.getCell(semName)) : "ODD";
                String classSecName = classIdx != -1 ? getCellValueAsString(row.getCell(classIdx)) : "I B.Com";
                String yearStr = yearIdx != -1 ? getCellValueAsString(row.getCell(yearIdx)) : "1";
                String email = emailIdx != -1 ? getCellValueAsString(row.getCell(emailIdx)) : "";
                String phone = phoneIdx != -1 ? getCellValueAsString(row.getCell(phoneIdx)) : "";
                String subjectsStr = subIdx != -1 ? getCellValueAsString(row.getCell(subIdx)) : "";

                saveImportedRecord(registerNum, rollNum, name, deptCode, academicYearName, semName, classSecName, yearStr, email, phone, subjectsStr);
                count++;
            }
        }
        return count;
    }

    private void saveImportedRecord(String registerNum, String rollNum, String name, String deptCode,
                                    String academicYearName, String semName, String classSecName, String yearStr,
                                    String email, String phone, String subjectsStr) {

        if (registerNum == null || registerNum.isBlank() || name == null || name.isBlank()) {
            throw new InvalidRequestException("Register Number and Name are mandatory for all students");
        }
        if (rollNum == null || rollNum.isBlank()) {
            rollNum = registerNum;
        }

        if (studentRepository.existsByRegisterNumber(registerNum)) {
            // Overwrite existing or skip? Let's skip to keep details intact, or update if user prefers
            return;
        }

        Department department = departmentRepository.findByCode(deptCode)
                .orElseGet(() -> departmentRepository.save(Department.builder().code(deptCode).name(deptCode + " Department").build()));

        AcademicYear ay = academicYearRepository.findByName(academicYearName)
                .orElseGet(() -> academicYearRepository.save(AcademicYear.builder().name(academicYearName).isActive(true).build()));

        Semester sem = semesterRepository.findByName(semName)
                .orElseGet(() -> semesterRepository.save(Semester.builder().name(semName).isActive(true).build()));

        ClassSection section = classSectionRepository.findByName(classSecName)
                .orElseGet(() -> classSectionRepository.save(ClassSection.builder().name(classSecName).build()));

        int yearOfStudy = 1;
        try {
            yearOfStudy = Integer.parseInt(yearStr.trim());
        } catch (Exception ignored) {}

        Student student = Student.builder()
                .registerNumber(registerNum)
                .rollNumber(rollNum)
                .name(name)
                .department(department)
                .academicYear(ay)
                .semester(sem)
                .classSection(section)
                .yearOfStudy(yearOfStudy)
                .email(email)
                .phoneNumber(phone)
                .isActive(true)
                .build();

        java.util.Set<Subject> enrolled = new java.util.HashSet<>();
        if (subjectsStr != null && !subjectsStr.isBlank()) {
            String[] codes = subjectsStr.split(",");
            for (String code : codes) {
                String cleanCode = code.trim();
                if (!cleanCode.isEmpty()) {
                    subjectRepository.findByCode(cleanCode).ifPresent(enrolled::add);
                }
            }
        }
        if (enrolled.isEmpty()) {
            java.util.List<Subject> semesterSubjects = subjectRepository.findBySemesterId(sem.getId());
            enrolled.addAll(semesterSubjects);
        }
        student.setEnrolledSubjects(enrolled);

        studentRepository.save(student);
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double d = cell.getNumericCellValue();
                if (d == (long) d) {
                    return String.format("%d", (long) d);
                } else {
                    return String.format("%s", d);
                }
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    private Student buildStudentFromRequest(StudentRequest req, Student target) {
        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        AcademicYear ay = academicYearRepository.findById(req.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic Year not found"));
        Semester sem = semesterRepository.findById(req.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found"));
        ClassSection cs = classSectionRepository.findById(req.getClassSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Class Section not found"));

        target.setRegisterNumber(req.getRegisterNumber());
        target.setRollNumber(req.getRollNumber());
        target.setName(req.getName());
        target.setDepartment(dept);
        target.setAcademicYear(ay);
        target.setSemester(sem);
        target.setClassSection(cs);
        target.setYearOfStudy(req.getYearOfStudy());
        target.setEmail(req.getEmail());
        target.setPhoneNumber(req.getPhoneNumber());
        target.setPhotographUrl(req.getPhotographUrl());
        if (req.getIsActive() != null) {
            target.setIsActive(req.getIsActive());
        }

        if (req.getSubjectIds() != null) {
            Set<Subject> subjects = new HashSet<>(subjectRepository.findAllById(req.getSubjectIds()));
            target.setEnrolledSubjects(subjects);
        } else {
            target.setEnrolledSubjects(new HashSet<>());
        }

        return target;
    }
}
