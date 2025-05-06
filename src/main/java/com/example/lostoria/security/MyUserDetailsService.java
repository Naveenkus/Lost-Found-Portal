package com.example.lostoria.security;

import com.example.lostoria.model.User;
import com.example.lostoria.dto.UserPrincipal;
import com.example.lostoria.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(identifier);
        if (!user.isPresent()){
            user = userRepository.findByEmail(identifier);
            if (!user.isPresent()){
                throw new UsernameNotFoundException("User not found with username or email: " + identifier);
            }
        }
        User user1 = user.get();
        return new UserPrincipal(user1);
    }
}
