package com.dataprotection.dataprotection.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration DECOY_WINDOW = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public boolean shouldRedirectToFakeUi(String key) {
        AttemptState state = attempts.get(key);
        if (state == null) {
            return false;
        }

        if (state.decoyUntil() != null && state.decoyUntil().isAfter(LocalDateTime.now())) {
            return true;
        }

        if (state.decoyUntil() != null && !state.decoyUntil().isAfter(LocalDateTime.now())) {
            attempts.remove(key);
        }

        return false;
    }

    public int recordFailure(String key) {
        AttemptState state = attempts.compute(key, (_ignored, current) -> {
            int nextFailedAttempts = current == null ? 1 : current.failedAttempts() + 1;
            LocalDateTime decoyUntil = nextFailedAttempts >= MAX_FAILED_ATTEMPTS
                    ? LocalDateTime.now().plus(DECOY_WINDOW)
                    : null;
            return new AttemptState(nextFailedAttempts, decoyUntil);
        });

        return state.failedAttempts();
    }

    public int getRemainingAttempts(int failedAttempts) {
        return Math.max(0, MAX_FAILED_ATTEMPTS - failedAttempts);
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private record AttemptState(int failedAttempts, LocalDateTime decoyUntil) {
    }
}
