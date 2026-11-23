package com.tradexchange.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SocketIoWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SocketIoWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> symbolSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    public SocketIoWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        sessionSubscriptions.put(sessionId, ConcurrentHashMap.newKeySet());

        log.info("Client connected: {}", sessionId);

        String handshake = String.format("0{\"sid\":\"%s\",\"upgrades\":[],\"pingInterval\":25000,\"pingTimeout\":20000,\"maxPayload\":1000000}", sessionId);
        session.sendMessage(new TextMessage(handshake));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String sessionId = session.getId();

        if (payload == null || payload.isEmpty()) return;

        if ("2".equals(payload)) {
            session.sendMessage(new TextMessage("3"));
            return;
        }

        if (payload.startsWith("40")) {
            session.sendMessage(new TextMessage("40{\"sid\":\"" + sessionId + "\"}"));
            return;
        }

        if (payload.startsWith("42")) {
            try {
                String jsonPart = payload.substring(2);
                JsonNode arrayNode = objectMapper.readTree(jsonPart);

                if (arrayNode.isArray() && arrayNode.size() >= 1) {
                    String eventName = arrayNode.get(0).asText();
                    JsonNode dataNode = arrayNode.size() > 1 ? arrayNode.get(1) : null;

                    handleSocketEvent(session, eventName, dataNode);
                }
            } catch (Exception e) {
                log.warn("Error parsing Socket.IO message from {}: {}", sessionId, e.getMessage());
            }
        }
    }

    private void handleSocketEvent(WebSocketSession session, String eventName, JsonNode dataNode) {
        String sessionId = session.getId();

        if ("subscribe".equalsIgnoreCase(eventName)) {
            String symbol = extractSymbol(dataNode);
            if (symbol != null) {
                String sym = symbol.toUpperCase().trim();
                symbolSubscriptions.computeIfAbsent(sym, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
                sessionSubscriptions.getOrDefault(sessionId, Collections.emptySet()).add(sym);
                log.info("{} subscribed to {}", sessionId, sym);
            }
        } else if ("unsubscribe".equalsIgnoreCase(eventName)) {
            String symbol = extractSymbol(dataNode);
            if (symbol != null) {
                String sym = symbol.toUpperCase().trim();
                Set<String> subs = symbolSubscriptions.get(sym);
                if (subs != null) {
                    subs.remove(sessionId);
                }
                Set<String> userSubs = sessionSubscriptions.get(sessionId);
                if (userSubs != null) {
                    userSubs.remove(sym);
                }
                log.info("{} unsubscribed from {}", sessionId, sym);
            }
        } else if ("joinUser".equalsIgnoreCase(eventName)) {
            String userId = dataNode != null ? dataNode.asText() : null;
            if (userId != null && !userId.isEmpty()) {
                symbolSubscriptions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
                sessionSubscriptions.getOrDefault(sessionId, Collections.emptySet()).add(userId);
                log.info("{} joined user room: {}", sessionId, userId);
            }
        }
    }

    private String extractSymbol(JsonNode dataNode) {
        if (dataNode == null) return null;
        if (dataNode.isTextual()) {
            return dataNode.asText();
        }
        if (dataNode.isObject() && dataNode.has("symbol")) {
            return dataNode.get("symbol").asText();
        }
        return null;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        log.info("Client disconnected: {}", sessionId);

        sessions.remove(sessionId);
        Set<String> subs = sessionSubscriptions.remove(sessionId);
        if (subs != null) {
            for (String sym : subs) {
                Set<String> set = symbolSubscriptions.get(sym);
                if (set != null) {
                    set.remove(sessionId);
                }
            }
        }
    }

    public void broadcast(String roomOrSymbol, String eventName, Object eventData) {
        if (roomOrSymbol == null) return;
        String room = roomOrSymbol.toUpperCase().trim();

        Set<String> sessionIds = symbolSubscriptions.get(room);
        if (sessionIds == null || sessionIds.isEmpty()) return;

        try {
            String dataJson = objectMapper.writeValueAsString(eventData);
            String frame = String.format("42[\"%s\",%s]", eventName, dataJson);
            TextMessage textMessage = new TextMessage(frame);

            for (String sid : sessionIds) {
                WebSocketSession session = sessions.get(sid);
                if (session != null && session.isOpen()) {
                    synchronized (session) {
                        try {
                            session.sendMessage(textMessage);
                        } catch (IOException e) {
                            log.warn("Failed to send WebSocket message to {}: {}", sid, e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Broadcast serialization error for {}: {}", eventName, e.getMessage());
        }
    }
}
