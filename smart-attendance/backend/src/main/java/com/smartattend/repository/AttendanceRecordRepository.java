package com.smartattend.repository;

import com.smartattend.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByAttendanceSessionId(Long sessionId);
    Optional<AttendanceRecord> findByAttendanceSessionIdAndStudentId(Long sessionId, Long studentId);
    List<AttendanceRecord> findByStudentId(Long studentId);
    
    // For reports
    List<AttendanceRecord> findByAttendanceSessionAttendanceDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);
    List<AttendanceRecord> findByAttendanceSessionAttendanceDateBetweenAndStudentId(java.time.LocalDate startDate, java.time.LocalDate endDate, Long studentId);
}
