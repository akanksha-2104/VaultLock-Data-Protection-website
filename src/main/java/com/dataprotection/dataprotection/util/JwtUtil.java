package com.dataprotection.dataprotection.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * STEP 1 — JWT UTILITY
 *
 * Place at: src/main/java/com/dataprotection/dataprotection/util/JwtUtil.java
 *
 * Responsibilities:
 *  - Generate a signed JWT token after successful login
 *  - Validate incoming tokens on every protected request
 *  - Extract the user's email from a valid token
 *
 * WHY JWT?
 *  Previously we used HTTP Basic which sent email+password on EVERY request.
 *  That means every API call hits MongoDB to validate credentials — expensive.
 *  JWT is issued ONCE at login, then the server validates the signature
 *  cryptographically (no database hit needed) on every subsequent request.
 */
@Component
public class JwtUtil {

    // Injected from application.properties — must be at least 256 bits (32 chars)
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // Token lifetime in milliseconds — 86400000 = 24 hours
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Build the signing key from the configured secret.
     * HMAC-SHA256 is used — symmetric signing (same key to sign and verify).
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT token for a successfully authenticated user.
     *
     * Token structure (base64-decoded):
     *   Header:  { "alg": "HS256", "typ": "JWT" }
     *   Payload: { "sub": "user@email.com", "role": "USER", "iat": ..., "exp": ... }
     *   Signature: HMAC-SHA256(header.payload, secret)
     *
     * @param email the authenticated user's email (stored as the token subject)
     * @param role  the user's role — "USER" or "ADMIN"
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)                          // custom claim
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract the email (subject) from a valid token.
     * Throws JwtException if token is invalid, expired, or tampered with.
     */
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Extract the role claim from a valid token.
     */
    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    /**
     * Validate a token — returns true only if the signature is valid
     * and the token has not expired.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Covers: expired, malformed, invalid signature, null token
            return false;
        }
    }
}
