package com.wallet.auth_service.controller;

import com.wallet.auth_service.dto.internal.ResolveUsersRequest;
import com.wallet.auth_service.dto.internal.ResolvedUser;
import com.wallet.auth_service.dto.response.ApiResponse;
import com.wallet.auth_service.service.UserResolveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserResolveService userResolveService;

    @PostMapping("/resolve")
    public ResponseEntity<ApiResponse<List<ResolvedUser>>> resolve(
            @Valid @RequestBody ResolveUsersRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Users resolved", userResolveService.resolveActive(request.userIds())));
    }
}