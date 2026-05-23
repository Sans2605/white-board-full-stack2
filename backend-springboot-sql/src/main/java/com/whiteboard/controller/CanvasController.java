package com.whiteboard.controller;

import com.whiteboard.service.CanvasService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/canvas")
public class CanvasController {

    private final CanvasService canvasService;

    public CanvasController(CanvasService canvasService) {
        this.canvasService = canvasService;
    }

    private Long currentUserId() {
        return Long.parseLong(
            (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<?> create() {
        try {
            return ResponseEntity.status(201).body(canvasService.createCanvas(currentUserId()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create canvas"));
        }
    }

    @GetMapping("/load/{id}")
    public ResponseEntity<?> load(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(canvasService.loadCanvas(id, currentUserId()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to load canvas"));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody Map<String, Object> body) {
        try {
            Long canvasId = Long.parseLong((String) body.get("canvasId"));
            @SuppressWarnings("unchecked")
            List<Object> elements = (List<Object>) body.get("elements");
            return ResponseEntity.ok(canvasService.updateCanvas(canvasId, currentUserId(), elements));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update canvas"));
        }
    }

    @PutMapping("/share/{id}")
    public ResponseEntity<?> share(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(canvasService.shareCanvas(id, currentUserId(), body.get("email")));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to share canvas"));
        }
    }

    @PutMapping("/unshare/{id}")
    public ResponseEntity<?> unshare(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(canvasService.unshareCanvas(id, currentUserId(), body.get("userIdToRemove")));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to unshare canvas"));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(canvasService.deleteCanvas(id, currentUserId()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete canvas"));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        try {
            return ResponseEntity.ok(canvasService.getUserCanvases(currentUserId()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch canvases"));
        }
    }
}
