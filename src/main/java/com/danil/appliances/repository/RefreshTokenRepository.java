package com.danil.appliances.repository;

import com.danil.appliances.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJtiHash(String jtiHash);

    void deleteByUserEmail(String userEmail);
}
