package com.smartattend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(name = "attempt_time", updatable = false)
    private LocalDateTime attemptTime;

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    @PrePersist
    protected void onCreate() {
        attemptTime = LocalDateTime.now();
    }
}
