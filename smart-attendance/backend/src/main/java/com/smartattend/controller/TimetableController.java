package com.smartattend.controller;

import com.smartattend.dto.timetable.TimetableEntryRequest;
import com.smartattend.dto.timetable.TimetableEntryResponse;
import com.smartattend.service.TimetableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/timetable")
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @GetMapping
    public ResponseEntity<List<TimetableEntryResponse>> getActiveTimetable() {
        return ResponseEntity.ok(timetableService.getActiveTimetable());
    }

    @PostMapping("/entries")
    public ResponseEntity<TimetableEntryResponse> createEntry(@Valid @RequestBody TimetableEntryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timetableService.createTimetableEntry(req));
    }

    @PutMapping("/entries/{id}")
    public ResponseEntity<TimetableEntryResponse> updateEntry(@PathVariable Long id, @Valid @RequestBody TimetableEntryRequest req) {
        return ResponseEntity.ok(timetableService.updateTimetableEntry(id, req));
    }
}
