package com.wallet.auth_service.service;

import com.wallet.auth_service.dto.response.LoginResponse;
import com.wallet.auth_service.dto.response.UserResponse;
import com.wallet.auth_service.dto.request.ChangePasswordRequest;
import com.wallet.auth_service.dto.request.LoginRequest;
import com.wallet.auth_service.dto.request.RegisterRequest;
import com.wallet.auth_service.entity.User;
import com.wallet.auth_service.exception.EmailAlreadyUsedException;
import com.wallet.auth_service.exception.InvalidPasswordChangeException;
import com.wallet.auth_service.repository.UserRepository;
import com.wallet.auth_service.security.SecurityUser;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException();
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());

        try {
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyUsedException();
        }
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password())
        );

        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        return tokenService.issueAccessToken(user);
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        verifyCurrentPassword(user.getEmail(), request.currentPassword());

        if (request.newPassword().equals(request.currentPassword())) {
            throw new InvalidPasswordChangeException("New password must be different from the current one");
        }

        userRepository.updatePasswordHash(user.getId(), passwordEncoder.encode(request.newPassword()));
   }
    private void verifyCurrentPassword(String email, String password) {
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        } catch (BadCredentialsException e) {
            throw new InvalidPasswordChangeException("Current password is incorrect");
        }
    }
}
