package com.buildermaster.projecttracker.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {
    private final String SECRET= "8nwaXw9omG/Sr+VR/BjWOljpol084OKd/hwZoora3zmbTrHWvpOpzWJ6JV6pZg4b";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());

    public Optional<String> getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return Optional.ofNullable(auth.getName());
        }
        return Optional.empty();
    }

    public String generateToken(String username) {
        // 15 minutes
        long EXPIRATION_TIME = 1000 * 60 * 15;
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

    }

    public String extractUsernameFromToken(String token) {
        return extraClaims(token).getSubject();
    }

    private Claims extraClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String username, UserDetails userDetails, String token) {

        // check if username is same as username in userDetails and if the token that we have is not expired
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extraClaims(token).getExpiration().before(new Date());
    }
}

