package com.danil.appliances.security.jwt;

import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.model.RefreshToken;
import com.danil.appliances.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public void store(String userEmail, String refreshJwt) {
        String jti = this.jwtService.extractJti(refreshJwt);
            Instant exp = this.jwtService.parse(refreshJwt).getPayload().getExpiration().toInstant();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserEmail(userEmail);
        refreshToken.setJtiHash(sha256Hex(jti));
        refreshToken.setExpiresAt(exp);

        this.refreshTokenRepository.save(refreshToken);
    }

    public TokenPair rotate(String refreshJwt, UserDetails userDetails) {
        if (!jwtService.isValid(refreshJwt, JwtType.REFRESH)) {
            throw new BusinessException("Invalid refresh token");
        }

        String jti = this.jwtService.extractJti(refreshJwt);
        String jtiHash = sha256Hex(jti);

        RefreshToken stored = this.refreshTokenRepository.findByJtiHash(jtiHash)
                .orElseThrow(() -> new BusinessException("Refresh token not recognized"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token expired or revoked");
        }

        this.refreshTokenRepository.delete(stored);

        String newAccess = this.jwtService.generateAccessToken(userDetails);
        String newRefresh = this.jwtService.generateRefreshToken(userDetails);
        store(userDetails.getUsername(), newRefresh);

        return new TokenPair(newAccess, newRefresh);
    }

    public void revokeAll(String userEmail) {
        this.refreshTokenRepository.deleteByUserEmail(userEmail);
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}
