package com.meli.inventorymanagement.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForInventoryManagementSystemThatIsLongEnough}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username) {
        log.debug("Generating JWT token for user: {}", username);

        try {
            Map<String, Object> claims = new HashMap<>();
            String token = createToken(claims, username);

            log.info("JWT token generated successfully for user: {} - Token expires in {} ms",
                    username, expiration);

            return token;
        } catch (Exception e) {
            log.error("Failed to generate JWT token for user: {} - Error: {}", username, e.getMessage(), e);
            throw new RuntimeException("Token generation failed", e);
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        log.debug("Creating JWT token - Subject: {} - Issued at: {} - Expires at: {}",
                subject, now, expirationDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, String username) {
        log.debug("Validating JWT token for user: {}", username);

        try {
            final String extractedUsername = extractUsername(token);
            boolean isValid = (extractedUsername.equals(username) && !isTokenExpired(token));

            if (isValid) {
                log.debug("JWT token validation successful for user: {}", username);
            } else {
                log.warn("JWT token validation failed for user: {} - Username mismatch or token expired", username);
            }

            return isValid;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired for user: {} - Expiration: {}", username, e.getClaims().getExpiration());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token for user: {} - Error: {}", username, e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature for user: {} - Error: {}", username, e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token for user: {} - Error: {}", username, e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token claims string is empty for user: {} - Error: {}", username, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during JWT token validation for user: {} - Error: {}",
                     username, e.getMessage(), e);
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            log.debug("Extracted username from JWT token: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Failed to extract username from JWT token - Error: {}", e.getMessage());
            throw e;
        }
    }

    public Date extractExpiration(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            log.debug("Extracted expiration from JWT token: {}", expiration);
            return expiration;
        } catch (Exception e) {
            log.error("Failed to extract expiration from JWT token - Error: {}", e.getMessage());
            throw e;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.debug("Failed to parse JWT claims - Error: {}", e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            boolean expired = expiration.before(new Date());

            if (expired) {
                log.debug("JWT token is expired - Expiration: {} - Current time: {}",
                         expiration, new Date());
            }

            return expired;
        } catch (Exception e) {
            log.warn("Error checking token expiration - Error: {}", e.getMessage());
            return true; // Consider token expired if we can't check
        }
    }
}
