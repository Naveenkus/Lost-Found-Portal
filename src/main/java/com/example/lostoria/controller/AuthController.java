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

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    //---------COMMON USER---------//
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user){
        return new ResponseEntity<>(userService.registerUser(user), HttpStatus.OK);
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginRequest){

//        Optional<User> user = userService.findByUsername(loginRequest.getUsername());
//
//        if (!user.isPresent())
//            user = userService.findByEmail(loginRequest.getEmail());
//
//        if (user.isPresent()) {
//            return new ResponseEntity<>("login success", HttpStatus.OK);
//        }else
//            return new ResponseEntity<>("Invalid Username", HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(userService.verify(loginRequest), HttpStatus.OK);

//        String token = userService.verify(loginRequest);
//        if (!"fail".equals(token)) {
//            return new ResponseEntity<>(token, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
//        }
    }

}
