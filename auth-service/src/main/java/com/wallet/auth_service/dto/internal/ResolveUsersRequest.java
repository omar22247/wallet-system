package com.wallet.auth_service.dto.internal;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ResolveUsersRequest(
        @NotEmpty
        @Size(max = 10)
        List<@NotNull UUID> userIds
) {}