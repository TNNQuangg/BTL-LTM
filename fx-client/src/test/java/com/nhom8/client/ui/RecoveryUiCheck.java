package com.nhom8.client.ui;

import com.nhom8.client.MainApplication;
import com.nhom8.client.ws.GameWebSocketClient;
import com.nhom8.common.dto.StrokeDTO;
import com.nhom8.common.message.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Kiểm tra luồng snapshot thật của MainApplication và lớp phủ reconnect trên JavaFX. */
public class RecoveryUiCheck extends Application {
    static Throwable failure;
    @Override public void start(Stage stage) {
        try {
            MainApplication app = new MainApplication();
            set(app, "primaryStage", stage); set(app, "currentUsername", "p1");
            set(app, "webSocketClient", new GameWebSocketClient("ws://localhost:8080/ws/game", event -> {}) {
                @Override public void send(Envelope event) {}
                @Override public boolean isConnected() { return true; }
            });
            stage.setScene(new Scene(new VBox(), 1100, 760));
            app.showRoomScreen("test-room", "Kiểm thử khôi phục");
            List<Envelope> events = List.of(new Envelope(MessageType.GAME_START).put("scores", List.of(Map.of("username", "p1", "totalPoints", 80))),
                    new Envelope(MessageType.TOPIC_CONFIRMED).put("topic", "con mèo"),
                    new Envelope(MessageType.DRAW_START).put("drawingId", "draft-one").put("draftVersion", 5).put("timeLimit", 25),
                    new Envelope(MessageType.DRAWING_RESUME).put("drawData", List.of(new StrokeDTO("BRUSH", "#000000", 3,
                            List.of(10.0, 200.0), List.of(10.0, 200.0)))));
            Method handler = MainApplication.class.getDeclaredMethod("handleMessage", Envelope.class); handler.setAccessible(true);
            handler.invoke(app, new Envelope(MessageType.GAME_STATE_SNAPSHOT).put("events", events));
            Platform.runLater(() -> {
                try {
                    Object game = field(app, "gameScreen");
                    require(Boolean.TRUE.equals(field(game, "isDrawingPhase")), "Snapshot phải mở pha vẽ");
                    require(((List<?>) field(game, "myStrokes")).size() == 1, "Snapshot phải khôi phục nét vẽ");
                    require("draft-one".equals(field(game, "drawingId")), "Snapshot phải giữ đúng mã tranh");
                    Parent before = stage.getScene().getRoot();
                    AtomicInteger retries = new AtomicInteger();
                    ReconnectOverlay overlay = new ReconnectOverlay(stage.getScene(), System.currentTimeMillis() + 30000, retries::incrementAndGet);
                    stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout();
                    var button = stage.getScene().getRoot().lookupAll(".button").stream()
                            .filter(n -> n instanceof Button b && b.getText().equals("Thử kết nối lại")).findFirst().orElseThrow();
                    ((Button) button).fire(); require(retries.get() == 1, "Nút thử lại phải gọi reconnect thật");
                    var image = stage.getScene().snapshot(null);
                    var bitmap = new java.awt.image.BufferedImage((int) image.getWidth(), (int) image.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    for (int y = 0; y < bitmap.getHeight(); y++) for (int x = 0; x < bitmap.getWidth(); x++)
                        bitmap.setRGB(x, y, image.getPixelReader().getArgb(x, y));
                    javax.imageio.ImageIO.write(bitmap, "png", new java.io.File("target/reconnect-preview.png"));
                    overlay.close(); require(stage.getScene().getRoot() == before, "Kết nối thành công phải gỡ lớp phủ");
                    System.out.println("PASS: authoritative snapshot routing, canvas restore, overlay, manual retry and close.");
                } catch (Throwable error) { failure = error; error.printStackTrace(); }
                finally { Platform.exit(); }
            });
        } catch (Throwable error) { failure = error; error.printStackTrace(); Platform.exit(); }
    }
    static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(target);
    }
    static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }
    static void require(boolean valid, String message) { if (!valid) throw new AssertionError(message); }
    public static void main(String[] args) { launch(args); if (failure != null) System.exit(1); }
}
