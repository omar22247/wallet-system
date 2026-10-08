package com.wallet.auth_service.security;

import com.wallet.auth_service.entity.Role;
import com.wallet.auth_service.entity.User;
import com.wallet.auth_service.entity.UserStatus;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


public class SecurityUser implements UserDetails, CredentialsContainer {

    private final UUID id;
    private final String email;
    private String passwordHash;
    private final Role role;
    private final UserStatus status;
    private final Instant lockedUntil;
    private final int failedLoginAttempts;

    public SecurityUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.lockedUntil = user.getLockedUntil();
        this.failedLoginAttempts = user.getFailedLoginAttempts();
    }

    public UUID getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public boolean hasFailedAttempts() {
        return failedLoginAttempts > 0 || lockedUntil != null;
    }


    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonLocked() {
        if (status == UserStatus.LOCKED) {
            return false;
        }
        return lockedUntil == null || lockedUntil.isBefore(Instant.now());
    }

    @Override
    public boolean isEnabled() {
        return status != UserStatus.DISABLED;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }
}
