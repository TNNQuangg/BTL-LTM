package com.nhom8.client.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

/** Lớp phủ chặn thao tác trong khi chờ khôi phục phiên từ máy chủ. */
public final class ReconnectOverlay {
    private final Scene scene;
    private final Parent previous;
    private final StackPane root;
    private final Timeline timer;
    private final Label countdown = new Label();
    private final Label status = new Label("Đang kết nối lại…");

    public ReconnectOverlay(Scene scene, long estimatedDeadline, Runnable retry) {
        this.scene = scene; previous = scene.getRoot();
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(560, 370);
        card.setStyle("-fx-background-color: #FDFBF7; -fx-padding: 28; -fx-border-color: #222; -fx-border-width: 3;");
        Label icon = new Label("⚠"); icon.setStyle("-fx-font-size: 38;");
        Label title = new Label("Mất kết nối với máy chủ");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");
        Label description = new Label("Đang thử kết nối lại. Vị trí của bạn được giữ tối đa 30 giây.");
        description.setWrapText(true);
        ProgressIndicator loading = new ProgressIndicator(); loading.setMaxSize(32, 32);
        Button button = new Button("Thử kết nối lại");
        button.getStyleClass().add("pencil-button-sm");
        button.setOnAction(event -> retry.run());
        Label note = new Label("Thời gian trận đấu vẫn tiếp tục chạy.");
        card.getChildren().addAll(icon, title, description, loading, status, countdown, button, note);
        StackPane scrim = new StackPane(); scrim.setStyle("-fx-background-color: rgba(15,23,42,.75);");
        root = new StackPane(previous, scrim, card);
        scene.setRoot(root);
        Runnable tick = () -> countdown.setText("Thời gian giữ chỗ ước tính: "
                + Math.max(0, (estimatedDeadline - System.currentTimeMillis() + 999) / 1000) + " giây");
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> tick.run()));
        timer.setCycleCount(Timeline.INDEFINITE); tick.run(); timer.play();
    }

    public void setStatus(String value) { status.setText(value); }
    public void close() { timer.stop(); root.getChildren().remove(previous); scene.setRoot(previous); }
}
