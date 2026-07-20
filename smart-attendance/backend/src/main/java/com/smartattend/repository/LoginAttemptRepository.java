package com.smartattend.repository;

import com.smartattend.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByUsernameAndAttemptTimeAfter(String username, LocalDateTime time);
    List<LoginAttempt> findByIpAddressAndAttemptTimeAfter(String ipAddress, LocalDateTime time);
}
