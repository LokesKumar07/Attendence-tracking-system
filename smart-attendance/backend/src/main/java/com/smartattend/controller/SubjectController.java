package com.smartattend.controller;

import com.smartattend.dto.subject.SubjectRequest;
import com.smartattend.dto.subject.SubjectResponse;
import com.smartattend.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getAll(@RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(subjectService.getAllSubjects(isActive));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getSubjectById(id));
    }

    @PostMapping
    public ResponseEntity<SubjectResponse> create(@Valid @RequestBody SubjectRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.createSubject(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponse> update(@PathVariable Long id, @Valid @RequestBody SubjectRequest req) {
        return ResponseEntity.ok(subjectService.updateSubject(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        subjectService.deleteOrDeactivateSubject(id);
        return ResponseEntity.ok(Map.of("message", "Subject successfully deleted or deactivated"));
    }
}
