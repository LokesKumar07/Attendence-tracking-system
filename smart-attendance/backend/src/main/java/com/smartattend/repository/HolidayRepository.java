package com.smartattend.repository;

import com.smartattend.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    Optional<Holiday> findByHolidayDate(LocalDate date);
    boolean existsByHolidayDate(LocalDate date);
    List<Holiday> findByHolidayDateBetween(LocalDate startDate, LocalDate endDate);
}
