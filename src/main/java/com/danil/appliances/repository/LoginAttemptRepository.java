package com.danil.appliances.repository;

import com.danil.appliances.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    Optional<LoginAttempt> findByUsernameAndIp(String username, String ip);

    void deleteByLastAttemptBefore(Instant cutoff);
}
