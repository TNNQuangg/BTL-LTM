package com.nhom8.client.ui;

import com.nhom8.client.MainApplication;
import com.nhom8.common.dto.StrokeDTO;
import com.nhom8.common.message.Envelope;
import com.nhom8.common.message.MessageType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.ArrayList;
import java.util.List;

public class GameScreen {
    private final MainApplication app;
    private final String roomId;
    private ObservableList<String> chatMessages;
    private ObservableList<String> playerList;
    private Canvas canvas;
    private GraphicsContext gc;
    private boolean isMyTurnToDraw = false;
    private Label topInfoLabel;
    
    // Lưu tạm các điểm vẽ
    private List<Double> currentXPoints = new ArrayList<>();
    private List<Double> currentYPoints = new ArrayList<>();
    private Color currentColor = Color.BLACK;
    private double currentWidth = 3.0;
    
    private Scene scene;

    public GameScreen(MainApplication app, String roomId) {
        this.app = app;
        this.roomId = roomId;
        this.chatMessages = FXCollections.observableArrayList();
        this.playerList = FXCollections.observableArrayList();
    }

    public Scene createScene() {
        if (scene != null) return scene;
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5B800;");

        // Top
        topInfoLabel = new Label("Phòng: " + roomId + " | Đang chờ người chơi...");
        topInfoLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 10;");
        BorderPane.setAlignment(topInfoLabel, javafx.geometry.Pos.CENTER);
        
        Button readyBtn = new Button("Sẵn sàng");
        readyBtn.setOnAction(e -> {
            app.getWsClient().send(new Envelope(MessageType.READY));
        });
        
        HBox topBox = new HBox(20, topInfoLabel, readyBtn);
        topBox.setAlignment(javafx.geometry.Pos.CENTER);
        root.setTop(topBox);

        // Center (Canvas)
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        canvas = new Canvas(600, 450);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        
        setupCanvasEvents();
        
        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(javafx.geometry.Pos.CENTER);
        Button clearBtn = new Button("Xóa bảng");
        clearBtn.setOnAction(e -> clearCanvas());
        
        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        colorPicker.setOnAction(e -> currentColor = colorPicker.getValue());
        
        Slider widthSlider = new Slider(1, 20, 3);
        widthSlider.valueProperty().addListener((obs, oldVal, newVal) -> currentWidth = newVal.doubleValue());
        
        toolbar.getChildren().addAll(new Label("Màu:"), colorPicker, new Label("Độ dày:"), widthSlider, clearBtn);
        
        centerBox.getChildren().addAll(canvas, toolbar);
        root.setCenter(centerBox);

        // Left (Players)
        VBox leftBox = new VBox(5);
        leftBox.setPadding(new Insets(10));
        leftBox.setPrefWidth(200);
        Label pLabel = new Label("Người chơi:");
        pLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        ListView<String> pList = new ListView<>(playerList);
        leftBox.getChildren().addAll(pLabel, pList);
        root.setLeft(leftBox);

        // Right (Chat / Guess)
        VBox rightBox = new VBox(5);
        rightBox.setPadding(new Insets(10));
        rightBox.setPrefWidth(250);
        ListView<String> chatView = new ListView<>(chatMessages);
        chatView.setPrefHeight(400);
        
        TextField guessInput = new TextField();
        guessInput.setPromptText("Nhập đáp án hoặc chat...");
        guessInput.setOnAction(e -> {
            String text = guessInput.getText();
            if (!text.isEmpty()) {
                Envelope env = new Envelope(MessageType.ROOM_CHAT);
                env.setContent(text);
                app.getWsClient().send(env);
                guessInput.clear();
            }
        });
        
        rightBox.getChildren().addAll(chatView, guessInput);
        root.setRight(rightBox);

        // Chồng UI chọn Topic lên trên
        topicSelectionBox = new VBox(15);
        topicSelectionBox.setAlignment(javafx.geometry.Pos.CENTER);
        topicSelectionBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        topicSelectionBox.setVisible(false);
        Label lblTopic = new Label("Chọn 1 chủ đề để vẽ:");
        lblTopic.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        topicButtonBox = new HBox(10);
        topicButtonBox.setAlignment(javafx.geometry.Pos.CENTER);
        topicSelectionBox.getChildren().addAll(lblTopic, topicButtonBox);

        StackPane stack = new StackPane(root, topicSelectionBox);
        scene = new Scene(stack, 1100, 650);
        return scene;
    }
    
    private VBox topicSelectionBox;
    private HBox topicButtonBox;
    
    public Scene getScene() {
        return scene;
    }

