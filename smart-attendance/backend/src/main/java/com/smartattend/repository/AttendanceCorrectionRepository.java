package com.smartattend.repository;

import com.smartattend.entity.AttendanceCorrection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttendanceCorrectionRepository extends JpaRepository<AttendanceCorrection, Long> {
    List<AttendanceCorrection> findByAttendanceRecordAttendanceSessionId(Long sessionId);
    List<AttendanceCorrection> findAllByOrderByChangedAtDesc();
}
