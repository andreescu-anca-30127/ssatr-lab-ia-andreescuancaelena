package com.ssatr.Project.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final Key key;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, long minutes) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + minutes * 60_000);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .addClaims(Map.of("type", "ACCESS"))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateQrToken(String surveyId, long minutes) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + minutes * 60_000);

        return Jwts.builder()
                .setSubject("QR_SESSION")
                .setIssuedAt(now)
                .setExpiration(exp)
                .addClaims(Map.of("type", "QR", "surveyId", surveyId))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
