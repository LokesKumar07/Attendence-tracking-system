# Database Schema Documentation

SmartAttend database runs on MySQL InnoDB engines with UTF-8 character collations.

## Tables
* **teachers**: User details, lockout parameters.
* **departments**: Code and department names.
* **academic_years**: List of years.
* **semesters**: Semester names (ODD, EVEN).
* **class_sections**: Class section tags (e.g. "I B.Com", "II B.Com (PA)").
* **students**: Profile register fields.
* **subjects**: Course type and code records.
* **periods**: Session schedule numbers and time limits.
* **timetables**: Timetable grid labels.
* **timetable_entries**: Weekly day mappings.
* **holidays**: Custom holiday dates.
* **attendance_sessions**: Generated class instances.
* **attendance_records**: Marks (PRESENT, ABSENT, LATE, ON_DUTY).
* **attendance_corrections**: Log changes for audit.
* **attendance_audit_logs**: Security action audit trails.
* **refresh_tokens**: Session handshake keys.
* **password_reset_tokens**: Passwords reset hashes.
* **login_attempts**: Rate-limiting trackers.
* **system_settings**: Configuration settings.
