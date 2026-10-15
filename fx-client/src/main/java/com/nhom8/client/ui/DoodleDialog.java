package com.nhom8.client.ui;

import com.nhom8.client.MainApplication;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Hệ thống Modal Dialog và Toast Notification phong cách Doodle Sketch
 * Thay thế hoàn toàn các Alert / TextInputDialog mặc định của JavaFX.
 */
public class DoodleDialog {

    public static void info(Stage owner, String title, String message) {
        showCustomAlert(owner, "🎨 " + (title != null ? title : "Thông báo"), message, "ĐỒNG Ý", null);
    }

    public static void warning(Stage owner, String title, String message) {
        showCustomAlert(owner, "⚠ " + (title != null ? title : "Cảnh báo"), message, "ĐÃ HIỂU", null);
    }

    public static void error(Stage owner, String title, String message) {
        showCustomAlert(owner, "❌ " + (title != null ? title : "Lỗi xảy ra"), message, "ĐÓNG", null);
    }

    public static void confirm(Stage owner, String title, String message, String okText, String cancelText, Consumer<Boolean> callback) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> confirm(owner, title, message, okText, cancelText, callback));
            return;
        }

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        if (owner != null) dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(15);
        box.getStyleClass().add("doodle-dialog-box");
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(420);
        box.setMaxWidth(520);

        Label titleLbl = new Label(title != null ? title : "❓ Xác nhận");
        titleLbl.setStyle("-fx-font-family: 'Amatic SC'; -fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: -fx-ink;");

        Label msgLbl = new Label(message != null ? message : "");
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 19px; -fx-text-fill: -fx-ink; -fx-alignment: center;");

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button(cancelText != null ? cancelText : "TỪ CHỐI");
        cancelBtn.getStyleClass().add("pencil-button-danger");
        cancelBtn.setOnAction(e -> {
            dialog.close();
            if (callback != null) callback.accept(false);
        });

        Button okBtn = new Button(okText != null ? okText : "ĐỒNG Ý");
        okBtn.getStyleClass().add("pencil-button-success");
        okBtn.setOnAction(e -> {
            dialog.close();
            if (callback != null) callback.accept(true);
        });

        btnBox.getChildren().addAll(cancelBtn, okBtn);
        box.getChildren().addAll(titleLbl, msgLbl, btnBox);

        StackPane root = new StackPane(box);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        MainApplication.applyTheme(scene);

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public static void input(Stage owner, String title, String prompt, String defaultValue, Consumer<String> callback) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> input(owner, title, prompt, defaultValue, callback));
            return;
        }

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        if (owner != null) dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(15);
        box.getStyleClass().add("doodle-dialog-box");
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(420);
        box.setMaxWidth(520);

        Label titleLbl = new Label(title != null ? title : "✏ Nhập thông tin");
        titleLbl.setStyle("-fx-font-family: 'Amatic SC'; -fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: -fx-ink;");

        Label promptLbl = new Label(prompt != null ? prompt : "");
        promptLbl.setWrapText(true);
        promptLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 18px; -fx-text-fill: -fx-ink;");

        TextField textField = new TextField(defaultValue != null ? defaultValue : "");
        textField.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 18px; -fx-padding: 6 12;");
        textField.setMaxWidth(Double.MAX_VALUE);

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("HỦY BỎ");
        cancelBtn.getStyleClass().add("pencil-button-danger");
        cancelBtn.setOnAction(e -> {
            dialog.close();
            if (callback != null) callback.accept(null);
        });

        Button okBtn = new Button("XÁC NHẬN");
        okBtn.getStyleClass().add("pencil-button-success");
        okBtn.setOnAction(e -> {
            dialog.close();
            if (callback != null) callback.accept(textField.getText().trim());
        });

        textField.setOnAction(e -> okBtn.fire());

        btnBox.getChildren().addAll(cancelBtn, okBtn);
        box.getChildren().addAll(titleLbl, promptLbl, textField, btnBox);

        StackPane root = new StackPane(box);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        MainApplication.applyTheme(scene);

        dialog.setScene(scene);
        Platform.runLater(textField::requestFocus);
        dialog.showAndWait();
    }

    private static void showCustomAlert(Stage owner, String title, String message, String okText, Runnable onClose) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showCustomAlert(owner, title, message, okText, onClose));
            return;
        }

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        if (owner != null) dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(15);
        box.getStyleClass().add("doodle-dialog-box");
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(400);
        box.setMaxWidth(500);

        Label titleLbl = new Label(title != null ? title : "Thông báo");
        titleLbl.setStyle("-fx-font-family: 'Amatic SC'; -fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: -fx-ink;");

        Label msgLbl = new Label(message != null ? message : "");
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 19px; -fx-text-fill: -fx-ink; -fx-alignment: center;");

        Button okBtn = new Button(okText != null ? okText : "ĐỒNG Ý");
        okBtn.getStyleClass().add("pencil-button-sm");
        okBtn.setOnAction(e -> {
            dialog.close();
            if (onClose != null) onClose.run();
        });

        box.getChildren().addAll(titleLbl, msgLbl, okBtn);

        StackPane root = new StackPane(box);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        MainApplication.applyTheme(scene);

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Hiển thị thông báo Toast nổi nhẹ nhàng, tự động biến mất sau 2.5 giây.
     */
    public static void showToast(Stage owner, String message) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showToast(owner, message));
            return;
        }
        if (owner == null) return;

        Popup popup = new Popup();
        Label toastLabel = new Label(message);
        toastLabel.getStyleClass().add("doodle-toast");

        popup.getContent().add(toastLabel);
        popup.setAutoHide(true);

        toastLabel.applyCss();
        double w = toastLabel.prefWidth(-1);
        if (w <= 0) w = 300;
        double x = owner.getX() + (owner.getWidth() - w) / 2.0;
        double y = owner.getY() + owner.getHeight() - 110;
        popup.show(owner, x, y);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toastLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition stay = new PauseTransition(Duration.seconds(2.2));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), toastLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> popup.hide());

        SequentialTransition seq = new SequentialTransition(fadeIn, stay, fadeOut);
        seq.play();
    }
}
