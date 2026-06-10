package org.wigo.auth.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void blacklistToken(String token, Instant expiryTime) {
        blacklist.put(token, expiryTime);
    }

    public boolean isBlacklisted(String token) {
        Instant expiry = blacklist.get(token);
        if (expiry == null) return false;
        if (expiry.isBefore(Instant.now())) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    public void cleanup() {
        Instant now = Instant.now();
        blacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
