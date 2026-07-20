package com.smartattend.controller;

import com.smartattend.dto.student.StudentRequest;
import com.smartattend.dto.student.StudentResponse;
import com.smartattend.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAll(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long classSectionId,
            @RequestParam(required = false) String search) {
        List<StudentResponse> list = studentService.getAllStudents(isActive, subjectId, classSectionId, search);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentRequest req) {
        return ResponseEntity.ok(studentService.updateStudent(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        studentService.deleteOrDeactivateStudent(id);
        return ResponseEntity.ok(Map.of("message", "Student successfully deleted or deactivated"));
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importStudents(@RequestParam("file") MultipartFile file) {
        int count = studentService.importStudents(file);
        return ResponseEntity.ok(Map.of("message", "Successfully imported " + count + " students"));
    }
}
