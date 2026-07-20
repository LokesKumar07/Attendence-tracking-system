package com.smartattend.controller;

import com.smartattend.entity.Holiday;
import com.smartattend.service.HolidayService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping
    public ResponseEntity<List<Holiday>> getAll() {
        return ResponseEntity.ok(holidayService.getAllHolidays());
    }

    @PostMapping
    public ResponseEntity<Holiday> create(@Valid @RequestBody Holiday holiday) {
        return ResponseEntity.status(HttpStatus.CREATED).body(holidayService.createHoliday(holiday));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Holiday> update(@PathVariable Long id, @Valid @RequestBody Holiday holiday) {
        return ResponseEntity.ok(holidayService.updateHoliday(id, holiday));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.ok(Map.of("message", "Holiday successfully deleted"));
    }
}
