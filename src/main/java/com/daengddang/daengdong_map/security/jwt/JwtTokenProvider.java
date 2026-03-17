package com.daengddang.daengdong_map.security.jwt;

import com.daengddang.daengdong_map.domain.user.UserStatus;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final Key key;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(
                properties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(Long userId, UserStatus status) {
        Instant now = Instant.now();
        Instant expiry = now.plus(
                properties.getAccessTokenExpireMinutes(),
                ChronoUnit.MINUTES
        );

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(properties.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("status", status.name())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(
                properties.getRefreshTokenExpireDays(),
                ChronoUnit.DAYS
        );

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(properties.getIssuer())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
