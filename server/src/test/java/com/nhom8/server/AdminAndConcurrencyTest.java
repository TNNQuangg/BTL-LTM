package com.nhom8.server;

import com.nhom8.common.message.Envelope;
import com.nhom8.common.message.MessageType;
import com.nhom8.server.repository.CustomTopicRepository;
import com.nhom8.server.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminAndConcurrencyTest {
    @LocalServerPort int port;
    @Autowired CustomTopicRepository topics;
    @Autowired MatchRepository matches;

    static class Client extends TextWebSocketHandler implements AutoCloseable {
        WebSocketSession session;
        LinkedBlockingQueue<Envelope> inbox = new LinkedBlockingQueue<>();
        void connect(int port) throws Exception {
            new StandardWebSocketClient().execute(this, "ws://localhost:" + port + "/ws/game")
                    .get(5, TimeUnit.SECONDS);
        }
        @Override public void afterConnectionEstablished(WebSocketSession s) { session = s; }
        @Override protected void handleTextMessage(WebSocketSession s, TextMessage m) {
            inbox.add(Envelope.fromJson(m.getPayload()));
        }
        void send(MessageType type, String key, Object value) throws Exception {
            Envelope msg = new Envelope(type);
            if (key != null) msg.put(key, value);
            session.sendMessage(new TextMessage(msg.toJson()));
        }
        Envelope await(MessageType type) throws Exception {
            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
            while (System.nanoTime() < end) {
                Envelope next = inbox.poll(300, TimeUnit.MILLISECONDS);
                if (next != null && next.getType() == type) return next;
            }
            fail("Không nhận được " + type);
            return null;
        }
        void authenticate(String username) throws Exception {
            session.sendMessage(new TextMessage(new Envelope(MessageType.LOGIN)
                    .put("username", username).put("password", "123456").toJson()));
            assertTrue(await(MessageType.LOGIN_RESULT).isSuccess());
        }
        @Override public void close() throws Exception { if (session != null) session.close(); }
    }

    @Test void adminPermissionsTopicsRoomsAndSingleGameStart() throws Exception {
        try (Client admin = new Client(); Client p1 = new Client(); Client p2 = new Client()) {
            admin.connect(port); p1.connect(port); p2.connect(port);
            admin.authenticate("admin"); p1.authenticate("player1"); p2.authenticate("player2");

            p1.send(MessageType.ADMIN_STATS, null, null);
            assertFalse(p1.await(MessageType.ADMIN_STATS).isSuccess());
            admin.send(MessageType.ADMIN_STATS, null, null);
            assertTrue(admin.await(MessageType.ADMIN_STATS).getData().path("totalUsers").asInt() >= 3);

            String word = "kiểm thử quản trị";
            admin.send(MessageType.ADMIN_TOPIC_ADD, "word", word);
            assertTrue(admin.await(MessageType.ADMIN_TOPIC_ADD).isSuccess());
            assertTrue(topics.existsByWordIgnoreCase(word));
            p1.send(MessageType.ADMIN_TOPIC_DELETE, "word", word);
            assertFalse(p1.await(MessageType.ADMIN_TOPIC_DELETE).isSuccess());
            assertTrue(topics.existsByWordIgnoreCase(word));
            admin.send(MessageType.ADMIN_TOPIC_DELETE, "word", word);
            assertTrue(admin.await(MessageType.ADMIN_TOPIC_DELETE).isSuccess());
            assertFalse(topics.existsByWordIgnoreCase(word));

            p1.send(MessageType.ROOM_CREATE, "roomName", "Kiểm thử");
            String roomId = p1.await(MessageType.ROOM_RESPONSE).getString("roomId");
            p2.send(MessageType.ROOM_JOIN, "roomId", roomId);
            assertTrue(p2.await(MessageType.ROOM_RESPONSE).isSuccess());
            long before = matches.count();
            p1.send(MessageType.READY, "isReady", true);
            p2.send(MessageType.READY, "isReady", true);
            p1.send(MessageType.READY, "isReady", true);
            p2.send(MessageType.READY, "isReady", true);
            p1.await(MessageType.GAME_START);
            Thread.sleep(400);
            assertEquals(before + 1, matches.count());

            String topic1 = p1.await(MessageType.TOPIC_OPTIONS).getData().path("topics").get(0).asText();
            String topic2 = p2.await(MessageType.TOPIC_OPTIONS).getData().path("topics").get(0).asText();
            p1.send(MessageType.TOPIC_SELECT, "topic", topic1);
            p2.send(MessageType.TOPIC_SELECT, "topic", topic2);
            p1.await(MessageType.DRAW_START);
            p2.await(MessageType.DRAW_START);
            java.util.Map<String, Object> eraser = java.util.Map.of(
                    "tool", "ERASER", "color", "#FFFFFF", "width", 20,
                    "xPoints", java.util.List.of(10, 20), "yPoints", java.util.List.of(10, 20));
            p1.send(MessageType.DRAW_SUBMIT, "drawData", java.util.List.of(eraser));
            p2.send(MessageType.DRAW_SUBMIT, "drawData", java.util.List.of(eraser));
            Envelope painting = p1.await(MessageType.PAINTING_DISPLAY);
            assertEquals("ERASER", painting.getData().path("drawData").get(0).path("tool").asText());

            admin.send(MessageType.ADMIN_DISSOLVE_ROOM, "roomId", roomId);
            assertTrue(admin.await(MessageType.ADMIN_DISSOLVE_ROOM).isSuccess());
            assertEquals(MessageType.ROOM_DISSOLVED, p1.await(MessageType.ROOM_DISSOLVED).getType());
        }
    }
}
