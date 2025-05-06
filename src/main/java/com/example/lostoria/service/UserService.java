package com.example.lostoria.service;

import com.example.lostoria.model.User;
import com.example.lostoria.repository.UserRepository;
import com.example.lostoria.util.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTService jwtService;
    @Autowired
    AuthenticationManager authManager;

    private  PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateUser(long id, User updated) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(updated.getUsername());
            user.setEmail(updated.getEmail());
            user.setRole(updated.getRole());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    public String verify(User user){
        String identifier = user.getUsername();
        Optional<User> user1 = userRepository.findByUsername(identifier);
        if (!user1.isPresent()) {
            user1 = userRepository.findByEmail(identifier);
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
            if (authentication.isAuthenticated()){
                return jwtService.generateToken(user.getEmail());
            }
        }else {
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            if (authentication.isAuthenticated()){
                return jwtService.generateToken(user.getUsername());
            }
        }
//        if (user1.isPresent()){
//            User existingUser = user1.get();
//            if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
//                return jwtService.generateToken(existingUser.getUsername());
//            }
//        }
//        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
//        if (authentication.isAuthenticated()){
//            return jwtService.generateToken(user.getUsername());
//        }

        return "fail";
    }
}
