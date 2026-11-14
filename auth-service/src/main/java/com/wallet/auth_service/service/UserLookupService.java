package com.wallet.auth_service.service;

import com.wallet.auth_service.dto.response.UserLookupResponse;
import com.wallet.auth_service.entity.User;
import com.wallet.auth_service.entity.UserStatus;
import com.wallet.auth_service.exception.UserNotFoundException;
import com.wallet.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private static final Logger audit = LoggerFactory.getLogger("SECURITY_AUDIT");

    private final UserRepository userRepository;

    public UserLookupResponse lookupByEmail(UUID requesterId, String email) {
        String normalized = email.trim().toLowerCase();

        Optional<User> user = userRepository.findByEmailAndStatus(normalized, UserStatus.ACTIVE);

        audit.info("user_lookup requester={} found={} target={}",
                requesterId, user.isPresent(), user.map(User::getId).orElse(null));

        return user
                .map(u -> new UserLookupResponse(u.getId()))
                .orElseThrow(UserNotFoundException::new);
    }
}