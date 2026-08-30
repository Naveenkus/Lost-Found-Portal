package com.example.lostoria.controller;

import com.example.lostoria.model.User;
import com.example.lostoria.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    //---------COMMON USER---------//
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
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
}
