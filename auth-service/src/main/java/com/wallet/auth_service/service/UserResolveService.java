package com.wallet.auth_service.service;

import com.wallet.auth_service.dto.internal.ResolvedUser;
import com.wallet.auth_service.entity.UserStatus;
import com.wallet.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserResolveService {

    private final UserRepository userRepository;

    public List<ResolvedUser> resolveActive(List<UUID> userIds) {
        Set<UUID> unique = new HashSet<>(userIds);   // the same id twice → one lookup
        return userRepository.findAllByIdInAndStatus(unique, UserStatus.ACTIVE)
                .stream()
                .map(ResolvedUser::from)
                .toList();
    }
}