package com.nhom8.client.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhom8.client.MainApplication;
import com.nhom8.common.message.Envelope;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.BiConsumer;

/** Hộp thư các cuộc trò chuyện riêng, kèm số tin chưa đọc. */
public final class InboxDialog {
    public static final class Conversation {
        public String username;
        public String displayName;
        public String preview;
        public String timestamp;
        public int unread;
    }

    private final Stage stage = new Stage();
    private final ObservableList<Conversation> conversations = FXCollections.observableArrayList();
    private final BiConsumer<String, String> openChat;
    private final Label summary = new Label("Đang tải hộp thư…");
    private final ListView<Conversation> list = new ListView<>(conversations);

    public InboxDialog(MainApplication app, BiConsumer<String, String> openChat) {
        this.openChat = openChat;
        stage.initOwner(app.getPrimaryStage());
        stage.initModality(Modality.NONE);
        stage.setTitle("💬 Hộp thư");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #FDFBF7;");
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color: #F5C800; -fx-border-color: -fx-ink; -fx-border-width: 0 0 2 0;");
        VBox heading = new VBox(2);
        Label title = new Label("💬 HỘP THƯ");
        title.setStyle("-fx-font-family: 'Amatic SC'; -fx-font-size: 30px; -fx-font-weight: bold;");
        summary.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 14px; -fx-text-fill: #555555;");
        heading.getChildren().addAll(title, summary);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button refresh = new Button("🔄 Làm mới");
        refresh.getStyleClass().add("pencil-button-sm");
        refresh.setOnAction(e -> app.requestChatInbox());
        header.getChildren().addAll(heading, spacer, refresh);

        list.setPlaceholder(new Label("Chưa có cuộc trò chuyện nào."));
        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(9, 10, 9, 10));
                VBox details = new VBox(3);
                Label name = new Label(item.displayName + "  •  @" + item.username);
                name.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 17px; -fx-font-weight: bold;");
                Label preview = new Label(item.preview == null ? "" : item.preview);
                preview.setMaxWidth(290);
                preview.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
                preview.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 14px; -fx-text-fill: #666666;");
                details.getChildren().addAll(name, preview);
                Region fill = new Region();
                HBox.setHgrow(fill, Priority.ALWAYS);
                if (item.unread > 0) {
                    Label badge = new Label(item.unread > 99 ? "99+" : Integer.toString(item.unread));
                    badge.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; -fx-font-family: 'Patrick Hand'; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 14;");
                    row.getChildren().addAll(details, fill, badge);
                } else {
                    Label time = new Label(formatTime(item.timestamp));
                    time.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 12px; -fx-text-fill: #777777;");
                    row.getChildren().addAll(details, fill, time);
                }
                setGraphic(row);
            }
        });
        list.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) openSelected();
        });

        Button open = new Button("MỞ CUỘC TRÒ CHUYỆN");
        open.getStyleClass().add("pencil-button-sm");
        open.setOnAction(e -> openSelected());
        HBox footer = new HBox(open);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 14, 12, 14));
        root.setTop(header);
        root.setCenter(list);
        root.setBottom(footer);
        Scene scene = new Scene(root, 490, 500);
        MainApplication.applyTheme(scene);
        stage.setScene(scene);
    }

    public void show() {
        if (!stage.isShowing()) stage.show();
        stage.toFront();
    }

    public void update(Envelope envelope) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> update(envelope));
            return;
        }
        conversations.clear();
        JsonNode data = envelope.getData();
        if (data != null && data.has("conversations") && data.get("conversations").isArray()) {
            for (JsonNode node : data.get("conversations")) {
                Conversation item = new Conversation();
                item.username = node.path("partnerUsername").asText("");
                item.displayName = node.path("partnerDisplayName").asText(item.username);
                item.preview = (node.path("lastMessageIsSelf").asBoolean(false) ? "Bạn: " : "")
                        + node.path("lastMessage").asText("");
                item.timestamp = node.path("timestamp").asText("");
                item.unread = node.path("unreadCount").asInt(0);
                conversations.add(item);
            }
        }
        summary.setText("" + envelope.getInt("unreadTotal", 0) + " tin chưa đọc • " + conversations.size() + " cuộc trò chuyện");
    }

    private void openSelected() {
        Conversation item = list.getSelectionModel().getSelectedItem();
        if (item == null) return;
        stage.hide();
        openChat.accept(item.username, item.displayName);
    }

    private String formatTime(String timestamp) {
        if (timestamp == null || timestamp.length() < 16) return "";
        return timestamp.substring(11, 16);
    }
}
