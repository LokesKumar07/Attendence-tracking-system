package com.smartattend.service;

import com.smartattend.entity.Holiday;
import com.smartattend.exception.DuplicateResourceException;
import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.repository.HolidayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HolidayService {

    private final HolidayRepository holidayRepository;

    public HolidayService(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    @Transactional
    public Holiday createHoliday(Holiday holiday) {
        if (holidayRepository.existsByHolidayDate(holiday.getHolidayDate())) {
            throw new DuplicateResourceException("Holiday already configured for date: " + holiday.getHolidayDate());
        }
        return holidayRepository.save(holiday);
    }

    @Transactional
    public Holiday updateHoliday(Long id, Holiday details) {
        Holiday existing = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found"));

        if (!existing.getHolidayDate().equals(details.getHolidayDate()) &&
                holidayRepository.existsByHolidayDate(details.getHolidayDate())) {
            throw new DuplicateResourceException("Holiday already configured for date: " + details.getHolidayDate());
        }

        existing.setHolidayDate(details.getHolidayDate());
        existing.setDescription(details.getDescription());
        return holidayRepository.save(existing);
    }

    @Transactional
    public void deleteHoliday(Long id) {
        if (!holidayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Holiday not found");
        }
        holidayRepository.deleteById(id);
    }
}
