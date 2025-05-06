package com.example.lostoria.service;

import com.example.lostoria.dto.UserPrincipal;
import com.example.lostoria.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

@Service
public class JWTService {
    @Autowired
    private UserRepository userRepository;
    private String secretKey = "";
//    private final long expirationTime = 86400000;

    public JWTService() {

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk =keyGen.generateKey();
            secretKey = Encoders.BASE64URL.encode(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(String identifier) {
        Map<String, Object> claims = new HashMap<>();
        String iden = identifier;
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(identifier)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .and()
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey(){
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token){
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserPrincipal userPrincipal) {
        final String userName = extractUserName(token);
//        Optional<User> user1 = userRepository.findByUsername(userName);
//        if (!user1.isPresent()) {
//            user1 = userRepository.findByEmail(userName);
//            return (userName.equals(userPrincipal.getEmail()) && !isTokenExpired(token));
//        }
        return ((userName.equals(userPrincipal.getUsername()) || userName.equals(userPrincipal.getEmail())) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}