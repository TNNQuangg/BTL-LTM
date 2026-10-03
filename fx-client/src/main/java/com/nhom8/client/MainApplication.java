package com.nhom8.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.nhom8.client.ui.GameScreen;
import com.nhom8.client.ui.LobbyScreen;
import com.nhom8.client.ws.GameWebSocketClient;
import com.nhom8.common.message.Envelope;
import com.nhom8.common.message.MessageType;

public class MainApplication extends Application {

    private GameWebSocketClient webSocketClient;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Scribble It! - Nhóm 8");

        // Load custom font if needed
        // Font.loadFont(getClass().getResourceAsStream("/fonts/gameScribble.ttf"), 14);

        showLoginScreen();
    }

    private Button loginBtn;
    private Label statusLabel;

    private void showLoginScreen() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F5B800; -fx-padding: 30;");

        Label titleLabel = new Label("Scribble It!");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 2);");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập (3-30 ký tự)");
        usernameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");
        passwordField.setMaxWidth(250);

        loginBtn = new Button("Đăng nhập");
        loginBtn.setStyle("-fx-background-color: white; -fx-text-fill: #F5B800; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #D8000C; -fx-font-weight: bold;");

        loginBtn.setOnAction(e -> {
            String user = usernameField.getText() != null ? usernameField.getText().trim() : "";
            String pass = passwordField.getText();
            
            // Kiểm tra tính hợp lệ dữ liệu ngay tại Client
            if (user.isEmpty() || !user.matches("^[a-zA-Z0-9_]{3,30}$")) {
                statusLabel.setStyle("-fx-text-fill: #D8000C; -fx-font-weight: bold;");
                statusLabel.setText("Tên đăng nhập không hợp lệ (3-30 ký tự, không chứa ký tự lạ)!");
                return;
            }
            if (pass == null || pass.isEmpty() || pass.length() > 64) {
                statusLabel.setStyle("-fx-text-fill: #D8000C; -fx-font-weight: bold;");
                statusLabel.setText("Mật khẩu không được để trống (tối đa 64 ký tự)!");
                return;
            }

            statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            statusLabel.setText("Đang kết nối tới máy chủ...");
            loginBtn.setDisable(true);
            connectAndLogin(user, pass);
        });

        root.getChildren().addAll(titleLabel, usernameField, passwordField, loginBtn, statusLabel);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectAndLogin(String username, String password) {
        if (webSocketClient == null) {
            webSocketClient = new GameWebSocketClient("ws://localhost:8080/ws/game", envelope -> {
                Platform.runLater(() -> handleMessage(envelope));
            });
        }

        webSocketClient.connect().thenAccept(ws -> {
            if (ws != null) {
                Platform.runLater(() -> statusLabel.setText("Đang xác thực thông tin..."));
                Envelope loginEnv = new Envelope(MessageType.LOGIN);
                loginEnv.put("username", username);
                loginEnv.put("password", password);
                webSocketClient.send(loginEnv);
            } else {
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    statusLabel.setStyle("-fx-text-fill: #D8000C; -fx-font-weight: bold;");
                    statusLabel.setText("Không thể kết nối máy chủ (ws://localhost:8080/ws/game)!");
                });
            }
        });
    }

    private LobbyScreen lobbyScreen;
    private GameScreen gameScreen;

    public GameWebSocketClient getWsClient() {
        return webSocketClient;
    }

    private void handleMessage(Envelope envelope) {
        if (envelope.getType() == MessageType.LOGIN_RESULT) {
            if (envelope.isSuccess()) {
                if (statusLabel != null) statusLabel.setText("");
                showLobbyScreen();
            } else {
                if (loginBtn != null) loginBtn.setDisable(false);
                if (statusLabel != null) {
                    statusLabel.setStyle("-fx-text-fill: #D8000C; -fx-font-weight: bold;");
                    statusLabel.setText(envelope.getContent());
                }
                Alert alert = new Alert(Alert.AlertType.ERROR, envelope.getContent());
                alert.show();
            }
        } else if (lobbyScreen != null && primaryStage.getScene() == lobbyScreen.getScene()) {
            lobbyScreen.handleMessage(envelope);
        } else if (gameScreen != null && primaryStage.getScene() == gameScreen.getScene()) {
            gameScreen.handleMessage(envelope);
        }
    }

    public void showLobbyScreen() {
        if (lobbyScreen == null) {
            lobbyScreen = new LobbyScreen(this);
        }
        primaryStage.setScene(lobbyScreen.createScene());
    }

    public void showGameScreen(String roomId) {
        gameScreen = new GameScreen(this, roomId);
        primaryStage.setScene(gameScreen.createScene());
    }

    @Override
    public void stop() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
