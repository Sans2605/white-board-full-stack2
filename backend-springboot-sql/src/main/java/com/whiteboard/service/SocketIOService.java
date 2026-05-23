package com.whiteboard.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.whiteboard.model.Canvas;
import com.whiteboard.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SocketIOService {

    private static final Logger log = LoggerFactory.getLogger(SocketIOService.class);

    private final SocketIOServer server;
    private final JwtUtil jwtUtil;
    private final CanvasService canvasService;

    private final ConcurrentHashMap<Long, List<Object>> canvasCache = new ConcurrentHashMap<>();

    public SocketIOService(SocketIOServer server, JwtUtil jwtUtil, CanvasService canvasService) {
        this.server = server;
        this.jwtUtil = jwtUtil;
        this.canvasService = canvasService;
    }

    @PostConstruct
    public void start() {
        server.addConnectListener(onConnect());
        server.addDisconnectListener(onDisconnect());
        server.addEventListener("joinCanvas", Map.class, onJoinCanvas());
        server.addEventListener("drawingUpdate", Map.class, onDrawingUpdate());
        server.start();
        log.info("Socket.IO server started on port {}", server.getConfiguration().getPort());
    }

    @PreDestroy
    public void stop() {
        server.stop();
    }

    private ConnectListener onConnect() {
        return client -> log.info("Client connected: {}", client.getSessionId());
    }

    private DisconnectListener onDisconnect() {
        return client -> log.info("Client disconnected: {}", client.getSessionId());
    }

    @SuppressWarnings("unchecked")
    private DataListener<Map> onJoinCanvas() {
        return (client, data, ackRequest) -> {
            String canvasIdStr = String.valueOf(data.get("canvasId"));
            log.info("joinCanvas: {}", canvasIdStr);

            Long userId = extractUserId(client);
            if (userId == null) {
                sendUnauthorized(client, "Access Denied: No Token");
                return;
            }

            Long canvasId;
            try { canvasId = Long.parseLong(canvasIdStr); }
            catch (NumberFormatException e) {
                sendUnauthorized(client, "Invalid canvas ID");
                return;
            }

            Canvas canvas = canvasService.findById(canvasId);
            if (canvas == null) {
                sendUnauthorized(client, "Canvas not found");
                return;
            }

            if (!canvasService.isAuthorized(canvas, userId)) {
                sendUnauthorized(client, "You are not authorized to join this canvas.");
                return;
            }

            client.joinRoom(canvasIdStr);
            log.info("User {} joined canvas {}", client.getSessionId(), canvasIdStr);

            List<Object> elements = canvasCache.getOrDefault(canvasId, canvasService.getElements(canvas));
            client.sendEvent("loadCanvas", elements);
        };
    }

    @SuppressWarnings("unchecked")
    private DataListener<Map> onDrawingUpdate() {
        return (client, data, ackRequest) -> {
            String canvasIdStr = String.valueOf(data.get("canvasId"));
            List<Object> elements = (List<Object>) data.get("elements");

            if (canvasIdStr == null || elements == null) return;

            Long canvasId;
            try { canvasId = Long.parseLong(canvasIdStr); }
            catch (NumberFormatException e) { return; }

            canvasCache.put(canvasId, elements);
            client.getNamespace().getRoomOperations(canvasIdStr).sendEvent("receiveDrawingUpdate", client, elements);
            canvasService.saveElements(canvasId, elements);
        };
    }

    private Long extractUserId(SocketIOClient client) {
        String authHeader = client.getHandshakeData().getHttpHeaders().get("authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return null;
        try {
            return Long.parseLong(jwtUtil.extractUserId(token));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void sendUnauthorized(SocketIOClient client, String message) {
        new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            client.sendEvent("unauthorized", Map.of("message", message));
        }).start();
    }
}
