package com.wallet.wallet_service.client;

import java.util.UUID;

public record ResolvedUser(UUID userId, String email) {}