package com.wallet.wallet_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "auth", url = "${AUTH_SERVICE_URL:http://localhost:8081}")
public interface AuthClient {

    @PostMapping("/internal/users/resolve")
    ResolveResponse resolve(@RequestBody ResolveRequest request);

    record ResolveRequest(Collection<UUID> userIds) {}

    record ResolveResponse(List<ResolvedUser> data) {}
}
