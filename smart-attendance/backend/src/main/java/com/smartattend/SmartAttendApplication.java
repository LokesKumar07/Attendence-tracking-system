package com.smartattend;

import com.smartattend.entity.Teacher;
import com.smartattend.repository.TeacherRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class SmartAttendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartAttendApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDatabase(TeacherRepository teacherRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (teacherRepository.findByUsername("lathika").isEmpty()) {
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
