package com.smartattend.service;

import com.smartattend.dto.subject.SubjectRequest;
import com.smartattend.dto.subject.SubjectResponse;
import com.smartattend.entity.Semester;
import com.smartattend.entity.Subject;
import com.smartattend.exception.DuplicateResourceException;
import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.mapper.SubjectMapper;
import com.smartattend.repository.SemesterRepository;
import com.smartattend.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;

    public SubjectService(SubjectRepository subjectRepository, SemesterRepository semesterRepository) {
        this.subjectRepository = subjectRepository;
        this.semesterRepository = semesterRepository;
    }

    public List<SubjectResponse> getAllSubjects(Boolean isActive) {
        List<Subject> list = (isActive == null) ? subjectRepository.findAll() : subjectRepository.findByIsActive(isActive);
        return list.stream().map(SubjectMapper::toResponse).collect(Collectors.toList());
    }

    public SubjectResponse getSubjectById(Long id) {
        Subject s = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        return SubjectMapper.toResponse(s);
    }

    @Transactional
    public SubjectResponse createSubject(SubjectRequest req) {
        if (subjectRepository.findByCode(req.getCode()).isPresent()) {
            throw new DuplicateResourceException("Subject with code " + req.getCode() + " already exists");
        }

        Semester sem = semesterRepository.findById(req.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found"));

        Subject s = Subject.builder()
                .code(req.getCode())
                .name(req.getName())
                .type(req.getType())
                .semester(sem)
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .build();

        Subject saved = subjectRepository.save(s);
        return SubjectMapper.toResponse(saved);
    }

    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectRequest req) {
        Subject existing = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        if (!existing.getCode().equalsIgnoreCase(req.getCode()) &&
                subjectRepository.findByCode(req.getCode()).isPresent()) {
            throw new DuplicateResourceException("Subject with code " + req.getCode() + " already exists");
        }

        Semester sem = semesterRepository.findById(req.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found"));

        existing.setCode(req.getCode());
        existing.setName(req.getName());
        existing.setType(req.getType());
        existing.setSemester(sem);
        if (req.getIsActive() != null) {
            existing.setIsActive(req.getIsActive());
        }

        Subject saved = subjectRepository.save(existing);
        return SubjectMapper.toResponse(saved);
    }

    @Transactional
    public void deleteOrDeactivateSubject(Long id) {
        Subject existing = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        
        // Soft delete/deactivate so old session records remain safe
        existing.setIsActive(false);
        subjectRepository.save(existing);
    }
}
