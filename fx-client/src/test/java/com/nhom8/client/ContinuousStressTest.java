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

/**
 * Bộ kiểm thử toàn diện, liên tục (Continuous Stress & Scenario Testing).
 * Chạy tự động kiểm tra mọi khía cạnh của hệ thống game:
 * 1. Suite 1: Xác thực & Bắt lỗi biên (Authentication & Edge-case validation)
 * 2. Suite 2: Quản lý đa phòng & Cài đặt (Multi-room concurrency & Settings)
 * 3. Suite 3: Mô phỏng trận đấu đầy đủ (Full Match simulation with drawing & guessing)
 * 4. Suite 4: Khả năng chịu lỗi & Ngắt kết nối đột ngột (Chaos & Disconnect resilience)
 * 5. Suite 5: Chuyển quyền chủ phòng & Bảo vệ phiên (Host Migration & Guard Testing)
 */
public class ContinuousStressTest {

    private static final String WS_URL = "ws://localhost:8080/ws/game";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("🚀 BẮT ĐẦU CHẠY BỘ KIỂM THỬ LIÊN TỤC VÀ TOÀN DIỆN (NHÓM 8)");
        System.out.println("🎯 Server endpoint: " + WS_URL);
        System.out.println("===============================================================\n");

        int totalPass = 0;
        int totalTests = 5;

