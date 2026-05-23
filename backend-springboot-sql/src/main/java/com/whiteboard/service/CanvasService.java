package com.whiteboard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whiteboard.dto.CanvasDTO;
import com.whiteboard.model.Canvas;
import com.whiteboard.model.CanvasShare;
import com.whiteboard.model.User;
import com.whiteboard.repository.CanvasRepository;
import com.whiteboard.repository.CanvasShareRepository;
import com.whiteboard.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CanvasService {

    private final CanvasRepository canvasRepository;
    private final CanvasShareRepository canvasShareRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public CanvasService(CanvasRepository canvasRepository,
                         CanvasShareRepository canvasShareRepository,
                         UserRepository userRepository,
                         ObjectMapper objectMapper) {
        this.canvasRepository = canvasRepository;
        this.canvasShareRepository = canvasShareRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Canvas requireCanvas(Long canvasId) {
        return canvasRepository.findById(canvasId)
                .orElseThrow(() -> new IllegalArgumentException("Canvas not found"));
    }

    private boolean isShared(Canvas canvas, Long userId) {
        return canvasShareRepository.findByCanvas(canvas).stream()
                .anyMatch(cs -> cs.getUser().getId().equals(userId));
    }

    private List<Object> parseElements(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String serializeElements(List<Object> elements) {
        try {
            return objectMapper.writeValueAsString(elements);
        } catch (Exception e) {
            return "[]";
        }
    }

    /** Convert entity → DTO (mimics old Mongo document shape for the frontend). */
    private CanvasDTO toDTO(Canvas canvas) {
        List<CanvasShare> shares = canvasShareRepository.findByCanvas(canvas);

        CanvasDTO dto = new CanvasDTO();
        dto.setId(String.valueOf(canvas.getId()));
        dto.setOwner(String.valueOf(canvas.getOwner().getId()));
        dto.setShared(shares.stream()
                .map(cs -> String.valueOf(cs.getUser().getId()))
                .collect(Collectors.toList()));
        dto.setElements(parseElements(canvas.getElementsJson()));
        dto.setCreatedAt(canvas.getCreatedAt());
        return dto;
    }

    // ── Public API ───────────────────────────────────────────────────────

    public Map<String, String> createCanvas(Long userId) {
        User owner = requireUser(userId);
        Canvas canvas = new Canvas();
        canvas.setOwner(owner);
        canvasRepository.save(canvas);
        return Map.of("message", "Canvas created successfully", "canvasId", String.valueOf(canvas.getId()));
    }

    public CanvasDTO loadCanvas(Long canvasId, Long userId) {
        Canvas canvas = requireCanvas(canvasId);
        boolean isOwner = canvas.getOwner().getId().equals(userId);
        if (!isOwner && !isShared(canvas, userId)) {
            throw new SecurityException("Unauthorized to access this canvas");
        }
        return toDTO(canvas);
    }

    @Transactional
    public Map<String, String> updateCanvas(Long canvasId, Long userId, List<Object> elements) {
        Canvas canvas = requireCanvas(canvasId);
        boolean isOwner = canvas.getOwner().getId().equals(userId);
        if (!isOwner && !isShared(canvas, userId)) {
            throw new SecurityException("Unauthorized to update this canvas");
        }
        canvas.setElementsJson(serializeElements(elements));
        canvasRepository.save(canvas);
        return Map.of("message", "Canvas updated successfully");
    }

    @Transactional
    public Map<String, String> shareCanvas(Long canvasId, Long userId, String email) {
        User targetUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User with this email not found"));
        Canvas canvas = requireCanvas(canvasId);

        if (!canvas.getOwner().getId().equals(userId)) {
            throw new SecurityException("Only the owner can share this canvas");
        }
        if (canvas.getOwner().getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("Owner cannot be added to shared list");
        }
        if (canvasShareRepository.existsByCanvasAndUser(canvas, targetUser)) {
            throw new IllegalArgumentException("Already shared with user");
        }

        canvasShareRepository.save(new CanvasShare(canvas, targetUser));
        return Map.of("message", "Canvas shared successfully");
    }

    @Transactional
    public Map<String, String> unshareCanvas(Long canvasId, Long userId, String userIdToRemoveStr) {
        Canvas canvas = requireCanvas(canvasId);
        if (!canvas.getOwner().getId().equals(userId)) {
            throw new SecurityException("Only the owner can unshare this canvas");
        }
        Long userIdToRemove = Long.parseLong(userIdToRemoveStr);
        User userToRemove = requireUser(userIdToRemove);
        canvasShareRepository.deleteByCanvasAndUser(canvas, userToRemove);
        return Map.of("message", "Canvas unshared successfully");
    }

    @Transactional
    public Map<String, String> deleteCanvas(Long canvasId, Long userId) {
        Canvas canvas = requireCanvas(canvasId);
        if (!canvas.getOwner().getId().equals(userId)) {
            throw new SecurityException("Only the owner can delete this canvas");
        }
        canvasShareRepository.findByCanvas(canvas).forEach(canvasShareRepository::delete);
        canvasRepository.delete(canvas);
        return Map.of("message", "Canvas deleted successfully");
    }

    public List<CanvasDTO> getUserCanvases(Long userId) {
        return canvasRepository.findAllForUser(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Called from SocketIOService — JWT already verified at socket level
    @Transactional
    public void saveElements(Long canvasId, List<Object> elements) {
        canvasRepository.findById(canvasId).ifPresent(canvas -> {
            canvas.setElementsJson(serializeElements(elements));
            canvasRepository.save(canvas);
        });
    }

    public Canvas findById(Long canvasId) {
        return canvasRepository.findById(canvasId).orElse(null);
    }

    public boolean isAuthorized(Canvas canvas, Long userId) {
        if (canvas.getOwner().getId().equals(userId)) return true;
        return isShared(canvas, userId);
    }

    public List<Object> getElements(Canvas canvas) {
        return parseElements(canvas.getElementsJson());
    }
}
