package com.smartattend.security;

import com.smartattend.entity.Teacher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Teacher teacher;

    public CustomUserDetails(Teacher teacher) {
        this.teacher = teacher;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER"));
    }

    @Override
    public String getPassword() {
        return teacher.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return teacher.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (teacher.getLockoutUntil() == null) {
            return true;
        }
        return java.time.LocalDateTime.now().isAfter(teacher.getLockoutUntil());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return teacher.getIsActive();
    }
}
