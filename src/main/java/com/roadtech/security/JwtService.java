package com.roadtech.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service // Marks this class as a Spring-managed service component
public class JwtService {

    // Secret key used to sign and verify JWTs (Base64 encoded)
    @Value("${jwt.secret}")
    private String secretKey;

    // Expiration time for access tokens (in milliseconds)
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // Expiration time for refresh tokens (in milliseconds)
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // Extracts the username (subject) from the JWT
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Generic method to extract any claim using a resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // Parse all claims from token
        return claimsResolver.apply(claims);           // Apply resolver to get required claim
    }

    // Generates an access token without extra claims
    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    // Generates an access token with additional custom claims
    public String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, accessTokenExpiration);
    }

    // Generates a refresh token (longer validity, no extra claims)
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshTokenExpiration);
    }

    // Returns configured access token expiration duration
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    // Returns configured refresh token expiration duration
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    // Builds and signs a JWT with claims, subject, issue time, and expiration
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts.builder()
                .claims(extraClaims)                               // Add custom claims
                .subject(userDetails.getUsername())                // Set username as subject
                .issuedAt(new Date(System.currentTimeMillis()))    // Token creation time
                .expiration(new Date(System.currentTimeMillis() + expiration)) // Expiry time
                .signWith(getSignInKey())                           // Sign token with secret key
                .compact();                                        // Build final JWT string
    }

    // Validates token by checking username match and expiration
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Checks whether the token has expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extracts expiration date from JWT
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Parses and returns all claims after verifying the token signature
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())   // Verify JWT signature using secret key
                .build()
                .parseSignedClaims(token)    // Parse signed JWT
                .getPayload();               // Extract claims payload
    }

    // Converts Base64 secret into HMAC SHA key for signing/verifying JWT
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
