package com.smartattend.repository;

import com.smartattend.entity.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {
    Optional<ClassSection> findByName(String name);
}
