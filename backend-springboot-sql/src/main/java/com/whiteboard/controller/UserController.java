package com.whiteboard.controller;

import com.whiteboard.model.User;
import com.whiteboard.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.status(201).body(
                userService.register(body.get("email"), body.get("password"))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Registration failed"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(
                userService.login(body.get("email"), body.get("password"))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Login failed"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUser() {
        try {
            Long userId = Long.parseLong(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
            );
            User user = userService.getUser(userId);
            user.setPassword(null); // never expose password
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
    }
}
