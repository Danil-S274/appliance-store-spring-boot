package com.danil.appliances.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties props;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDetails user) {
        return buildToken(
                user,
                JwtType.ACCESS,
                this.props.getAccessTtl()
        );
    }

    public String generateRefreshToken(UserDetails user) {
        return buildRefreshToken(user);
    }

    private String buildToken(UserDetails user, JwtType type, java.time.Duration ttl) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getUsername())
                .claim(
                        JwtClaims.ROLES,
                        user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                )
                .claim(JwtClaims.TYPE, type.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(this.secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private String buildRefreshToken(UserDetails user) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(user.getUsername())
                .claim(
                        JwtClaims.ROLES,
                        user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                )
                .claim(JwtClaims.TYPE, JwtType.REFRESH.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.getRefreshTtl())))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(this.secretKey)
                .build()
                .parseSignedClaims(token);
    }

    public boolean isValid(String token, JwtType expectedType) {
        try {
            Claims c = parse(token).getPayload();
            if (!expectedType.name().equals(c.get(JwtClaims.TYPE))) return false;
            if (c.getSubject() == null || c.getSubject().isBlank()) return false;
            return c.getExpiration() != null && c.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parse(token).getPayload().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) parse(token)
                .getPayload()
                .get(JwtClaims.ROLES);
    }

    public String extractJti(String token) {
        return parse(token).getPayload().getId();
    }

    public Authentication toAuthentication(String token) {
        Claims claims = parse(token).getPayload();

        var authorities = extractRoles(token).stream()
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .toList();

        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                claims.getSubject(),
                null,
                authorities
        );
    }
}

