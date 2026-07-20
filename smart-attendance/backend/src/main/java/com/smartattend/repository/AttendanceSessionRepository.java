package com.smartattend.repository;

import com.smartattend.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findByAttendanceDateAndSubjectIdAndPeriodId(LocalDate attendanceDate, Long subjectId, Long periodId);
    List<AttendanceSession> findByAttendanceDate(LocalDate attendanceDate);
    List<AttendanceSession> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);
    List<AttendanceSession> findByAttendanceDateBetweenAndSubjectId(LocalDate startDate, LocalDate endDate, Long subjectId);
}
