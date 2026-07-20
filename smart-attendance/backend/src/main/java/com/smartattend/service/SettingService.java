package com.smartattend.service;

import com.smartattend.entity.SystemSetting;
import com.smartattend.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SettingService {

    private final SystemSettingRepository settingRepository;

    public SettingService(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public List<SystemSetting> getAllSettings() {
        return settingRepository.findAll();
    }

    public String getSetting(String key, String defaultValue) {
        return settingRepository.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }

    @Transactional
    public void updateSetting(String key, String value) {
        SystemSetting setting = settingRepository.findBySettingKey(key)
                .orElseGet(() -> SystemSetting.builder().settingKey(key).build());
        setting.setSettingValue(value);
        settingRepository.save(setting);
    }
}
