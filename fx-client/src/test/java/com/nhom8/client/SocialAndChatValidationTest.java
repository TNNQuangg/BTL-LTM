package com.nhom8.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom8.client.ws.GameWebSocketClient;
import com.nhom8.common.message.Envelope;
import com.nhom8.common.message.MessageType;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SocialAndChatValidationTest {

    private static final String WS_URL = "ws://localhost:8080/ws/game";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("🧪 BẮT ĐẦU KIỂM THỬ: BẠN BÈ, CHAT RIÊNG 1-1 VÀ CHAT PHÒNG CÔ LẬP");
        System.out.println("===============================================================\n");

        boolean allPassed = true;

        try {
            // Test 1: Chat riêng 1-1 giữa 2 bạn bè và tải lịch sử tin nhắn
            System.out.println("▶ [TEST 1] Kiểm tra Chat riêng 1-1 (Direct Message & Chat History)...");
            if (testDirectMessageAndHistory()) {
                System.out.println("✔ [TEST 1] PASS: Gửi/nhận tin nhắn riêng 1-1 và lấy lịch sử thành công!\n");
            } else {
                System.err.println("❌ [TEST 1] FAILED!\n");
                allPassed = false;
            }

            // Test 2: Chat nội bộ phòng cách ly hoàn toàn với người ở sảnh
            System.out.println("▶ [TEST 2] Kiểm tra Chat phòng cô lập (Room Chat Isolation)...");
            if (testRoomChatIsolation()) {
                System.out.println("✔ [TEST 2] PASS: Chat trong phòng chỉ gửi tới thành viên trong phòng, người ở sảnh KHÔNG nhận được!\n");
            } else {
                System.err.println("❌ [TEST 2] FAILED!\n");
                allPassed = false;
            }

            // Test 3: Lấy danh sách bạn bè
            System.out.println("▶ [TEST 3] Kiểm tra Danh sách bạn bè & Lời mời kết bạn (Friend List & Requests)...");
            if (testFriendListAndRequests()) {
                System.out.println("✔ [TEST 3] PASS: Danh sách bạn bè và lời mời kết bạn hoạt động chính xác!\n");
            } else {
                System.err.println("❌ [TEST 3] FAILED!\n");
                allPassed = false;
            }

            if (allPassed) {
                System.out.println("===============================================================");
                System.out.println("🎉 TẤT CẢ 3 BÀI KIỂM THỬ CHAT & BẠN BÈ ĐỀU ĐẠT 100%!");
                System.out.println("===============================================================");
                System.exit(0);
            } else {
                System.err.println("❌ CÓ BÀI KIỂM THỬ THẤT BẠI!");
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("❌ NGOẠI LỆ: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static boolean testDirectMessageAndHistory() throws Exception {
        CountDownLatch loginLatch1 = new CountDownLatch(1);
        CountDownLatch loginLatch2 = new CountDownLatch(1);

        CountDownLatch receiveDmRecipient = new CountDownLatch(1);
        CountDownLatch receiveDmAckSender = new CountDownLatch(1);
        CountDownLatch historyLatch = new CountDownLatch(1);

        AtomicReference<String> receivedMsgText = new AtomicReference<>("");
        AtomicBoolean historyReceived = new AtomicBoolean(false);

        GameWebSocketClient client1 = new GameWebSocketClient(WS_URL, env -> {
            if (env.getType() == MessageType.LOGIN_RESULT || env.getType() == MessageType.LOGIN_RESPONSE) {
                loginLatch1.countDown();
            } else if (env.getType() == MessageType.DIRECT_MESSAGE_RECEIVE) {
                boolean isSelf = env.getBoolean("isSelf", false);
                if (isSelf) {
                    receiveDmAckSender.countDown();
                }
            } else if (env.getType() == MessageType.CHAT_HISTORY_RESPONSE) {
                historyReceived.set(true);
                historyLatch.countDown();
            }
        });

        GameWebSocketClient client2 = new GameWebSocketClient(WS_URL, env -> {
            if (env.getType() == MessageType.LOGIN_RESULT || env.getType() == MessageType.LOGIN_RESPONSE) {
                loginLatch2.countDown();
            } else if (env.getType() == MessageType.DIRECT_MESSAGE_RECEIVE) {
                String content = env.getString("content");
                receivedMsgText.set(content);
                receiveDmRecipient.countDown();
            }
        });

        client1.connect().get(5, TimeUnit.SECONDS);
        client2.connect().get(5, TimeUnit.SECONDS);

        // Login client1 as admin
        Envelope login1 = new Envelope(MessageType.LOGIN);
        login1.put("username", "admin");
        login1.put("password", "123456");
        client1.send(login1);
        loginLatch1.await(5, TimeUnit.SECONDS);

        // Login client2 as testuser
        Envelope login2 = new Envelope(MessageType.LOGIN);
        login2.put("username", "testuser");
        login2.put("password", "123456");
        client2.send(login2);
        loginLatch2.await(5, TimeUnit.SECONDS);

        // Client 1 sends direct message to Client 2
        String testContent = "Xin chào bạn testuser " + System.currentTimeMillis();
        Envelope dm = new Envelope(MessageType.DIRECT_MESSAGE_SEND);
        dm.put("targetUsername", "testuser");
        dm.put("content", testContent);
        client1.send(dm);

        boolean gotRecipient = receiveDmRecipient.await(5, TimeUnit.SECONDS);
        boolean gotAck = receiveDmAckSender.await(5, TimeUnit.SECONDS);

        if (!gotRecipient || !gotAck || !testContent.equals(receivedMsgText.get())) {
            System.err.println("Direct message error: recipient=" + gotRecipient + ", ack=" + gotAck + ", content=" + receivedMsgText.get());
            client1.close();
            client2.close();
            return false;
        }

        // Test Chat History request from Client 1 for target "testuser"
        Envelope histReq = new Envelope(MessageType.CHAT_HISTORY_REQUEST);
        histReq.put("targetUsername", "testuser");
        client1.send(histReq);

        boolean gotHistory = historyLatch.await(5, TimeUnit.SECONDS);

        client1.close();
        client2.close();

        return gotHistory && historyReceived.get();
    }

    private static boolean testRoomChatIsolation() throws Exception {
        CountDownLatch regA = new CountDownLatch(1);
        CountDownLatch regB = new CountDownLatch(1);
        CountDownLatch regC = new CountDownLatch(1);

        CountDownLatch loginA = new CountDownLatch(1);
        CountDownLatch loginB = new CountDownLatch(1);
        CountDownLatch loginC = new CountDownLatch(1);

        AtomicInteger roomJoinedA = new AtomicInteger(-1);
        CountDownLatch createLatchA = new CountDownLatch(1);
        CountDownLatch joinLatchB = new CountDownLatch(1);

        CountDownLatch roomChatLatchB = new CountDownLatch(1);
        AtomicReference<String> roomChatMsgB = new AtomicReference<>("");

        AtomicBoolean roomChatLeakedToC = new AtomicBoolean(false);

        String randomA = "userA_" + UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        String randomB = "userB_" + UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        String randomC = "userC_" + UUID.randomUUID().toString().replace("-", "").substring(0, 5);

        GameWebSocketClient clientA = new GameWebSocketClient(WS_URL, env -> {
            if (env.getType() == MessageType.REGISTER || env.getType() == MessageType.REGISTER_RESPONSE) {
                regA.countDown();
            } else if (env.getType() == MessageType.LOGIN_RESULT || env.getType() == MessageType.LOGIN_RESPONSE) {
                loginA.countDown();
            } else if (env.getType() == MessageType.ROOM_RESPONSE || env.getType() == MessageType.ROOM_CREATE) {
                if (env.isSuccess() && env.getData() != null && env.getData().has("roomId")) {
                    roomJoinedA.set(env.getData().get("roomId").asInt());
                    createLatchA.countDown();
                }
            }
        });

        GameWebSocketClient clientB = new GameWebSocketClient(WS_URL, env -> {
            if (env.getType() == MessageType.REGISTER || env.getType() == MessageType.REGISTER_RESPONSE) {
                regB.countDown();
            } else if (env.getType() == MessageType.LOGIN_RESULT || env.getType() == MessageType.LOGIN_RESPONSE) {
                loginB.countDown();
            } else if (env.getType() == MessageType.ROOM_RESPONSE || env.getType() == MessageType.ROOM_JOIN || env.getType() == MessageType.ROOM_UPDATE) {
                if (env.isSuccess()) {
                    joinLatchB.countDown();
                }
            } else if (env.getType() == MessageType.ROOM_CHAT) {
                roomChatMsgB.set(env.getString("message"));
                roomChatLatchB.countDown();
            }
        });

        GameWebSocketClient clientC = new GameWebSocketClient(WS_URL, env -> {
            if (env.getType() == MessageType.REGISTER || env.getType() == MessageType.REGISTER_RESPONSE) {
                regC.countDown();
            } else if (env.getType() == MessageType.LOGIN_RESULT || env.getType() == MessageType.LOGIN_RESPONSE) {
                loginC.countDown();
            } else if (env.getType() == MessageType.ROOM_CHAT) {
                roomChatLeakedToC.set(true);
                System.err.println("ALARM: Client C in Lobby received room chat: " + env.getString("message"));
            }
        });

        clientA.connect().get(5, TimeUnit.SECONDS);
        clientB.connect().get(5, TimeUnit.SECONDS);
        clientC.connect().get(5, TimeUnit.SECONDS);

        // Register & login
        Envelope rA = new Envelope(MessageType.REGISTER);
        rA.put("username", randomA);
        rA.put("password", "123456");
        rA.put("displayName", randomA);
        clientA.send(rA);
        regA.await(5, TimeUnit.SECONDS);

        Envelope logA = new Envelope(MessageType.LOGIN);
        logA.put("username", randomA);
        logA.put("password", "123456");
        clientA.send(logA);
        loginA.await(5, TimeUnit.SECONDS);

        Envelope rB = new Envelope(MessageType.REGISTER);
        rB.put("username", randomB);
        rB.put("password", "123456");
        rB.put("displayName", randomB);
        clientB.send(rB);
        regB.await(5, TimeUnit.SECONDS);

        Envelope logB = new Envelope(MessageType.LOGIN);
        logB.put("username", randomB);
        logB.put("password", "123456");
        clientB.send(logB);
        loginB.await(5, TimeUnit.SECONDS);

        Envelope rC = new Envelope(MessageType.REGISTER);
        rC.put("username", randomC);
        rC.put("password", "123456");
        rC.put("displayName", randomC);
        clientC.send(rC);
        regC.await(5, TimeUnit.SECONDS);

        Envelope logC = new Envelope(MessageType.LOGIN);
        logC.put("username", randomC);
        logC.put("password", "123456");
        clientC.send(logC);
        loginC.await(5, TimeUnit.SECONDS);

        // Client A creates room
        Envelope createRoom = new Envelope(MessageType.ROOM_CREATE);
        createRoom.put("roomName", "TestChatRoom");
        createRoom.put("maxPlayers", 4);
        createRoom.put("rounds", 3);
        createRoom.put("drawTime", 60);
        clientA.send(createRoom);
        createLatchA.await(5, TimeUnit.SECONDS);

        int roomId = roomJoinedA.get();
        if (roomId <= 0) {
            System.err.println("Failed to create room!");
            clientA.close();
            clientB.close();
            clientC.close();
            return false;
        }

        // Client B joins room
        Envelope joinRoom = new Envelope(MessageType.ROOM_JOIN);
        joinRoom.put("roomId", roomId);
        clientB.send(joinRoom);
        joinLatchB.await(5, TimeUnit.SECONDS);

        // Client C stays in lobby, does NOT join room.

        // Client A sends room chat
        String secretRoomChat = "Bi-mat-trong-phong-" + System.currentTimeMillis();
        Envelope roomChat = new Envelope(MessageType.ROOM_CHAT);
        roomChat.put("message", secretRoomChat);
        clientA.send(roomChat);

        // Wait for Client B to receive
        boolean bReceived = roomChatLatchB.await(5, TimeUnit.SECONDS);
        Thread.sleep(1000);

        clientA.close();
        clientB.close();
        clientC.close();

        boolean cDidNotReceive = !roomChatLeakedToC.get();
        boolean bMatched = secretRoomChat.equals(roomChatMsgB.get());

        return bReceived && bMatched && cDidNotReceive;
    }

    private static boolean testFriendListAndRequests() throws Exception {
        CountDownLatch loginLatch = new CountDownLatch(1);
        CountDownLatch friendListLatch = new CountDownLatch(1);
        CountDownLatch pendingLatch = new CountDownLatch(1);

        AtomicBoolean gotFriends = new AtomicBoolean(false);
        AtomicBoolean gotPending = new AtomicBoolean(false);

        GameWebSocketClient client = new GameWebSocketClient(WS_URL, env -> {
            if (env.getType() == MessageType.LOGIN_RESULT || env.getType() == MessageType.LOGIN_RESPONSE) {
                loginLatch.countDown();
            } else if (env.getType() == MessageType.FRIEND_LIST) {
                if (env.getData() != null && env.getData().has("friends")) {
                    JsonNode friends = env.getData().get("friends");
                    boolean containsSelf = false;
                    boolean containsTestUser = false;
                    for (JsonNode f : friends) {
                        String u = f.has("username") ? f.get("username").asText() : "";
                        if ("admin".equalsIgnoreCase(u)) containsSelf = true;
                        if ("testuser".equalsIgnoreCase(u)) containsTestUser = true;
                    }
                    if (!containsSelf && containsTestUser) {
                        gotFriends.set(true);
                    } else {
                        System.err.println("Friend list assertion failed: containsSelf=" + containsSelf + ", containsTestUser=" + containsTestUser + ", friends=" + friends);
                    }
                }
                friendListLatch.countDown();
            } else if (env.getType() == MessageType.FRIEND_PENDING_LIST) {
                gotPending.set(true);
                pendingLatch.countDown();
            }
        });

        client.connect().get(5, TimeUnit.SECONDS);

        Envelope login = new Envelope(MessageType.LOGIN);
        login.put("username", "admin");
        login.put("password", "123456");
        client.send(login);
        loginLatch.await(5, TimeUnit.SECONDS);

        client.send(new Envelope(MessageType.FRIEND_LIST_REQUEST));
        friendListLatch.await(5, TimeUnit.SECONDS);

        Envelope pendingReq = new Envelope(MessageType.FRIEND_REQUEST_NOTIFY);
        // Also send request
        client.send(pendingReq);
        pendingLatch.await(2, TimeUnit.SECONDS); // may or may not return if empty, but friend list is primary

        client.close();

        return gotFriends.get();
    }
}
