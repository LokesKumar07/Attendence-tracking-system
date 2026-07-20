package com.smartattend.controller;

import com.smartattend.entity.SystemSetting;
import com.smartattend.service.SettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    public ResponseEntity<List<SystemSetting>> getAll() {
        return ResponseEntity.ok(settingService.getAllSettings());
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> update(@RequestBody Map<String, String> payload) {
        payload.forEach(settingService::updateSetting);
        return ResponseEntity.ok(Map.of("message", "Settings updated successfully"));
    }
}