    private void setupCanvasEvents() {
        canvas.setOnMousePressed(e -> {
            if (!isMyTurnToDraw) return;
            currentXPoints.clear();
            currentYPoints.clear();
            gc.setStroke(currentColor);
            gc.setLineWidth(currentWidth);
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
            currentXPoints.add(e.getX());
            currentYPoints.add(e.getY());
        });

        canvas.setOnMouseDragged(e -> {
            if (!isMyTurnToDraw) return;
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
            currentXPoints.add(e.getX());
            currentYPoints.add(e.getY());
            
            // Gửi batch nhỏ nếu cần, ở đây gửi theo batch mỗi 10 điểm
            if (currentXPoints.size() >= 10) {
                sendStrokeBatch();
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (!isMyTurnToDraw) return;
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
            gc.closePath();
            currentXPoints.add(e.getX());
            currentYPoints.add(e.getY());
            sendStrokeBatch();
        });
    }

    private void sendStrokeBatch() {
        if (currentXPoints.isEmpty()) return;
        
        StrokeDTO dto = new StrokeDTO();
        dto.setTool("BRUSH");
        dto.setColor(toHexString(currentColor));
        dto.setWidth(currentWidth);
        dto.setxPoints(new ArrayList<>(currentXPoints));
        dto.setyPoints(new ArrayList<>(currentYPoints));
        
        Envelope env = new Envelope(MessageType.STROKE_BATCH);
        env.put("stroke", dto);
        app.getWsClient().send(env);
        
        currentXPoints.clear();
        currentYPoints.clear();
    }
    
    private void clearCanvas() {
        if (!isMyTurnToDraw) return;
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        StrokeDTO dto = new StrokeDTO();
        dto.setTool("CLEAR");
        Envelope env = new Envelope(MessageType.STROKE_BATCH);
        env.put("stroke", dto);
        app.getWsClient().send(env);
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    public void handleMessage(Envelope env) {
        Platform.runLater(() -> {
            switch (env.getType()) {
                case TOPIC_OPTIONS:
                    topicButtonBox.getChildren().clear();
                    if (env.getData().has("topics")) {
                        env.getData().get("topics").forEach(tNode -> {
                            String topic = tNode.asText();
                            Button btn = new Button(topic);
                            btn.setStyle("-fx-font-size: 18px; -fx-padding: 10 20; -fx-background-color: white; -fx-text-fill: #F5B800; -fx-font-weight: bold;");
                            btn.setOnAction(e -> {
                                Envelope selEnv = new Envelope(MessageType.TOPIC_SELECT);
                                selEnv.put("topic", topic);
                                app.getWsClient().send(selEnv);
                                topicSelectionBox.setVisible(false);
                                topInfoLabel.setText("Đã chọn chủ đề: " + topic);
                            });
                            topicButtonBox.getChildren().add(btn);
                        });
                        topicSelectionBox.setVisible(true);
                        topInfoLabel.setText("Hãy chọn chủ đề! Thời gian: " + env.getInt("timeLimit", 10) + "s");
                    }
                    break;
                case TOPIC_CONFIRMED:
                    topicSelectionBox.setVisible(false);
                    topInfoLabel.setText("Đã xác nhận chủ đề: " + env.getString("topic"));
                    break;
                case ROUND_RESULT:
                    topInfoLabel.setText("Hết giờ! Bức tranh là: " + env.getString("topic") + 
                                         " | Người vẽ (+ " + env.getInt("painterPoints", 0) + " điểm)");
                    break;
                case ROOM_CHAT_RECEIVE:
                case ROOM_CHAT:
                    chatMessages.add(env.getSender() + ": " + env.getContent());
                    break;
                case DRAW_PHASE_START:
                    isMyTurnToDraw = true;
                    topInfoLabel.setText("Tới lượt bạn vẽ! Chủ đề: " + env.getString("word"));
                    break;
                case GUESS_PHASE_START:
                    isMyTurnToDraw = false;
                    topInfoLabel.setText("Người khác đang vẽ! Gợi ý: " + env.getString("wordHint"));
                    break;
                case STROKE_BATCH:
                    if (!isMyTurnToDraw) {
                        try {
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            StrokeDTO stroke = mapper.convertValue(env.getData().get("stroke"), StrokeDTO.class);
                            drawStrokeFromNetwork(stroke);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case ROOM_UPDATE:
                    // Cập nhật người chơi
                    if (env.getData().has("players")) {
                        playerList.clear();
                        env.getData().get("players").forEach(p -> {
                            playerList.add(p.get("username").asText() + " (" + p.get("score").asInt() + " điểm)");
                        });
                    }
                    break;
                case MATCH_SUMMARY:
                    topInfoLabel.setText("Game Over!");
                    break;
                case TIMER_UPDATE:
                    int time = env.getInt("timeLeft", 0);
                    // Cập nhật lên UI (có thể thêm label thời gian riêng)
                    break;
                default:
                    break;
            }
        });
    }

    private void drawStrokeFromNetwork(StrokeDTO stroke) {
        if ("CLEAR".equals(stroke.getTool())) {
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            return;
        }
        
        List<Double> xs = stroke.getxPoints();
        List<Double> ys = stroke.getyPoints();
        if (xs == null || xs.isEmpty()) return;

        gc.setStroke(Color.web(stroke.getColor()));
        gc.setLineWidth(stroke.getWidth());
        gc.beginPath();
        gc.moveTo(xs.get(0), ys.get(0));
        for (int i = 1; i < xs.size(); i++) {
            gc.lineTo(xs.get(i), ys.get(i));
        }
        gc.stroke();
        gc.closePath();
    }
}
