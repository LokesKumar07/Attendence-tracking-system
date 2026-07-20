package com.smartattend;

import com.smartattend.entity.Teacher;
import com.smartattend.repository.TeacherRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

@SpringBootApplication
@EnableScheduling
public class SmartAttendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartAttendApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDatabase(TeacherRepository teacherRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<Teacher> teacherOpt = teacherRepository.findByUsername("lathika");
            if (teacherOpt.isPresent()) {
                System.out.println("Updating password for default teacher 'lathika' to ensure it matches 'lathika123'...");
                Teacher lathika = teacherOpt.get();
                lathika.setPasswordHash(passwordEncoder.encode("lathika123"));
                lathika.setFailedLoginAttempts(0);
                lathika.setLockoutUntil(null);
                teacherRepository.save(lathika);
                System.out.println("Default teacher password updated successfully!");
            } else {
                System.out.println("Seeding default teacher 'lathika'...");
                Teacher lathika = Teacher.builder()
                        .username("lathika")
                        .email("lathika@ppg.edu.in")
                        .passwordHash(passwordEncoder.encode("lathika123"))
                        .fullName("Ms. V. Lathika")
                        .isActive(true)
                        .failedLoginAttempts(0)
                        .build();
                teacherRepository.save(lathika);
                System.out.println("Default teacher 'lathika' seeded successfully!");
            }
        };
    }
}
