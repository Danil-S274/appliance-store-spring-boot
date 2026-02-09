package com.danil.appliances.service.impl;

import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.model.LoginAttempt;
import com.danil.appliances.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginRateLimitService {

    private final LoginAttemptRepository loginAttemptRepository;

    private static final int MAX_FAILS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final Duration GC_AFTER = Duration.ofDays(30);

    public void assertNotLocked(String username, String ip) {
        if (username == null) username = "";
        username = username.trim().toLowerCase();

        this.loginAttemptRepository.findByUsernameAndIp(username, ip).ifPresent(a -> {
            Instant now = Instant.now();
            if (a.getLockedUntil() != null && a.getLockedUntil().isAfter(now)) {
                long seconds = a.getLockedUntil().getEpochSecond() - now.getEpochSecond();
                throw new BusinessException("Too many login attempts. Try again in " + seconds + "s");
            }
        });
    }

    public void onSuccess(String username, String ip) {
        username = (username == null ? "" : username.trim().toLowerCase());
        this.loginAttemptRepository.findByUsernameAndIp(username, ip).ifPresent(this.loginAttemptRepository::delete);
    }

    public void onFailure(String username, String ip) {
        username = (username == null ? "" : username.trim().toLowerCase());
        Instant now = Instant.now();

        String finalUsername = username;
        LoginAttempt attempt = this.loginAttemptRepository.findByUsernameAndIp(username, ip).orElseGet(() -> {
            LoginAttempt x = new LoginAttempt();
            x.setUsername(finalUsername);
            x.setIp(ip);
            x.setFails(0);
            x.setLockedUntil(null);
            x.setLastAttempt(now);
            return x;
        });

        attempt.setLastAttempt(now);

        if (attempt.getLockedUntil() != null && !attempt.getLockedUntil().isAfter(now)) {
            attempt.setFails(0);
            attempt.setLockedUntil(null);
        }

        int fails = attempt.getFails() + 1;
        attempt.setFails(fails);

        if (fails >= MAX_FAILS) {
            attempt.setLockedUntil(now.plus(LOCK_DURATION));
        }

        this.loginAttemptRepository.save(attempt);
    }

    @Transactional
    public void cleanupOld() {
        this.loginAttemptRepository.deleteByLastAttemptBefore(Instant.now().minus(GC_AFTER));
    }
}

