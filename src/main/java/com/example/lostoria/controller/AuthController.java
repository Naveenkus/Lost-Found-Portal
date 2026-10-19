package com.example.lostoria.controller;

import com.example.lostoria.model.User;
import com.example.lostoria.service.JWTService;
import com.example.lostoria.service.UserService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JWTService jwtService;

    //---------COMMON USER---------//
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user){
        User createdUser = userService.registerUser(user);
        createdUser.setPassword(null); // Never leak password in response
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest){
        String token = userService.verify(loginRequest);
        if (!"fail".equals(token) && token != null) {
            String identifier = loginRequest.getUsername() != null && !loginRequest.getUsername().trim().isEmpty()
                    ? loginRequest.getUsername().trim()
                    : (loginRequest.getEmail() != null ? loginRequest.getEmail().trim() : "");
            User authenticatedUser = userService.findByUsername(identifier)
                    .or(() -> userService.findByEmail(identifier))
                    .orElse(null);

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("token", token);
            response.put("message", "Login successful");
            if (authenticatedUser != null) {
                response.put("user", authenticatedUser);
            }
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "message", "Invalid username/email or password"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout (HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer") && stringRedisTemplate != null) {
            String token = authHeader.substring(7);
            long remaining  = jwtService.getRemainingExpirationMillis(token);
            if (remaining > 0) {
                stringRedisTemplate.opsForValue().set("blacklist:" + token, "revoked", Duration.ofMillis(remaining));
            }
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
