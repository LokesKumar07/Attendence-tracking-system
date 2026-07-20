package com.smartattend;

import com.smartattend.dto.timetable.TimetableEntryRequest;
import com.smartattend.entity.*;
import com.smartattend.exception.InvalidRequestException;
import com.smartattend.repository.*;
import com.smartattend.service.TimetableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class SmartAttendApplicationTests {

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private TimetableEntryRepository entryRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private TimetableService timetableService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTimetableClashDetection_OverlappingTime() {
        TimetableEntryRequest req = TimetableEntryRequest.builder()
                .timetableId(1L)
                .dayOrder(1)
                .periodNumber(2)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .entryType(TimetableEntryType.CLASS)
                .build();

        List<TimetableEntry> existing = new ArrayList<>();
        existing.add(TimetableEntry.builder()
                .id(100L)
                .periodNumber(1)
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 30))
                .entryType(TimetableEntryType.CLASS)
                .build());

        when(entryRepository.findByTimetableIsActiveTrueAndDayOrder(1)).thenReturn(existing);
        when(timetableRepository.findById(1L)).thenReturn(Optional.of(new Timetable()));

        assertThrows(InvalidRequestException.class, () -> {
            timetableService.createTimetableEntry(req);
        });
    }

    @Test
    void testTimetableClashDetection_DuplicatePeriod() {
        TimetableEntryRequest req = TimetableEntryRequest.builder()
                .timetableId(1L)
                .dayOrder(1)
                .periodNumber(1)
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(13, 0))
                .entryType(TimetableEntryType.CLASS)
                .build();

        List<TimetableEntry> existing = new ArrayList<>();
        existing.add(TimetableEntry.builder()
                .id(100L)
                .periodNumber(1)
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 30))
                .entryType(TimetableEntryType.CLASS)
                .build());

        when(entryRepository.findByTimetableIsActiveTrueAndDayOrder(1)).thenReturn(existing);
        when(timetableRepository.findById(1L)).thenReturn(Optional.of(new Timetable()));

        assertThrows(InvalidRequestException.class, () -> {
            timetableService.createTimetableEntry(req);
        });
    }
}
