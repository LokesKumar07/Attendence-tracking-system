package com.smartattend.repository;

import com.smartattend.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findByTimetableId(Long timetableId);
    List<TimetableEntry> findByTimetableIsActiveTrue();
    List<TimetableEntry> findByTimetableIsActiveTrueAndDayOrder(Integer dayOrder);
    Optional<TimetableEntry> findByTimetableIdAndDayOrderAndPeriodNumber(Long timetableId, Integer dayOrder, Integer periodNumber);
}
