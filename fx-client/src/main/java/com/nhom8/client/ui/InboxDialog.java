package com.nhom8.client.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhom8.client.MainApplication;
import com.nhom8.common.message.Envelope;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Locale;

/** Two-pane inbox: conversations on the left and the selected chat on the right. */
public final class InboxDialog {
    public static final class Conversation {
        public String username;
        public String displayName;
        public String preview;
        public String timestamp;
        public int unread;
    }

    private final MainApplication app;
    private ModalOverlay overlay;
    private final ObservableList<Conversation> conversations = FXCollections.observableArrayList();
    private final FilteredList<Conversation> filteredConversations = new FilteredList<>(conversations, item -> true);
    private final ListView<Conversation> list = new ListView<>(filteredConversations);
    private final Label summary = new Label("Đang tải hộp thư…");
    private final BorderPane chatHost = new BorderPane();
    private final VBox emptyState = new VBox(8);
    private PrivateChatWindow activeChat;
    private String activeUsername;
    private boolean updatingList;
    private final BorderPane root = new BorderPane();

    public InboxDialog(MainApplication app) {
        this.app = app;
        buildUi();
    }

    private void buildUi() {
        root.setStyle("-fx-background-color: #FDFBF7;");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 18, 12, 18));
        header.setStyle("-fx-background-color: #F5C800; -fx-border-color: -fx-ink; -fx-border-width: 0 0 2 0;");

        VBox heading = new VBox(1);
        Label title = new Label("TIN NHẮN");
        title.setStyle("-fx-font-family: 'Amatic SC'; -fx-font-size: 32px; -fx-font-weight: bold;");
        summary.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 14px; -fx-text-fill: #555555;");
        heading.getChildren().addAll(title, summary);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = new Button("✕");
        close.getStyleClass().add("modal-close-button");
        close.setTooltip(new Tooltip("Đóng tin nhắn (Esc)"));
        close.setOnAction(e -> hide());
        header.getChildren().addAll(heading, spacer, close);

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(14));
        sidebar.setPrefWidth(310);
        sidebar.setMinWidth(260);
        sidebar.setMaxWidth(340);
        sidebar.setStyle("-fx-background-color: #FFFDF7; -fx-border-color: #DED8CB; -fx-border-width: 0 1 0 0;");

        Label conversationsTitle = new Label("CUỘC TRÒ CHUYỆN");
        conversationsTitle.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 17px; -fx-font-weight: bold;");
        TextField search = new TextField();
        search.setPromptText("Tìm người hoặc tin nhắn");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase(Locale.ROOT);
            filteredConversations.setPredicate(item -> query.isEmpty()
                    || safe(item.displayName).toLowerCase(Locale.ROOT).contains(query)
                    || safe(item.username).toLowerCase(Locale.ROOT).contains(query)
                    || safe(item.preview).toLowerCase(Locale.ROOT).contains(query));
        });

        list.setPlaceholder(new Label("Chưa có cuộc trò chuyện nào."));
        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                VBox details = new VBox(3);
                Label name = new Label(safe(item.displayName) + "  ·  @" + safe(item.username));
                name.setMaxWidth(Double.MAX_VALUE);
                name.setTextOverrun(OverrunStyle.ELLIPSIS);
                name.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #24211C;");
                Label preview = new Label(safe(item.preview).isBlank() ? "Bắt đầu trò chuyện" : safe(item.preview));
                preview.setMaxWidth(Double.MAX_VALUE);
                preview.setTextOverrun(OverrunStyle.ELLIPSIS);
                preview.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 13px; -fx-text-fill: #68635A;");
                details.getChildren().addAll(name, preview);

                Region fill = new Region();
                HBox.setHgrow(fill, Priority.ALWAYS);
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(9, 8, 9, 8));
                row.getChildren().addAll(details, fill);
                if (item.unread > 0) {
                    Label unread = new Label(item.unread > 99 ? "99+" : Integer.toString(item.unread));
                    unread.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; -fx-font-family: 'Patrick Hand'; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 2 7; -fx-background-radius: 12;");
                    row.getChildren().add(unread);
                } else {
                    Label time = new Label(formatTime(item.timestamp));
                    time.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 12px; -fx-text-fill: #777777;");
                    row.getChildren().add(time);
                }
                setText(null);
                setGraphic(row);
                updateSelected(isSelected());
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                setStyle(selected
                        ? "-fx-background-color: #FFF0A6; -fx-background-radius: 9;"
                        : "-fx-background-color: transparent;");
            }
        });
        VBox.setVgrow(list, Priority.ALWAYS);
        sidebar.getChildren().addAll(conversationsTitle, search, list);

        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(24));
        Label emptyTitle = new Label("Chọn một cuộc trò chuyện");
        emptyTitle.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #34312A;");
        Label emptyHint = new Label("Tin nhắn và nội dung mới sẽ xuất hiện tại đây.");
        emptyHint.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 16px; -fx-text-fill: #777777;");
        emptyState.getChildren().addAll(emptyTitle, emptyHint);
        chatHost.setCenter(emptyState);
        HBox.setHgrow(chatHost, Priority.ALWAYS);

        HBox body = new HBox(sidebar, chatHost);
        VBox.setVgrow(body, Priority.ALWAYS);
        root.setTop(header);
        root.setCenter(body);
        root.setPrefSize(1080, 660);
        root.setMinSize(640, 420);
        root.setMaxSize(1400, 900);

        list.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, item) -> {
            if (!updatingList && item != null) activate(item, false);
        });
    }

    public void show() {
        if (overlay == null || !overlay.isShowing()) {
            overlay = ModalOverlay.show(app.getPrimaryStage(), root, 1080, 660, null);
        } else {
            overlay.bringToFront();
        }
        if (activeChat != null) Platform.runLater(() -> activeChat.focusInput());
    }

    public void hide() {
        if (overlay != null) overlay.close();
    }

    public boolean isShowing() {
        return overlay != null && overlay.isShowing();
    }

    public String getActiveUsername() {
        return activeUsername;
    }

    public void openConversation(String username, String displayName) {
        if (username == null || username.isBlank()) return;
        Conversation conversation = conversations.stream()
                .filter(item -> username.equalsIgnoreCase(item.username)).findFirst().orElse(null);
        if (conversation == null) {
            conversation = new Conversation();
            conversation.username = username;
            conversation.displayName = safe(displayName).isBlank() ? username : displayName;
            conversation.preview = "";
            conversations.add(0, conversation);
        }
        show();
        selectConversation(conversation);
    }

    private void selectConversation(Conversation conversation) {
        for (int i = 0; i < filteredConversations.size(); i++) {
            if (filteredConversations.get(i) == conversation) {
                list.getSelectionModel().select(i);
                break;
            }
        }
        activate(conversation, false);
    }

    private void activate(Conversation conversation, boolean forceReload) {
        if (conversation == null || conversation.username == null) return;
        boolean sameConversation = conversation.username.equalsIgnoreCase(activeUsername);
        if (sameConversation && activeChat != null && !forceReload) return;
        activeUsername = conversation.username;
        activeChat = new PrivateChatWindow(app, conversation.username, safe(conversation.displayName));
        chatHost.setCenter(activeChat.getView());
        activeChat.requestHistory();
        app.markConversationRead(activeUsername);
    }

    public void update(Envelope envelope) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> update(envelope));
            return;
        }
        String selectedUsername = activeUsername;
        List<Conversation> latest = parseConversations(envelope.getData());
        if (selectedUsername != null && latest.stream().noneMatch(item -> selectedUsername.equalsIgnoreCase(item.username))) {
            Conversation current = new Conversation();
            current.username = selectedUsername;
            current.displayName = activeChat == null ? selectedUsername : activeChat.getTargetDisplayName();
            current.preview = "";
            latest.add(0, current);
        }

        updatingList = true;
        conversations.setAll(latest);
        if (selectedUsername != null) {
            for (int i = 0; i < filteredConversations.size(); i++) {
                if (selectedUsername.equalsIgnoreCase(filteredConversations.get(i).username)) {
                    list.getSelectionModel().select(i);
                    break;
                }
            }
        }
        updatingList = false;
        summary.setText(envelope.getInt("unreadTotal", 0) + " tin chưa đọc  ·  " + conversations.size() + " cuộc trò chuyện");
    }

    private List<Conversation> parseConversations(JsonNode data) {
        ObservableList<Conversation> latest = FXCollections.observableArrayList();
        if (data != null && data.path("conversations").isArray()) {
            for (JsonNode node : data.path("conversations")) {
                Conversation item = new Conversation();
                item.username = node.path("partnerUsername").asText("");
                item.displayName = node.path("partnerDisplayName").asText(item.username);
                item.preview = (node.path("lastMessageIsSelf").asBoolean(false) ? "Bạn: " : "")
                        + node.path("lastMessage").asText("");
                item.timestamp = node.path("timestamp").asText("");
                item.unread = node.path("unreadCount").asInt(0);
                latest.add(item);
            }
        }
        return latest;
    }

    public void handleDirectMessage(Envelope envelope) {
        if (activeChat == null || activeUsername == null) return;
        String sender = envelope.getBoolean("isSelf", false)
                ? envelope.getString("targetUsername") : envelope.getString("senderUsername");
        if (sender != null && sender.equalsIgnoreCase(activeUsername)) {
            activeChat.handleReceiveMessage(envelope);
            if (isShowing()) app.markConversationRead(activeUsername);
        }
    }

    public void handleHistoryResponse(Envelope envelope) {
        if (activeChat == null || activeUsername == null) return;
        String target = envelope.getString("targetUsername");
        if (target != null && target.equalsIgnoreCase(activeUsername)) activeChat.handleHistoryResponse(envelope);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatTime(String timestamp) {
        if (timestamp == null || timestamp.length() < 16) return "";
        return timestamp.substring(11, 16);
    }
}
