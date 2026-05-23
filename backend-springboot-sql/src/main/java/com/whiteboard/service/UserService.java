package com.whiteboard.service;

import com.whiteboard.model.User;
import com.whiteboard.repository.UserRepository;
import com.whiteboard.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, String> register(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("All fields are required");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        return Map.of("message", "User registered successfully!");
    }

    public Map<String, String> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Token carries the numeric SQL id as a String — same shape as before
        String token = jwtUtil.generateToken(String.valueOf(user.getId()));
        return Map.of("message", "Login successful", "token", token);
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
