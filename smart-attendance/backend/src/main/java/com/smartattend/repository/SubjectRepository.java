package com.smartattend.repository;

import com.smartattend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByCode(String code);
    List<Subject> findByIsActive(Boolean isActive);
    List<Subject> findBySemesterId(Long semesterId);
}
