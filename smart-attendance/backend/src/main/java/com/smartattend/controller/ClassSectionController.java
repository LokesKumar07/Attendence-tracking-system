package com.smartattend.controller;

import com.smartattend.entity.ClassSection;
import com.smartattend.repository.ClassSectionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/class-sections")
public class ClassSectionController {

    private final ClassSectionRepository classSectionRepository;

    public ClassSectionController(ClassSectionRepository classSectionRepository) {
        this.classSectionRepository = classSectionRepository;
    }

    @GetMapping
    public ResponseEntity<List<ClassSection>> getAll() {
        return ResponseEntity.ok(classSectionRepository.findAll());
    }
}
