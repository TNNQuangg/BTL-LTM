package com.nhom8.client.ui;

import com.nhom8.client.MainApplication;
import com.nhom8.common.message.Envelope;
import com.nhom8.common.message.MessageType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LobbyScreen {
    private final MainApplication app;
    private ObservableList<String> roomList;
    private ObservableList<String> playerList;

    private Scene scene;

    public LobbyScreen(MainApplication app) {
        this.app = app;
        this.roomList = FXCollections.observableArrayList();
        this.playerList = FXCollections.observableArrayList();
    }

    public Scene createScene() {
        if (scene != null) return scene;
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5B800;");

        // Top Header
        Label title = new Label("Sảnh Chờ - Scribble It!");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 20;");
        BorderPane.setAlignment(title, javafx.geometry.Pos.CENTER);
        root.setTop(title);

        // Center (Rooms)
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(20));
        
        Label roomLabel = new Label("Danh sách phòng:");
        roomLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        ListView<String> roomListView = new ListView<>(roomList);
        roomListView.setPrefHeight(400);
        
        Button createRoomBtn = new Button("Tạo phòng mới");
        createRoomBtn.setStyle("-fx-background-color: white; -fx-text-fill: #F5B800; -fx-font-weight: bold;");
        createRoomBtn.setOnAction(e -> createRoom());

        Button joinRoomBtn = new Button("Vào phòng");
        joinRoomBtn.setStyle("-fx-background-color: white; -fx-text-fill: #F5B800; -fx-font-weight: bold;");
        joinRoomBtn.setOnAction(e -> {
            String selected = roomListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Giả sử item có dạng "Phòng 1234 (Host: abc)"
                try {
                    String roomId = selected.split(" ")[1];
                    joinRoom(roomId);
                } catch(Exception ex) {}
            }
        });
        
        HBox btnBox = new HBox(10, createRoomBtn, joinRoomBtn);
        centerBox.getChildren().addAll(roomLabel, roomListView, btnBox);
        root.setCenter(centerBox);

        // Right (Players)
        VBox rightBox = new VBox(10);
        rightBox.setPadding(new Insets(20));
        rightBox.setPrefWidth(250);
        
        Label playerLabel = new Label("Người chơi online:");
        playerLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        ListView<String> playerListView = new ListView<>(playerList);
        playerListView.setPrefHeight(400);
        
        rightBox.getChildren().addAll(playerLabel, playerListView);
        root.setRight(rightBox);

        scene = new Scene(root, 1000, 600);
        return scene;
    }

    public Scene getScene() {
        return scene;
    }

    private void createRoom() {
        Envelope env = new Envelope(MessageType.ROOM_CREATE);
        app.getWsClient().send(env);
    }

    private void joinRoom(String roomId) {
        Envelope env = new Envelope(MessageType.ROOM_JOIN);
        env.put("roomId", roomId);
        app.getWsClient().send(env);
    }

    public void handleMessage(Envelope env) {
        Platform.runLater(() -> {
            if (env.getType() == MessageType.ROOM_LIST) {
                roomList.clear();
                // Parsing logic phụ thuộc vào cách server gửi. Tạm thời dùng raw content.
                if (env.getData() != null && env.getData().has("rooms")) {
                     env.getData().get("rooms").forEach(r -> {
                         roomList.add("Phòng " + r.get("id").asText() + " (Host: " + r.get("host").asText() + ")");
                     });
                }
            } else if (env.getType() == MessageType.ONLINE_LIST) {
                playerList.clear();
                if (env.getData() != null && env.getData().has("users")) {
                     env.getData().get("users").forEach(u -> {
                         playerList.add(u.get("username").asText());
                     });
                }
            } else if (env.getType() == MessageType.ROOM_JOIN || env.getType() == MessageType.ROOM_CREATE) {
                if (env.isSuccess()) {
                    String roomId = env.getString("roomId");
                    app.showGameScreen(roomId);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, env.getContent());
                    alert.show();
                }
            }
        });
    }
}
