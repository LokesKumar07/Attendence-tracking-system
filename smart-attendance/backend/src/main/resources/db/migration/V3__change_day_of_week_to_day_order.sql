-- Create a supporting index on timetable_id so we can drop uq_day_period
ALTER TABLE timetable_entries ADD KEY idx_timetable_id (timetable_id);

-- Drop unique key constraint uq_day_period which references day_of_week
ALTER TABLE timetable_entries DROP INDEX uq_day_period;

-- Add day_order column
ALTER TABLE timetable_entries ADD COLUMN day_order INT;

-- Populate day_order from day_of_week
UPDATE timetable_entries SET day_order = CASE day_of_week
    WHEN 'MONDAY' THEN 1
    WHEN 'TUESDAY' THEN 2
    WHEN 'WEDNESDAY' THEN 3
    WHEN 'THURSDAY' THEN 4
    WHEN 'FRIDAY' THEN 5
    WHEN 'SATURDAY' THEN 6
    ELSE 1
END;

-- Enforce NOT NULL on day_order
ALTER TABLE timetable_entries MODIFY COLUMN day_order INT NOT NULL;

-- Remove day_of_week
ALTER TABLE timetable_entries DROP COLUMN day_of_week;

-- Recreate unique key constraint uq_day_period referencing day_order
ALTER TABLE timetable_entries ADD UNIQUE KEY uq_day_period (timetable_id, day_order, period_number);

-- Drop the temporary index idx_timetable_id since uq_day_period now supports foreign keys
ALTER TABLE timetable_entries DROP INDEX idx_timetable_id;
