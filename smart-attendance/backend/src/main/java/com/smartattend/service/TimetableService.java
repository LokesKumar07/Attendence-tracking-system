package com.smartattend.service;

import com.smartattend.dto.timetable.TimetableEntryRequest;
import com.smartattend.dto.timetable.TimetableEntryResponse;
import com.smartattend.entity.*;
import com.smartattend.exception.InvalidRequestException;
import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.mapper.TimetableMapper;
import com.smartattend.repository.SubjectRepository;
import com.smartattend.repository.TimetableEntryRepository;
import com.smartattend.repository.TimetableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository entryRepository;
    private final SubjectRepository subjectRepository;

    public TimetableService(TimetableRepository timetableRepository,
                            TimetableEntryRepository entryRepository,
                            SubjectRepository subjectRepository) {
        this.timetableRepository = timetableRepository;
        this.entryRepository = entryRepository;
        this.subjectRepository = subjectRepository;
    }

    public List<TimetableEntryResponse> getActiveTimetable() {
        return entryRepository.findByTimetableIsActiveTrue()
                .stream()
                .map(TimetableMapper::toEntryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TimetableEntryResponse updateTimetableEntry(Long id, TimetableEntryRequest req) {
        TimetableEntry existing = entryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable entry not found"));

        validateTimes(req.getStartTime(), req.getEndTime());
        checkForClash(req, id);

        Subject subject = null;
        if (req.getSubjectId() != null) {
            subject = subjectRepository.findById(req.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        }

        existing.setDayOrder(req.getDayOrder());
        existing.setPeriodNumber(req.getPeriodNumber());
        existing.setStartTime(req.getStartTime());
        existing.setEndTime(req.getEndTime());
        existing.setSubject(subject);
        existing.setEntryType(req.getEntryType());

        TimetableEntry saved = entryRepository.save(existing);
        return TimetableMapper.toEntryResponse(saved);
    }

    @Transactional
    public TimetableEntryResponse createTimetableEntry(TimetableEntryRequest req) {
        Timetable timetable = timetableRepository.findById(req.getTimetableId())
                .orElseThrow(() -> new ResourceNotFoundException("Timetable not found"));

        validateTimes(req.getStartTime(), req.getEndTime());
        checkForClash(req, null);

        Subject subject = null;
        if (req.getSubjectId() != null) {
            subject = subjectRepository.findById(req.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        }

        TimetableEntry entry = TimetableEntry.builder()
                .timetable(timetable)
                .dayOrder(req.getDayOrder())
                .periodNumber(req.getPeriodNumber())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .subject(subject)
                .entryType(req.getEntryType())
                .build();

        TimetableEntry saved = entryRepository.save(entry);
        return TimetableMapper.toEntryResponse(saved);
    }

    private void validateTimes(LocalTime start, LocalTime end) {
        if (start.isAfter(end) || start.equals(end)) {
            throw new InvalidRequestException("Start time must be strictly before end time");
        }
    }

    private void checkForClash(TimetableEntryRequest req, Long entryId) {
        List<TimetableEntry> activeEntries = entryRepository.findByTimetableIsActiveTrueAndDayOrder(req.getDayOrder());
        for (TimetableEntry existing : activeEntries) {
            if (entryId != null && existing.getId().equals(entryId)) {
                continue; // Skip self
            }
            
            // Check if period overlaps: (start1 < end2) AND (start2 < end1)
            boolean overlap = (req.getStartTime().isBefore(existing.getEndTime())) &&
                              (existing.getStartTime().isBefore(req.getEndTime()));
            
            if (overlap) {
                throw new InvalidRequestException("Timetable clash: Overlaps with an existing period " + 
                        existing.getPeriodNumber() + " (" + existing.getStartTime() + " - " + existing.getEndTime() + ")");
            }
 
            if (existing.getPeriodNumber().equals(req.getPeriodNumber())) {
                throw new InvalidRequestException("Period number " + req.getPeriodNumber() + " is already occupied on Day Order " + req.getDayOrder());
            }
        }
    }
}
