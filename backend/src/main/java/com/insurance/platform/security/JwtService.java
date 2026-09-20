package com.insurance.platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates and validates JWTs using jjwt 0.12 APIs.
 * Claims carried: userId, email, role.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret:change-me-to-a-very-long-secret-key-for-dev-only-0123456789}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationMs;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT for the given user.
     *
     * @param userId user id claim
     * @param email  email claim (also subject)
     * @param role   role claim
     * @return compact JWT
     */
    public String generateToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey())
                .compact();
    }

    /** Generates a token from Spring Security user details. */
    public String generateToken(UserDetails userDetails) {
        Long userId = null;
        String role = null;
        if (userDetails instanceof CustomUserDetailsService.CustomUserPrincipal principal) {
            userId = principal.getUserId();
            role = principal.getRole();
        }
        return generateToken(userId, userDetails.getUsername(), role);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extracts the subject (email) from a token. */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /** Extracts the userId claim. */
    public Long extractUserId(String token) {
        Object value = parseClaims(token).get("userId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value != null ? Long.valueOf(value.toString()) : null;
    }

    /** Extracts the role claim. */
    public String extractRole(String token) {
        Object value = parseClaims(token).get("role");
        return value != null ? value.toString() : null;
    }

    /** Returns true when the token is well-formed, signed, unexpired and matches the user. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parseClaims(token);
            boolean expired = claims.getExpiration() != null && claims.getExpiration().before(new Date());
            boolean subjectMatches = claims.getSubject() != null
                    && claims.getSubject().equals(userDetails.getUsername());
            return !expired && subjectMatches;
        } catch (Exception ex) {
            log.debug("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }
}
