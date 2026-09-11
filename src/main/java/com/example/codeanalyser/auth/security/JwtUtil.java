package com.example.codeanalyser.auth.security;

import java.util.Date;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
     @Value("${security.jwt.secret}")
    private String SECRET_KEY; // keep it secure!

    public String generateToken(String username) {
         var signingKey = signingKey();
         return Jwts.builder()
                   .setSubject(username)
                   .setIssuedAt(new Date(System.currentTimeMillis()))
                   .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                   .signWith(signingKey, SignatureAlgorithm.HS256)
                   .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(signingKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                              .setSigningKey(signingKey())
                              .build()
                              .parseClaimsJws(token)
                              .getBody()
                              .getExpiration();
        return expiration.before(new Date());
    }

    private javax.crypto.SecretKey signingKey() {
        byte[] secret = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 32 bytes. Set JWT_SECRET to a long random value.");
        }
        return Keys.hmacShaKeyFor(secret);
    }
}
