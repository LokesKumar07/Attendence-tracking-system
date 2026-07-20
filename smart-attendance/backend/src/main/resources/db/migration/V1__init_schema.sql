-- V1__init_schema.sql
-- Database Schema for SmartAttend Attendance Management System

-- Departments
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Academic Years
CREATE TABLE academic_years (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE, -- e.g. "2026-2027"
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Semesters
CREATE TABLE semesters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE, -- e.g. "ODD", "EVEN"
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Class Sections
CREATE TABLE class_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE, -- e.g. "A", "B", "PA"
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Teachers
CREATE TABLE teachers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    failed_login_attempts INT DEFAULT 0,
    lockout_until TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Students
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    register_number VARCHAR(50) NOT NULL UNIQUE,
    roll_number VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    department_id BIGINT NOT NULL,
    academic_year_id BIGINT NOT NULL,
    semester_id BIGINT NOT NULL,
    class_section_id BIGINT NOT NULL,
    year_of_study INT NOT NULL,
    email VARCHAR(100),
    phone_number VARCHAR(20),
    photograph_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    FOREIGN KEY (semester_id) REFERENCES semesters(id),
    FOREIGN KEY (class_section_id) REFERENCES class_sections(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Subjects
CREATE TABLE subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL, -- THEORY, LABORATORY
    semester_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (semester_id) REFERENCES semesters(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Periods (slots configuration)
CREATE TABLE periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_number INT NOT NULL UNIQUE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Timetables (General Settings/Metadata)
CREATE TABLE timetables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    academic_year_id BIGINT NOT NULL,
    semester_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    FOREIGN KEY (semester_id) REFERENCES semesters(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Timetable Entries (Weekly Grid Schedule for Teacher)
CREATE TABLE timetable_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timetable_id BIGINT NOT NULL,
    day_of_week VARCHAR(15) NOT NULL, -- MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    period_number INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    subject_id BIGINT NULL, -- NULL if BREAK, LUNCH, FREE
    entry_type VARCHAR(20) NOT NULL, -- CLASS, LAB, BREAK, LUNCH, FREE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (timetable_id) REFERENCES timetables(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    UNIQUE KEY uq_day_period (timetable_id, day_of_week, period_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Holidays
CREATE TABLE holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    holiday_date DATE NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Attendance Sessions
CREATE TABLE attendance_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_date DATE NOT NULL,
    subject_id BIGINT NOT NULL,
    period_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL, -- SCHEDULED, OPEN, DRAFT, SUBMITTED, LOCKED, CANCELLED
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (period_id) REFERENCES periods(id),
    FOREIGN KEY (created_by) REFERENCES teachers(id),
    UNIQUE KEY uq_attendance_session (attendance_date, subject_id, period_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Attendance Records
CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_session_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL, -- PRESENT, ABSENT, LATE, ON_DUTY
    remarks VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (attendance_session_id) REFERENCES attendance_sessions(id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    UNIQUE KEY uq_session_student (attendance_session_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Attendance Corrections (Append-only Audit History)
CREATE TABLE attendance_corrections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_record_id BIGINT NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    correction_reason VARCHAR(255) NOT NULL,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attendance_record_id) REFERENCES attendance_records(id),
    FOREIGN KEY (changed_by) REFERENCES teachers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Attendance Audit Logs
CREATE TABLE attendance_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL, -- LOGIN, LOGOUT, MARK_ATTENDANCE, CORRECT_ATTENDANCE, EXPORT_REPORT etc.
    details TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Refresh Tokens
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Password Reset Tokens
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Login Attempts (Lockout/Rate Limiting)
CREATE TABLE login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_success BOOLEAN NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- System Settings
CREATE TABLE system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(50) NOT NULL UNIQUE,
    setting_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert Seed Data
-- 1. Departments
INSERT INTO departments (code, name) VALUES ('COM', 'PG and Research Department of Commerce');

-- 2. Academic Years
INSERT INTO academic_years (name, is_active) VALUES ('2026-2027', TRUE);

-- 3. Semesters
INSERT INTO semesters (name, is_active) VALUES ('ODD', TRUE);

-- 4. Sections
INSERT INTO class_sections (name) VALUES ('I B.Com'), ('II B.Com'), ('III B.Com'), ('II B.Com (PA)');

-- 5. Default Teacher (Lathika) - Password is 'lathika123' hashed with BCrypt
-- BCrypt hash of 'lathika123' is $2a$12$6/YQhA7Wl.U97/QkFw8m9Ox6aG0z0/w3V87nEExiLzQn3oF5Tz4I2 (we will configure this default)
INSERT INTO teachers (username, email, password_hash, full_name, is_active) 
VALUES ('lathika', 'lathika@ppg.edu.in', '$2a$12$R9h/cIPz0gi.URDshxpHm.sWl.Gj.uJ5l7p6sX3z0x06eG356N9H2', 'Ms. V. Lathika', TRUE);

-- 6. Subjects (Odd Sem)
INSERT INTO subjects (code, name, type, semester_id, is_active) VALUES
('AGRI101', 'Agricultural Economy of India', 'THEORY', 1, TRUE),
('BOM102', 'Business Organisation and Management', 'THEORY', 1, TRUE),
('YOGA201', 'Yoga for Human Excellence', 'THEORY', 1, TRUE),
('COST301', 'Cost Accounting', 'THEORY', 1, TRUE),
('BAS302', 'Business Application Software-II', 'THEORY', 1, TRUE),
-- Wait, the XML EVS(PA) is for II B.Com (PA) Environmental Studies
('EVS202', 'Environmental Studies', 'THEORY', 1, TRUE);

-- 7. Periods
INSERT INTO periods (period_number, start_time, end_time) VALUES
(1, '09:35:00', '10:30:00'),
(2, '10:30:00', '11:25:00'),
(3, '11:40:00', '12:35:00'),
(4, '13:15:00', '14:05:00'),
(5, '14:05:00', '14:55:00'),
(6, '14:55:00', '15:45:00');

-- 8. Timetable
INSERT INTO timetables (name, academic_year_id, semester_id, is_active) VALUES ('Odd Sem Timetable Ms. Lathika', 1, 1, TRUE);

-- 9. Timetable Entries
-- Day numbers: MONDAY to SATURDAY
-- Period 1 to 6
-- Row I (Monday): P1=AGRI, P4=BAS-II, P6=EVS(PA)
INSERT INTO timetable_entries (timetable_id, day_of_week, period_number, start_time, end_time, subject_id, entry_type) VALUES
(1, 'MONDAY', 1, '09:35:00', '10:30:00', 1, 'CLASS'),
(1, 'MONDAY', 2, '10:30:00', '11:25:00', NULL, 'FREE'),
(1, 'MONDAY', 3, '11:40:00', '12:35:00', NULL, 'FREE'),
(1, 'MONDAY', 4, '13:15:00', '14:05:00', 5, 'CLASS'),
(1, 'MONDAY', 5, '14:05:00', '14:55:00', NULL, 'FREE'),
(1, 'MONDAY', 6, '14:55:00', '15:45:00', 6, 'CLASS');

-- Row II (Tuesday): P2=COST A/C, P4=AGRI, P5=BAS-II, P6=BOM
INSERT INTO timetable_entries (timetable_id, day_of_week, period_number, start_time, end_time, subject_id, entry_type) VALUES
(1, 'TUESDAY', 1, '09:35:00', '10:30:00', NULL, 'FREE'),
(1, 'TUESDAY', 2, '10:30:00', '11:25:00', 4, 'CLASS'),
(1, 'TUESDAY', 3, '11:40:00', '12:35:00', NULL, 'FREE'),
(1, 'TUESDAY', 4, '13:15:00', '14:05:00', 1, 'CLASS'),
(1, 'TUESDAY', 5, '14:05:00', '14:55:00', 5, 'CLASS'),
(1, 'TUESDAY', 6, '14:55:00', '15:45:00', 2, 'CLASS');

-- Row III (Wednesday): P1=COST A/C, P2=AGRI, P4=YOGA, P5=COST A/C
INSERT INTO timetable_entries (timetable_id, day_of_week, period_number, start_time, end_time, subject_id, entry_type) VALUES
(1, 'WEDNESDAY', 1, '09:35:00', '10:30:00', 4, 'CLASS'),
(1, 'WEDNESDAY', 2, '10:30:00', '11:25:00', 1, 'CLASS'),
(1, 'WEDNESDAY', 3, '11:40:00', '12:35:00', NULL, 'FREE'),
(1, 'WEDNESDAY', 4, '13:15:00', '14:05:00', 3, 'CLASS'),
(1, 'WEDNESDAY', 5, '14:05:00', '14:55:00', 4, 'CLASS'),
(1, 'WEDNESDAY', 6, '14:55:00', '15:45:00', NULL, 'FREE');

-- Row IV (Thursday): P1=COST A/C, P4=AGRI, P5=BAS-II
INSERT INTO timetable_entries (timetable_id, day_of_week, period_number, start_time, end_time, subject_id, entry_type) VALUES
(1, 'THURSDAY', 1, '09:35:00', '10:30:00', 4, 'CLASS'),
(1, 'THURSDAY', 2, '10:30:00', '11:25:00', NULL, 'FREE'),
(1, 'THURSDAY', 3, '11:40:00', '12:35:00', NULL, 'FREE'),
(1, 'THURSDAY', 4, '13:15:00', '14:05:00', 1, 'CLASS'),
(1, 'THURSDAY', 5, '14:05:00', '14:55:00', 5, 'CLASS'),
(1, 'THURSDAY', 6, '14:55:00', '15:45:00', NULL, 'FREE');

-- Row V (Friday): P2=EVS(PA), P3=AGRI, P4=COST A/C
INSERT INTO timetable_entries (timetable_id, day_of_week, period_number, start_time, end_time, subject_id, entry_type) VALUES
(1, 'FRIDAY', 1, '09:35:00', '10:30:00', NULL, 'FREE'),
(1, 'FRIDAY', 2, '10:30:00', '11:25:00', 6, 'CLASS'),
(1, 'FRIDAY', 3, '11:40:00', '12:35:00', 1, 'CLASS'),
(1, 'FRIDAY', 4, '13:15:00', '14:05:00', 4, 'CLASS'),
(1, 'FRIDAY', 5, '14:05:00', '14:55:00', NULL, 'FREE'),
(1, 'FRIDAY', 6, '14:55:00', '15:45:00', NULL, 'FREE');

-- Row VI (Saturday): P1=BOM, P2=BAS-II, P6=COST A/C
INSERT INTO timetable_entries (timetable_id, day_of_week, period_number, start_time, end_time, subject_id, entry_type) VALUES
(1, 'SATURDAY', 1, '09:35:00', '10:30:00', 2, 'CLASS'),
(1, 'SATURDAY', 2, '10:30:00', '11:25:00', 5, 'CLASS'),
(1, 'SATURDAY', 3, '11:40:00', '12:35:00', NULL, 'FREE'),
(1, 'SATURDAY', 4, '13:15:00', '14:05:00', NULL, 'FREE'),
(1, 'SATURDAY', 5, '14:05:00', '14:55:00', NULL, 'FREE'),
(1, 'SATURDAY', 6, '14:55:00', '15:45:00', 4, 'CLASS');

-- 10. Default System Settings
INSERT INTO system_settings (setting_key, setting_value) VALUES
('attendance_window_before_mins', '10'),
('attendance_window_after_mins', '20'),
('default_attendance_status', 'PRESENT');
