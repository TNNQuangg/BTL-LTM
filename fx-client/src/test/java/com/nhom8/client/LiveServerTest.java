package com.nhom8.client;

import com.nhom8.client.ws.GameWebSocketClient;
import com.nhom8.common.message.Envelope;
import com.nhom8.common.message.MessageType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LiveServerTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=== BẮT ĐẦU KIỂM THỬ LIVE CLIENT -> SERVER ===");

        CountDownLatch registerLatch = new CountDownLatch(1);
        CountDownLatch loginLatch = new CountDownLatch(1);
        CountDownLatch roomLatch = new CountDownLatch(1);

        AtomicBoolean registerSuccess = new AtomicBoolean(false);
        AtomicBoolean loginSuccess = new AtomicBoolean(false);
        AtomicBoolean roomSuccess = new AtomicBoolean(false);

        String endpoint = args.length > 0 ? args[0] : "ws://localhost:8080/ws/game";
        String testUsername = "user_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        String testPassword = "mypassword123";
        String testDisplayName = "Hoa Si " + testUsername;

        GameWebSocketClient client = new GameWebSocketClient(endpoint, env -> {
            System.out.println("[TEST NHẬN] " + env.getType() + " | success=" + env.isSuccess() + " | content=" + env.getContent());

            if (env.getType() == MessageType.REGISTER || env.getType() == MessageType.REGISTER_RESPONSE) {
                if (env.isSuccess()) {
                    registerSuccess.set(true);
                }
                registerLatch.countDown();
            } else if (env.getType() == MessageType.LOGIN_RESULT || env.getType() == MessageType.LOGIN_RESPONSE) {
                if (env.isSuccess()) {
                    loginSuccess.set(true);
                }
                loginLatch.countDown();
            } else if (env.getType() == MessageType.ROOM_UPDATE || env.getType() == MessageType.ROOM_CREATE) {
                roomSuccess.set(true);
                roomLatch.countDown();
            }
        });

        // 1. Kết nối
        System.out.println("1. Đang kết nối tới " + endpoint + "...");
        client.connect().get(5, TimeUnit.SECONDS);

        // 2. Test Đăng ký
        System.out.println("2. Gửi gói tin ĐĂNG KÝ: username=" + testUsername + ", displayName=" + testDisplayName);
        Envelope regEnv = new Envelope(MessageType.REGISTER);
        regEnv.put("username", testUsername);
        regEnv.put("password", testPassword);
        regEnv.put("displayName", testDisplayName);
        client.send(regEnv);

        boolean regWait = registerLatch.await(5, TimeUnit.SECONDS);
        if (!regWait || !registerSuccess.get()) {
            System.err.println("❌ TEST ĐĂNG KÝ THẤT BẠI!");
            System.exit(1);
        }
        System.out.println("✔ ĐĂNG KÝ THÀNH CÔNG!");

        // 3. Test Đăng nhập
        System.out.println("3. Gửi gói tin ĐĂNG NHẬP: username=" + testUsername);
        Envelope loginEnv = new Envelope(MessageType.LOGIN);
        loginEnv.put("username", testUsername);
        loginEnv.put("password", testPassword);
        client.send(loginEnv);

        boolean loginWait = loginLatch.await(5, TimeUnit.SECONDS);
        if (!loginWait || !loginSuccess.get()) {
            System.err.println("❌ TEST ĐĂNG NHẬP THẤT BẠI!");
            System.exit(1);
        }
        System.out.println("✔ ĐĂNG NHẬP THÀNH CÔNG!");

        // 4. Test Tạo phòng
        System.out.println("4. Gửi yêu cầu TẠO PHÒNG...");
        Envelope createEnv = new Envelope(MessageType.ROOM_CREATE);
        createEnv.put("roomName", "Phòng Doodle Vẽ Đẹp");
        createEnv.put("maxPlayers", 6);
        client.send(createEnv);

        boolean roomWait = roomLatch.await(5, TimeUnit.SECONDS);
        if (!roomWait || !roomSuccess.get()) {
            System.err.println("❌ TEST TẠO PHÒNG THẤT BẠI!");
            System.exit(1);
        }
        System.out.println("✔ TẠO PHÒNG THÀNH CÔNG!");

        // Đóng client
        client.close();
        System.out.println("=== TẤT CẢ CÁC BƯỚC TEST CLIENT - SERVER THÀNH CÔNG 100%! ===");
        System.exit(0);
    }
}
