package com.smartattend.service;

import com.smartattend.dto.auth.*;
import com.smartattend.entity.*;
import com.smartattend.exception.*;
import com.smartattend.repository.*;
import com.smartattend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final TeacherRepository teacherRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    public AuthService(TeacherRepository teacherRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository resetTokenRepository,
                       LoginAttemptRepository loginAttemptRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditService auditService) {
        this.teacherRepository = teacherRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        String input = request.getUsernameOrEmail();
        
        // Find teacher
        Optional<Teacher> teacherOpt = teacherRepository.findByUsernameOrEmail(input, input);
        if (teacherOpt.isEmpty()) {
            trackAttempt(input, ipAddress, false);
            throw new UnauthorizedException("Invalid username or password.");
        }

        Teacher teacher = teacherOpt.get();

        // Check Lockout
        if (teacher.getLockoutUntil() != null && LocalDateTime.now().isBefore(teacher.getLockoutUntil())) {
            throw new UnauthorizedException("Account is temporarily locked. Try again later.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), teacher.getPasswordHash())) {
            int attempts = teacher.getFailedLoginAttempts() + 1;
            teacher.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                teacher.setLockoutUntil(LocalDateTime.now().plusMinutes(15));
                teacher.setFailedLoginAttempts(0); // reset count after lock is applied
            }
            teacherRepository.save(teacher);
            trackAttempt(input, ipAddress, false);
            throw new UnauthorizedException("Invalid username or password.");
        }

        // Success - clear lockout and failed attempts
        teacher.setFailedLoginAttempts(0);
        teacher.setLockoutUntil(null);
        teacherRepository.save(teacher);
        
        trackAttempt(input, ipAddress, true);

        // Generate tokens
        String accessToken = tokenProvider.generateToken(teacher.getUsername());
        String refreshTokenStr = generateRefreshToken(teacher);

        auditService.log(teacher, "LOGIN", "Logged in successfully", ipAddress);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .username(teacher.getUsername())
                .fullName(teacher.getFullName())
                .email(teacher.getEmail())
                .build();
    }

    @Transactional
    public void logout(String refreshTokenVal, String username, String ipAddress) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshTokenVal);
        if (tokenOpt.isPresent()) {
            RefreshToken token = tokenOpt.get();
            token.setIsRevoked(true);
            refreshTokenRepository.save(token);
            
            Teacher teacher = token.getTeacher();
            auditService.log(teacher, "LOGOUT", "Logged out successfully", ipAddress);
        }
    }

    @Transactional
    public LoginResponse rotateToken(RefreshTokenRequest request, String ipAddress) {
        String tokenStr = request.getRefreshToken();
        RefreshToken token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new UnauthorizedException("Invalid or revoked refresh token"));

        if (token.getIsRevoked()) {
            // Revocation strategy: if someone reuses a revoked token, revoke all of that teacher's tokens
            refreshTokenRepository.deleteByTeacher(token.getTeacher());
            throw new UnauthorizedException("Refresh token was previously revoked. All tokens cleared.");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            token.setIsRevoked(true);
            refreshTokenRepository.save(token);
            throw new UnauthorizedException("Refresh token expired");
        }

        // Rotate
        token.setIsRevoked(true);
        refreshTokenRepository.save(token);

        Teacher teacher = token.getTeacher();
        String newAccessToken = tokenProvider.generateToken(teacher.getUsername());
        String newRefreshToken = generateRefreshToken(teacher);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .username(teacher.getUsername())
                .fullName(teacher.getFullName())
                .email(teacher.getEmail())
                .build();
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request, String ipAddress) {
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), teacher.getPasswordHash())) {
            throw new InvalidRequestException("Incorrect current password");
        }

        teacher.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        teacherRepository.save(teacher);

        auditService.log(teacher, "CHANGE_PASSWORD", "Password updated successfully", ipAddress);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, String ipAddress) {
        // To prevent account enumeration, return success even if username/email doesn't exist
        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(request.getEmail());
        if (teacherOpt.isPresent()) {
            Teacher teacher = teacherOpt.get();
            String token = UUID.randomUUID().toString();
            
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .teacher(teacher)
                    .token(token)
                    .expiryDate(Instant.now().plusSeconds(1800)) // 30 minutes
                    .isUsed(false)
                    .build();
            resetTokenRepository.save(resetToken);
            
            // Log local email simulation (no actual SMTP configured unless needed)
            System.out.println("SIMULATING EMAIL FOR FORGOT PASSWORD: reset token is: " + token);
            auditService.log(teacher, "FORGOT_PASSWORD_REQUEST", "Password reset request initiated", ipAddress);
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request, String ipAddress) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidRequestException("Invalid or expired password reset token"));

        if (resetToken.getIsUsed() || resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidRequestException("Reset token has already been used or has expired");
        }

        Teacher teacher = resetToken.getTeacher();
        teacher.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        teacher.setFailedLoginAttempts(0);
        teacher.setLockoutUntil(null);
        teacherRepository.save(teacher);

        resetToken.setIsUsed(true);
        resetTokenRepository.save(resetToken);

        auditService.log(teacher, "RESET_PASSWORD", "Password reset successfully via token", ipAddress);
    }

    private String generateRefreshToken(Teacher teacher) {
        String tokenVal = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .teacher(teacher)
                .token(tokenVal)
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return tokenVal;
    }

    private void trackAttempt(String username, String ipAddress, boolean isSuccess) {
        LoginAttempt attempt = LoginAttempt.builder()
                .username(username)
                .ipAddress(ipAddress)
                .isSuccess(isSuccess)
                .build();
        loginAttemptRepository.save(attempt);
    }

    public List<String> getTeacherUsernames() {
        return teacherRepository.findAll().stream().map(Teacher::getUsername).toList();
    }
}
