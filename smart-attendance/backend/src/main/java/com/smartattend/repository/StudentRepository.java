package com.smartattend.repository;

import com.smartattend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRegisterNumber(String registerNumber);
    Optional<Student> findByRollNumber(String rollNumber);
    boolean existsByRegisterNumber(String registerNumber);
    boolean existsByRollNumber(String rollNumber);
    List<Student> findByEnrolledSubjectsIdAndIsActiveTrue(Long subjectId);


    @Query("SELECT s FROM Student s WHERE s.isActive = true " +
           "AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.registerNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Student> searchActiveStudents(@Param("search") String search);

    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN s.enrolledSubjects sub " +
           "WHERE (:isActive IS NULL OR s.isActive = :isActive) " +
           "AND (:subjectId IS NULL OR sub.id = :subjectId) " +
           "AND (:classSectionId IS NULL OR s.classSection.id = :classSectionId) " +
           "AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.registerNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Student> filterStudents(
        @Param("isActive") Boolean isActive,
        @Param("subjectId") Long subjectId,
        @Param("classSectionId") Long classSectionId,
        @Param("search") String search
    );
}
