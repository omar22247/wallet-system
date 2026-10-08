package com.wallet.auth_service.service;

import com.wallet.auth_service.dto.reponse.LoginResponse;
import com.wallet.auth_service.dto.reponse.UserResponse;
import com.wallet.auth_service.dto.request.LoginRequest;
import com.wallet.auth_service.dto.request.RegisterRequest;
import com.wallet.auth_service.entity.User;
import com.wallet.auth_service.exception.EmailAlreadyUsedException;
import com.wallet.auth_service.repository.UserRepository;
import com.wallet.auth_service.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
