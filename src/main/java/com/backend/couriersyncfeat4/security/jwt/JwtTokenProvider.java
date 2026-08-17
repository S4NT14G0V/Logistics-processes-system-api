package com.backend.couriersyncfeat4.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret.code}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-ttl}")
    private long accessTokenTtlInMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(String email, String role, List<String> permissions) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenTtlInMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("permissions", permissions)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Función genérica para obtener cualquier claim del token
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("role", String.class));
    }

    public List<String> getPermissionsFromToken(String token) {
        Object permissions = getClaimFromToken(token, claims -> claims.get("permissions"));
        if (permissions instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Puedes loggear el error si lo necesitas
            // log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}