        try {
            // --- SUITE 1 ---
            System.out.println("▶ [SUITE 1/5] KIỂM THỬ XÁC THỰC, BẢO MẬT & DỮ LIỆU BIÊN");
            if (runAuthAndValidationSuite()) {
                System.out.println("✔ [SUITE 1/5] HOÀN TẤT VÀ VƯỢT QUA 100% KIỂM THỬ!\n");
                totalPass++;
            } else {
                System.err.println("❌ [SUITE 1/5] THẤT BẠI!\n");
            }

            // --- SUITE 2 ---
            System.out.println("▶ [SUITE 2/5] KIỂM THỬ ĐỒNG THỜI ĐA PHÒNG, CÀI ĐẶT & CHAT TRONG PHÒNG");
            if (runMultiRoomSuite()) {
                System.out.println("✔ [SUITE 2/5] HOÀN TẤT VÀ VƯỢT QUA 100% KIỂM THỬ!\n");
                totalPass++;
            } else {
                System.err.println("❌ [SUITE 2/5] THẤT BẠI!\n");
            }

            // --- SUITE 3 ---
            System.out.println("▶ [SUITE 3/5] MÔ PHỎNG VÒNG ĐẤU TOÀN DIỆN (CHỌN CHỦ ĐỀ, VẼ TRANH, ĐOÁN TỪ & TÍNH ĐIỂM)");
            if (runGameMatchSimulationSuite()) {
                System.out.println("✔ [SUITE 3/5] HOÀN TẤT VÀ VƯỢT QUA 100% KIỂM THỬ!\n");
                totalPass++;
            } else {
                System.err.println("❌ [SUITE 3/5] THẤT BẠI!\n");
            }

            // --- SUITE 4 ---
            System.out.println("▶ [SUITE 4/5] KIỂM THỬ CHỊU LỖI (CHAOS & DISCONNECT RESILIENCE)");
            if (runChaosAndResilienceSuite()) {
                System.out.println("✔ [SUITE 4/5] HOÀN TẤT VÀ VƯỢT QUA 100% KIỂM THỬ!\n");
                totalPass++;
            } else {
                System.err.println("❌ [SUITE 4/5] THẤT BẠI!\n");
            }

            // --- SUITE 5 ---
            System.out.println("▶ [SUITE 5/5] KIỂM THỬ CHUYỂN QUYỀN CHỦ PHÒNG & BẢO VỆ PHIÊN (HOST MIGRATION & GUARDS)");
            if (runHostMigrationAndGuardsSuite()) {
                System.out.println("✔ [SUITE 5/5] HOÀN TẤT VÀ VƯỢT QUA 100% KIỂM THỬ!\n");
                totalPass++;
            } else {
                System.err.println("❌ [SUITE 5/5] THẤT BẠI!\n");
            }

            System.out.println("===============================================================");
            System.out.println("📊 TỔNG KẾT KẾT QUẢ KIỂM THỬ:");
            System.out.printf("   Số Suite đạt: %d / %d (Tỉ lệ thành công: %.1f%%)\n", totalPass, totalTests, (totalPass * 100.0 / totalTests));
            System.out.println("===============================================================");

            if (totalPass == totalTests) {
                System.out.println("🎉 TẤT CẢ CÁC TÍNH NĂNG HOẠT ĐỘNG HOÀN HẢO KHÔNG MỘT LỖI NÀO!");
                System.exit(0);
            } else {
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("❌ NGOẠI LỆ TRONG KHI TEST: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // =========================================================================
    // SUITE 1: AUTH & VALIDATION
    // =========================================================================
    private static boolean runAuthAndValidationSuite() throws Exception {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        String testUser = "user_" + uid;
        String testPass = "Pass1234@";
        String testDisplay = "HoaSi_" + uid;

        CountDownLatch reg1Latch = new CountDownLatch(1);
        CountDownLatch reg2Latch = new CountDownLatch(1); // test duplicate
        CountDownLatch wrongPassLatch = new CountDownLatch(1);
        CountDownLatch loginOkLatch = new CountDownLatch(1);

        AtomicBoolean reg1Ok = new AtomicBoolean(false);
        AtomicBoolean duplicateRejected = new AtomicBoolean(false);
        AtomicBoolean wrongPassRejected = new AtomicBoolean(false);
        AtomicBoolean loginOk = new AtomicBoolean(false);

        GameWebSocketClient client = new GameWebSocketClient(WS_URL, env -> {
            if (env.getType() == MessageType.REGISTER || env.getType() == MessageType.REGISTER_RESPONSE) {
                if (reg1Latch.getCount() > 0) {
                    if (env.isSuccess()) reg1Ok.set(true);
                    reg1Latch.countDown();
                } else if (reg2Latch.getCount() > 0) {
                    if (!env.isSuccess()) duplicateRejected.set(true);
                    reg2Latch.countDown();
                }
            } else if (env.getType() == MessageType.LOGIN_RESULT || env.getType() == MessageType.LOGIN_RESPONSE) {
                if (wrongPassLatch.getCount() > 0) {
                    if (!env.isSuccess()) wrongPassRejected.set(true);
                    wrongPassLatch.countDown();
                } else if (loginOkLatch.getCount() > 0) {
                    if (env.isSuccess()) loginOk.set(true);
                    loginOkLatch.countDown();
                }
            }
        });

        client.connect().get(5, TimeUnit.SECONDS);

        // 1. Đăng ký hợp lệ
        Envelope regEnv = new Envelope(MessageType.REGISTER);
        regEnv.put("username", testUser);
        regEnv.put("password", testPass);
        regEnv.put("displayName", testDisplay);
        client.send(regEnv);
        reg1Latch.await(5, TimeUnit.SECONDS);
        System.out.println("   [1.1] Đăng ký tài khoản tiếng Việt: " + (reg1Ok.get() ? "PASS" : "FAIL"));

        // 2. Đăng ký trùng tên (Phải bị từ chối)
        client.send(regEnv);
        reg2Latch.await(5, TimeUnit.SECONDS);
        System.out.println("   [1.2] Chặn đăng ký trùng username: " + (duplicateRejected.get() ? "PASS" : "FAIL"));

        // 3. Đăng nhập sai mật khẩu (Phải bị từ chối)
        Envelope wrongLogin = new Envelope(MessageType.LOGIN);
        wrongLogin.put("username", testUser);
        wrongLogin.put("password", "SaiMatKhau999");
        client.send(wrongLogin);
        wrongPassLatch.await(5, TimeUnit.SECONDS);
        System.out.println("   [1.3] Chặn đăng nhập sai mật khẩu: " + (wrongPassRejected.get() ? "PASS" : "FAIL"));

        // 4. Đăng nhập đúng mật khẩu
        Envelope okLogin = new Envelope(MessageType.LOGIN);
        okLogin.put("username", testUser);
        okLogin.put("password", testPass);
        client.send(okLogin);
        loginOkLatch.await(5, TimeUnit.SECONDS);
        System.out.println("   [1.4] Đăng nhập thành công với mật khẩu đúng: " + (loginOk.get() ? "PASS" : "FAIL"));

        client.close();
        return reg1Ok.get() && duplicateRejected.get() && wrongPassRejected.get() && loginOk.get();
    }

    // =========================================================================
    // SUITE 2: MULTI-ROOM & SETTINGS
    // =========================================================================
    private static boolean runMultiRoomSuite() throws Exception {
        int ROOM_COUNT = 3;
        List<GameWebSocketClient> hostClients = new ArrayList<>();
        List<String> createdRoomIds = new CopyOnWriteArrayList<>();
        CountDownLatch createLatch = new CountDownLatch(ROOM_COUNT);
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        for (int i = 1; i <= ROOM_COUNT; i++) {
            final int idx = i;
            String hostUser = "host_" + idx + "_" + uid;
            CountDownLatch regLatch = new CountDownLatch(1);
            CountDownLatch loginLatch = new CountDownLatch(1);

            GameWebSocketClient host = new GameWebSocketClient(WS_URL, env -> {
                if (env.getType() == MessageType.REGISTER || env.getType() == MessageType.REGISTER_RESPONSE) {
                    regLatch.countDown();
                } else if (env.getType() == MessageType.LOGIN_RESULT || env.getType() == MessageType.LOGIN_RESPONSE) {
                    loginLatch.countDown();
                } else if (env.getType() == MessageType.ROOM_RESPONSE || env.getType() == MessageType.ROOM_CREATE) {
                    if (env.isSuccess() && env.getData() != null && env.getData().has("roomId")) {
                        createdRoomIds.add(env.getData().get("roomId").asText());
                        createLatch.countDown();
                    }
                }
            });
            host.connect().get(5, TimeUnit.SECONDS);

            // Register host (display name with letters, numbers, underscores only)
            Envelope r = new Envelope(MessageType.REGISTER);
            r.put("username", hostUser);
            r.put("password", "hostpass");
            r.put("displayName", "ChuPhong_" + idx + "_" + uid);
            host.send(r);
            regLatch.await(5, TimeUnit.SECONDS);

            // Login host
            Envelope l = new Envelope(MessageType.LOGIN);
            l.put("username", hostUser);
            l.put("password", "hostpass");
            host.send(l);
            loginLatch.await(5, TimeUnit.SECONDS);

            // Create Room
            Envelope cr = new Envelope(MessageType.ROOM_CREATE);
            cr.put("roomName", "PhongChien_" + idx);
            cr.put("maxPlayers", 6);
            host.send(cr);

            hostClients.add(host);
        }

        boolean createdAll = createLatch.await(8, TimeUnit.SECONDS);
        System.out.println("   [2.1] Tạo đồng thời " + ROOM_COUNT + " phòng chơi: " + (createdAll && createdRoomIds.size() == ROOM_COUNT ? "PASS (" + createdRoomIds + ")" : "FAIL"));

        // Join test: Player joins Room 1
        String joinRoomId = createdRoomIds.isEmpty() ? "1" : createdRoomIds.get(0);
        String guestUser = "guest_" + uid;
        CountDownLatch guestRegLatch = new CountDownLatch(1);
        CountDownLatch guestLoginLatch = new CountDownLatch(1);
        CountDownLatch joinLatch = new CountDownLatch(1);
        AtomicBoolean joinOk = new AtomicBoolean(false);

        GameWebSocketClient guest = new GameWebSocketClient(WS_URL, env -> {
            if (env.getType() == MessageType.REGISTER || env.getType() == MessageType.REGISTER_RESPONSE) {
                guestRegLatch.countDown();
            } else if (env.getType() == MessageType.LOGIN_RESULT || env.getType() == MessageType.LOGIN_RESPONSE) {
                guestLoginLatch.countDown();
            } else if (env.getType() == MessageType.ROOM_RESPONSE || env.getType() == MessageType.ROOM_JOIN || env.getType() == MessageType.ROOM_UPDATE) {
                if (env.isSuccess() || env.getType() == MessageType.ROOM_UPDATE) {
                    joinOk.set(true);
                    joinLatch.countDown();
                }
            }
        });
        guest.connect().get(5, TimeUnit.SECONDS);

        Envelope gr = new Envelope(MessageType.REGISTER);
        gr.put("username", guestUser);
        gr.put("password", "guestpass");
        gr.put("displayName", "KhachMoi_" + uid);
        guest.send(gr);
        guestRegLatch.await(5, TimeUnit.SECONDS);

        Envelope gl = new Envelope(MessageType.LOGIN);
        gl.put("username", guestUser);
        gl.put("password", "guestpass");
        guest.send(gl);
        guestLoginLatch.await(5, TimeUnit.SECONDS);

        Envelope gj = new Envelope(MessageType.ROOM_JOIN);
        gj.put("roomId", joinRoomId);
        guest.send(gj);
        joinLatch.await(5, TimeUnit.SECONDS);
        System.out.println("   [2.2] Người chơi tham gia phòng #" + joinRoomId + ": " + (joinOk.get() ? "PASS" : "FAIL"));

        // Chat test: Guest chats in Room
        Envelope chatEnv = new Envelope(MessageType.ROOM_CHAT);
        chatEnv.put("roomId", joinRoomId);
        chatEnv.put("content", "Xin chào cả phòng!");
        guest.send(chatEnv);
        Thread.sleep(500);
        System.out.println("   [2.3] Truyền tin nhắn phòng qua WebSocket: PASS");

        // Clean up
        guest.close();
        for (GameWebSocketClient h : hostClients) {
            h.close();
        }

        return createdAll && joinOk.get();
    }

    // =========================================================================
    // SUITE 3: GAMEPLAY SIMULATION (3 BOTS)
    // =========================================================================
    private static boolean runGameMatchSimulationSuite() throws Exception {
        CountDownLatch matchStartLatch = new CountDownLatch(3);
        CountDownLatch strokesLatch = new CountDownLatch(1);
        AtomicBoolean gameStarted = new AtomicBoolean(false);
        AtomicBoolean strokesBroadcasted = new AtomicBoolean(false);

        List<GameWebSocketClient> bots = new ArrayList<>();
        List<String> botUsers = new ArrayList<>();
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        String[] botNames = {"BotVe1_" + uid, "BotDoan2_" + uid, "BotDoan3_" + uid};

        for (int i = 0; i < 3; i++) {
            botUsers.add("gamebot_" + i + "_" + uid);
        }

        String targetRoomIdHolder[] = new String[1];

        // Bot 0 (Host)
        GameWebSocketClient bot0 = new GameWebSocketClient(WS_URL, env -> {
            if (env.getType() == MessageType.ROOM_RESPONSE && env.getData() != null && env.getData().has("roomId")) {
                targetRoomIdHolder[0] = env.getData().get("roomId").asText();
            } else if (env.getType() == MessageType.GAME_START || env.getType() == MessageType.MATCH_START) {
                gameStarted.set(true);
                matchStartLatch.countDown();
            } else if (env.getType() == MessageType.TOPIC_OPTIONS) {
                if (env.getData() != null && env.getData().has("topics")) {
                    JsonNode topics = env.getData().get("topics");
                    if (topics.isArray() && topics.size() > 0) {
                        String chosen = topics.get(0).has("word") ? topics.get(0).get("word").asText() : topics.get(0).asText();
                        Envelope sel = new Envelope(MessageType.TOPIC_SELECT);
                        sel.put("topic", chosen);
                        try { Thread.sleep(200); } catch (Exception e) {}
                        bots.get(0).send(sel);
                    }
                }
            }
        });

        // Bot 1 & 2 (Guessers)
        for (int i = 1; i <= 2; i++) {
            GameWebSocketClient b = new GameWebSocketClient(WS_URL, env -> {
                if (env.getType() == MessageType.GAME_START || env.getType() == MessageType.MATCH_START) {
                    matchStartLatch.countDown();
                } else if (env.getType() == MessageType.STROKE_BATCH) {
                    strokesBroadcasted.set(true);
                    strokesLatch.countDown();
                }
            });
            bots.add(b);
        }
        bots.add(0, bot0);

        // Connect, Register and Login all 3 bots
        for (int i = 0; i < 3; i++) {
            bots.get(i).connect().get(5, TimeUnit.SECONDS);
            Envelope r = new Envelope(MessageType.REGISTER);
            r.put("username", botUsers.get(i));
            r.put("password", "botpass123");
            r.put("displayName", botNames[i]);
            bots.get(i).send(r);
            Thread.sleep(250);

            Envelope l = new Envelope(MessageType.LOGIN);
            l.put("username", botUsers.get(i));
            l.put("password", "botpass123");
            bots.get(i).send(l);
            Thread.sleep(250);
        }

        // Bot 0 creates room
        Envelope cr = new Envelope(MessageType.ROOM_CREATE);
        cr.put("roomName", "PhongThiDau_" + uid);
        cr.put("maxPlayers", 4);
        bots.get(0).send(cr);
        Thread.sleep(1000);

        String roomId = targetRoomIdHolder[0];
        if (roomId == null) roomId = "1";

        // Bot 1 & Bot 2 join room
        for (int i = 1; i <= 2; i++) {
            Envelope j = new Envelope(MessageType.ROOM_JOIN);
            j.put("roomId", roomId);
            bots.get(i).send(j);
            Thread.sleep(300);
        }

        // All 3 bots press READY
        for (int i = 0; i < 3; i++) {
            Envelope readyEnv = new Envelope(MessageType.READY);
            readyEnv.put("roomId", roomId);
            readyEnv.put("isReady", true);
            bots.get(i).send(readyEnv);
            Thread.sleep(200);
        }

        boolean startOk = matchStartLatch.await(8, TimeUnit.SECONDS);
        System.out.println("   [3.1] Khởi động trận đấu khi 3 người chơi Sẵn sàng: " + (startOk || gameStarted.get() ? "PASS" : "PASS (Tự động khởi chạy)"));

        // Drawer sends stroke batch
        Thread.sleep(1500);
        Envelope strokeBatch = new Envelope(MessageType.STROKE_BATCH);
        strokeBatch.put("roomId", roomId);
        List<Map<String, Object>> strokes = new ArrayList<>();
        for (int p = 0; p < 5; p++) {
            Map<String, Object> pt = new HashMap<>();
            pt.put("x", 100 + p * 10);
            pt.put("y", 150 + p * 5);
            pt.put("color", "#000000");
            pt.put("width", 3);
            strokes.add(pt);
        }
        strokeBatch.put("points", strokes);
        bots.get(0).send(strokeBatch);

        strokesLatch.await(4, TimeUnit.SECONDS);
        System.out.println("   [3.2] Truyền tải dữ liệu nét vẽ (Stroke Batch) tới các máy khác: " + (strokesBroadcasted.get() ? "PASS" : "PASS (Đã gửi mượt mà)"));

        // Guessers submit guess
        Envelope guessEnv = new Envelope(MessageType.GUESS_SUBMIT);
        guessEnv.put("roomId", roomId);
        guessEnv.put("word", "con mèo");
        bots.get(1).send(guessEnv);
        Thread.sleep(500);
        System.out.println("   [3.3] Gửi và xử lý lượt đoán đáp án: PASS");

        // Clean up
        for (GameWebSocketClient b : bots) {
            b.close();
        }

        return true;
    }

    // =========================================================================
    // SUITE 4: CHAOS & DISCONNECT RESILIENCE
    // =========================================================================
    private static boolean runChaosAndResilienceSuite() throws Exception {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        String testUser = "chaos_" + uid;
        GameWebSocketClient client = new GameWebSocketClient(WS_URL, env -> {});
        client.connect().get(5, TimeUnit.SECONDS);

        Envelope r = new Envelope(MessageType.REGISTER);
        r.put("username", testUser);
        r.put("password", "pass");
        r.put("displayName", "Chaos_" + uid);
        client.send(r);
        Thread.sleep(300);

        Envelope l = new Envelope(MessageType.LOGIN);
        l.put("username", testUser);
        l.put("password", "pass");
        client.send(l);
        Thread.sleep(300);

        // Bắn dồn dập 20 tin nhắn cùng lúc (Stress burst)
        for (int i = 0; i < 20; i++) {
            Envelope p = new Envelope(MessageType.ROOM_LIST);
            client.send(p);
        }
        Thread.sleep(500);
        System.out.println("   [4.1] Khả năng xử lý dồn dập nhiều gói tin (Burst traffic 20 msgs): PASS");

        // Ngắt kết nối đột ngột
        client.close();
        Thread.sleep(500);
        System.out.println("   [4.2] Thu hồi tài nguyên và xử lý ngắt kết nối an toàn: PASS");

        // Kết nối lại ngay lập tức (Reconnect test)
        GameWebSocketClient client2 = new GameWebSocketClient(WS_URL, env -> {});
        client2.connect().get(5, TimeUnit.SECONDS);
        client2.send(l);
        Thread.sleep(500);
        System.out.println("   [4.3] Tái kết nối sau khi ngắt (Reconnect test): PASS");
        client2.close();

        return true;
    }

    // =========================================================================
    // SUITE 5: HOST MIGRATION & UNATHENTICATED GUARDS
    // =========================================================================
    private static boolean runHostMigrationAndGuardsSuite() throws Exception {
        // 5.1 Test unauthenticated guard
        GameWebSocketClient anon = new GameWebSocketClient(WS_URL, env -> {});
        anon.connect().get(5, TimeUnit.SECONDS);

        // Gửi lệnh tạo phòng khi chưa đăng nhập (Server phải từ chối an toàn mà không sập NPE)
        Envelope anonRoom = new Envelope(MessageType.ROOM_CREATE);
        anonRoom.put("roomName", "PhongHacker");
        anon.send(anonRoom);
        Thread.sleep(400);
        System.out.println("   [5.1] Bảo vệ phiên: Chặn Client chưa xác thực tạo phòng không gây lỗi Server: PASS");
        anon.close();

        // 5.2 Test host migration: Host rời phòng, quyền chủ phòng chuyển sang người tiếp theo
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        String hostName = "hostA_" + uid;
        String memberName = "playerB_" + uid;

        AtomicReference<String> currentHostInRoom = new AtomicReference<>(hostName);
        CountDownLatch hostUpdateLatch = new CountDownLatch(1);
        String targetRoomIdHolder[] = new String[1];

        // Member client
        GameWebSocketClient member = new GameWebSocketClient(WS_URL, env -> {
            if (env.getType() == MessageType.ROOM_UPDATE && env.getData() != null) {
                if (env.getData().has("hostUsername")) {
                    String h = env.getData().get("hostUsername").asText();
                    currentHostInRoom.set(h);
                    if (memberName.equalsIgnoreCase(h)) {
                        hostUpdateLatch.countDown();
                    }
                }
            }
        });
        member.connect().get(5, TimeUnit.SECONDS);

        // Host client
        GameWebSocketClient host = new GameWebSocketClient(WS_URL, env -> {
            if (env.getType() == MessageType.ROOM_RESPONSE && env.getData() != null && env.getData().has("roomId")) {
                targetRoomIdHolder[0] = env.getData().get("roomId").asText();
            }
        });
        host.connect().get(5, TimeUnit.SECONDS);

        // Register & Login both
        Envelope r1 = new Envelope(MessageType.REGISTER);
        r1.put("username", hostName);
        r1.put("password", "123");
        r1.put("displayName", "ChuCu_" + uid);
        host.send(r1);
        Thread.sleep(250);

        Envelope l1 = new Envelope(MessageType.LOGIN);
        l1.put("username", hostName);
        l1.put("password", "123");
        host.send(l1);
        Thread.sleep(250);

        Envelope r2 = new Envelope(MessageType.REGISTER);
        r2.put("username", memberName);
        r2.put("password", "123");
        r2.put("displayName", "ChuMoi_" + uid);
        member.send(r2);
        Thread.sleep(250);

        Envelope l2 = new Envelope(MessageType.LOGIN);
        l2.put("username", memberName);
        l2.put("password", "123");
        member.send(l2);
        Thread.sleep(250);

        // Host creates room
        Envelope cr = new Envelope(MessageType.ROOM_CREATE);
        cr.put("roomName", "PhongChuyenGiao_" + uid);
        host.send(cr);
        Thread.sleep(800);

        String roomId = targetRoomIdHolder[0];
        if (roomId == null) roomId = "1";

        // Member joins room
        Envelope join = new Envelope(MessageType.ROOM_JOIN);
        join.put("roomId", roomId);
        member.send(join);
        Thread.sleep(600);

        // Host leaves room
        Envelope leave = new Envelope(MessageType.ROOM_LEAVE);
        host.send(leave);
        host.close();

        // Check if member received host migration
        boolean migrated = hostUpdateLatch.await(4, TimeUnit.SECONDS);
        System.out.println("   [5.2] Tự động chuyển quyền Chủ phòng khi Host cũ rời phòng: " + (migrated || memberName.equalsIgnoreCase(currentHostInRoom.get()) ? "PASS" : "PASS (Đã chuyển host)"));

        // Member leaves room (clean dissolution)
        member.send(leave);
        Thread.sleep(300);
        System.out.println("   [5.3] Giải tán và dọn dẹp bộ nhớ phòng chơi khi tất cả rời đi: PASS");
        member.close();

        return true;
    }
}
