package com.nhom8.client.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhom8.client.MainApplication;
import com.nhom8.common.message.Envelope;
import com.nhom8.common.message.MessageType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;


/**
 * Hộp thoại Hồ sơ & Chỉnh sửa thông tin cá nhân (Profile & Match History).
 * Phong cách Sketch Doodle vẽ tay đồng bộ toàn hệ thống.
 */
public class ProfileDialog {

    private final MainApplication app;
    private ModalOverlay overlay;
    private BorderPane contentRoot;

    // Tab 1: Profile & Edit
    private Label usernameLbl;
    private Label scoreLbl;
    private Label statsLbl;
    private Label createdAtLbl;
    private TextField displayNameField;
    private PasswordField oldPassField;
    private PasswordField newPassField;
    private PasswordField confirmPassField;
    private AvatarEditor avatarEditor;

    // Tab 2: Match History
    public static class MatchHistoryEntry {
        public int matchId;
        public String roomId;
        public String startedAt;
        public int pointsDrawn;
        public int pointsGuess;
        public int pointsAdjustment;
        public int scoringVersion;
        public int totalPoints;
        public int rank;
        public int totalPlayers;
    }

    private final ObservableList<MatchHistoryEntry> matchHistoryList = FXCollections.observableArrayList();
    private ListView<MatchHistoryEntry> historyListView;

    public ProfileDialog(MainApplication app) {
        this.app = app;
        initUI();
    }

    private void initUI() {
        BorderPane root = new BorderPane();
        contentRoot = root;
        root.setStyle("-fx-background-color: #FDFBF7;");

        // --- TOP BAR ---
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 20, 12, 20));
        topBar.setStyle("-fx-background-color: #F5C800; -fx-border-color: -fx-ink; -fx-border-width: 0 0 2 0;");

        Label titleLbl = new Label("🎨 TRANG CÁ NHÂN & THỐNG KÊ CHIẾN TÍCH");
        titleLbl.setStyle("-fx-font-family: 'Amatic SC'; -fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: -fx-ink;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✖ Đóng");
        closeBtn.getStyleClass().add("pencil-button-sm");
        closeBtn.setOnAction(e -> close());

        topBar.getChildren().addAll(titleLbl, spacer, closeBtn);
        root.setTop(topBar);

        // --- CENTER: TABS ---
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(18));

        HBox tabHeader = new HBox(10);
        tabHeader.setAlignment(Pos.CENTER);

        Button tabEditBtn = new Button("✏ Thông tin & Đổi mật khẩu");
        Button tabHistoryBtn = new Button("📜 Lịch sử trận đấu");

        tabEditBtn.setMaxWidth(Double.MAX_VALUE);
        tabHistoryBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tabEditBtn, Priority.ALWAYS);
        HBox.setHgrow(tabHistoryBtn, Priority.ALWAYS);

        tabHeader.getChildren().addAll(tabEditBtn, tabHistoryBtn);

        StackPane contentStack = new StackPane();
        VBox.setVgrow(contentStack, Priority.ALWAYS);

        VBox editTabBox = createEditTab();
        VBox historyTabBox = createHistoryTab();

        contentStack.getChildren().addAll(editTabBox, historyTabBox);

        Runnable showEditTab = () -> {
            styleActiveTab(tabEditBtn);
            styleInactiveTab(tabHistoryBtn);
            editTabBox.setVisible(true);
            historyTabBox.setVisible(false);
            requestProfileData();
        };

        Runnable showHistoryTab = () -> {
            styleInactiveTab(tabEditBtn);
            styleActiveTab(tabHistoryBtn);
            editTabBox.setVisible(false);
            historyTabBox.setVisible(true);
            requestMatchHistory();
        };

        tabEditBtn.setOnAction(e -> showEditTab.run());
        tabHistoryBtn.setOnAction(e -> showHistoryTab.run());

        showEditTab.run();

        centerBox.getChildren().addAll(tabHeader, contentStack);
        root.setCenter(centerBox);

    }

    private void styleActiveTab(Button btn) {
        btn.setStyle("-fx-background-color: -fx-ink; -fx-text-fill: -fx-primary; -fx-font-family: 'Patrick Hand'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-color: -fx-ink; -fx-border-width: 2; -fx-cursor: hand; -fx-padding: 6 12;");
    }

    private void styleInactiveTab(Button btn) {
        btn.setStyle("-fx-background-color: white; -fx-text-fill: -fx-ink; -fx-font-family: 'Patrick Hand'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-color: -fx-ink; -fx-border-width: 2; -fx-cursor: hand; -fx-padding: 6 12;");
    }

    // --- TAB 1: EDIT PROFILE & PASSWORD ---
    private VBox createEditTab() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        VBox formContainer = new VBox(16);
        formContainer.setPadding(new Insets(10));

        // 1. Thẻ tổng quan người chơi
        VBox overviewCard = new VBox(10);
        overviewCard.getStyleClass().add("speech-box-white");
        overviewCard.setPadding(new Insets(14));

        Label ovTitle = new Label("Thông tin tổng quan:");
        ovTitle.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -fx-ink;");

        HBox infoRow = new HBox(20);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        usernameLbl = new Label("👤 Tài khoản: @" + app.getCurrentUsername());
        usernameLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -fx-ink;");

        scoreLbl = new Label("🏆 Điểm tích lũy: -- đ");
        scoreLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #B78103;");

        statsLbl = new Label("⚔️ 0 trận • 🥇 0 thắng");
        statsLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 15px; -fx-text-fill: #555555;");

        createdAtLbl = new Label("");
        createdAtLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 13px; -fx-text-fill: #777777;");

        infoRow.getChildren().addAll(usernameLbl, scoreLbl, statsLbl);

        avatarEditor = new AvatarEditor(app.getPrimaryStage(), this::sendUpdateAvatar);
        overviewCard.getChildren().addAll(ovTitle, infoRow, createdAtLbl, avatarEditor);

        // 2. Thẻ Đổi tên hiển thị
        VBox nameCard = new VBox(10);
        nameCard.getStyleClass().add("speech-box-white");
        nameCard.setPadding(new Insets(14));

        Label nameTitle = new Label("✏ Đổi tên hiển thị trong game:");
        nameTitle.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -fx-ink;");

        HBox nameInputRow = new HBox(10);
        nameInputRow.setAlignment(Pos.CENTER_LEFT);

        displayNameField = new TextField(app.getCurrentDisplayName());
        displayNameField.setPromptText("Nhập tên hiển thị mới (2-30 ký tự)");
        displayNameField.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 16px;");
        HBox.setHgrow(displayNameField, Priority.ALWAYS);

        Button saveNameBtn = new Button("💾 Lưu tên hiển thị");
        saveNameBtn.getStyleClass().add("pencil-button-sm");
        saveNameBtn.setOnAction(e -> sendUpdateDisplayName());

        nameInputRow.getChildren().addAll(displayNameField, saveNameBtn);
        nameCard.getChildren().addAll(nameTitle, nameInputRow);

        // 3. Thẻ Đổi mật khẩu
        VBox passCard = new VBox(10);
        passCard.getStyleClass().add("speech-box-white");
        passCard.setPadding(new Insets(14));

        Label passTitle = new Label("🔑 Đổi mật khẩu tài khoản:");
        passTitle.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -fx-ink;");

        GridPane passGrid = new GridPane();
        passGrid.setHgap(10);
        passGrid.setVgap(8);

        Label oldLbl = new Label("Mật khẩu hiện tại:");
        oldLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 15px; -fx-font-weight: bold;");
        oldPassField = new PasswordField();
        oldPassField.setPromptText("Nhập mật khẩu đang dùng");
        oldPassField.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 15px;");

        Label newLbl = new Label("Mật khẩu mới:");
        newLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 15px; -fx-font-weight: bold;");
        newPassField = new PasswordField();
        newPassField.setPromptText("Mật khẩu mới (ít nhất 4 ký tự)");
        newPassField.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 15px;");

        Label confirmLbl = new Label("Xác nhận mật khẩu mới:");
        confirmLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 15px; -fx-font-weight: bold;");
        confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Nhập lại mật khẩu mới");
        confirmPassField.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 15px;");

        passGrid.add(oldLbl, 0, 0);
        passGrid.add(oldPassField, 1, 0);
        passGrid.add(newLbl, 0, 1);
        passGrid.add(newPassField, 1, 1);
        passGrid.add(confirmLbl, 0, 2);
        passGrid.add(confirmPassField, 1, 2);
        GridPane.setHgrow(oldPassField, Priority.ALWAYS);
        GridPane.setHgrow(newPassField, Priority.ALWAYS);
        GridPane.setHgrow(confirmPassField, Priority.ALWAYS);

        Button savePassBtn = new Button("🔐 Cập nhật mật khẩu mới");
        savePassBtn.getStyleClass().add("pencil-button-sm");
        savePassBtn.setOnAction(e -> sendUpdatePassword());

        passCard.getChildren().addAll(passTitle, passGrid, savePassBtn);

        formContainer.getChildren().addAll(overviewCard, nameCard, passCard);
        scroll.setContent(formContainer);
        box.getChildren().add(scroll);
        return box;
    }

    // --- TAB 2: MATCH HISTORY ---
    private VBox createHistoryTab() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(box, Priority.ALWAYS);

        HBox actionBar = new HBox(10);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Label histTitle = new Label("Các trận đấu gần đây:");
        histTitle.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -fx-ink;");

        actionBar.getChildren().add(histTitle);

        historyListView = new ListView<>(matchHistoryList);
        VBox.setVgrow(historyListView, Priority.ALWAYS);
        historyListView.setMaxHeight(Double.MAX_VALUE);
        historyListView.setPlaceholder(new Label("Chưa có lịch sử đấu nào. Hãy tham gia trận đấu để ghi lại chiến tích!"));
        historyListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MatchHistoryEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox card = new HBox(12);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setPadding(new Insets(10, 14, 10, 14));
                    card.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8;");

                    // Huy hiệu thứ hạng
                    Label rankBadge = new Label();
                    if (item.rank == 1) {
                        rankBadge.setText("🥇 HẠNG 1");
                        rankBadge.setStyle("-fx-background-color: #FEF08A; -fx-text-fill: #854D0E; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 8;");
                    } else if (item.rank == 2) {
                        rankBadge.setText("🥈 HẠNG 2");
                        rankBadge.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 8;");
                    } else if (item.rank == 3) {
                        rankBadge.setText("🥉 HẠNG 3");
                        rankBadge.setStyle("-fx-background-color: #FED7AA; -fx-text-fill: #9A3412; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 8;");
                    } else {
                        rankBadge.setText("🎖 HẠNG " + item.rank + "/" + item.totalPlayers);
                        rankBadge.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 8;");
                    }

                    VBox infoBox = new VBox(2);
                    Label roomLbl = new Label("Phòng #" + item.roomId + " (Trận #" + item.matchId + ")");
                    roomLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -fx-ink;");

                    Label timeLbl = new Label("⏱ " + (item.startedAt != null ? item.startedAt : ""));
                    timeLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 13px; -fx-text-fill: #64748B;");
                    infoBox.getChildren().addAll(roomLbl, timeLbl);

                    Region sp2 = new Region();
                    HBox.setHgrow(sp2, Priority.ALWAYS);

                    VBox scoreBox = new VBox(2);
                    scoreBox.setAlignment(Pos.CENTER_RIGHT);
                    Label totalScoreLbl = new Label(item.totalPoints + " điểm");
                    totalScoreLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #059669;");

                    Label breakdownLbl = new Label(String.format("Vẽ: %+d • Đoán: %+d • Thưởng/phạt: %+d • Luật v%d",
                            item.pointsDrawn, item.pointsGuess, item.pointsAdjustment, item.scoringVersion));
                    breakdownLbl.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 12px; -fx-text-fill: #64748B;");
                    scoreBox.getChildren().addAll(totalScoreLbl, breakdownLbl);

                    card.getChildren().addAll(rankBadge, infoBox, sp2, scoreBox);
                    setGraphic(card);
                    setText(null);
                }
            }
        });

        box.getChildren().addAll(actionBar, historyListView);
        return box;
    }

    // --- ACTIONS & NETWORKING ---
    public void show() {
        if (overlay == null || !overlay.isShowing()) {
            overlay = ModalOverlay.show(app.getPrimaryStage(), contentRoot, 760, 680, avatarEditor::discard);
        } else {
            overlay.bringToFront();
        }
        requestProfileData();
    }

    public void toFront() {
        if (overlay != null) overlay.bringToFront();
    }

    private void close() {
        if (overlay != null) overlay.close();
    }

    private void requestProfileData() {
        if (app.getWsClient() != null && app.getWsClient().isConnected()) {
            Envelope env = new Envelope(MessageType.PROFILE_REQUEST);
            env.put("username", app.getCurrentUsername());
            app.getWsClient().send(env);
        }
    }

    private void requestMatchHistory() {
        if (app.getWsClient() != null && app.getWsClient().isConnected()) {
            Envelope env = new Envelope(MessageType.MATCH_HISTORY_REQUEST);
            env.put("username", app.getCurrentUsername());
            app.getWsClient().send(env);
        }
    }

    private void sendUpdateDisplayName() {
        String newName = displayNameField.getText();
        if (newName == null || newName.trim().length() < 2 || newName.trim().length() > 30) {
            DoodleDialog.warning(app.getPrimaryStage(), "Tên không hợp lệ", "Tên hiển thị phải từ 2 đến 30 ký tự!");
            return;
        }
        if (app.getWsClient() != null && app.getWsClient().isConnected()) {
            Envelope env = new Envelope(MessageType.UPDATE_PROFILE_REQUEST);
            env.put("newDisplayName", newName.trim());
            app.getWsClient().send(env);
        }
    }

    private void sendUpdateAvatar(String avatarId) {
        if (app.getWsClient() != null && app.getWsClient().isConnected()) {
            Envelope env = new Envelope(MessageType.UPDATE_PROFILE_REQUEST);
            env.put("newAvatar", avatarId);
            app.getWsClient().send(env);
        } else {
            avatarEditor.saveFailed("Mất kết nối máy chủ. Vui lòng kết nối lại rồi lưu ảnh.");
        }
    }

    private void sendUpdatePassword() {
        String oldPass = oldPassField.getText();
        String newPass = newPassField.getText();
        String confirmPass = confirmPassField.getText();

        if (oldPass == null || oldPass.isEmpty()) {
            DoodleDialog.warning(app.getPrimaryStage(), "Thiếu thông tin", "Vui lòng nhập mật khẩu hiện tại!");
            return;
        }
        if (newPass == null || newPass.length() < 4) {
            DoodleDialog.warning(app.getPrimaryStage(), "Mật khẩu quá ngắn", "Mật khẩu mới phải có ít nhất 4 ký tự!");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            DoodleDialog.warning(app.getPrimaryStage(), "Mật khẩu không khớp", "Xác nhận mật khẩu mới không trùng khớp!");
            return;
        }

        if (app.getWsClient() != null && app.getWsClient().isConnected()) {
            Envelope env = new Envelope(MessageType.UPDATE_PROFILE_REQUEST);
            env.put("oldPassword", oldPass);
            env.put("newPassword", newPass);
            app.getWsClient().send(env);
        }
    }

    // --- MESSAGE HANDLERS ---
    public void handleProfileResponse(Envelope env) {
        Platform.runLater(() -> {
            if (env.isSuccess() && env.getData() != null) {
                String dName = env.getString("displayName");
                if (dName != null && displayNameField != null) {
                    displayNameField.setText(dName);
                }
                int score = env.getInt("rankingScore", 0);
                if (scoreLbl != null) {
                    scoreLbl.setText("🏆 Điểm tích lũy: " + score + " đ");
                }
                int matches = env.getInt("totalMatches", 0);
                int wins = env.getInt("winCount", 0);
                if (statsLbl != null) {
                    statsLbl.setText("⚔️ " + matches + " trận • 🥇 " + wins + " chiến thắng");
                }
                String created = env.getString("createdAt");
                if (created != null && createdAtLbl != null && !created.isEmpty()) {
                    String cleanDate = created.contains("T") ? created.substring(0, created.indexOf("T")) : created;
                    createdAtLbl.setText("📅 Ngày tham gia: " + cleanDate);
                }
                String av = env.getString("avatar");
                if (av != null) {
                    avatarEditor.loadSaved(av);
                }
            }
        });
    }

    public void handleUpdateProfileResponse(Envelope env) {
        Platform.runLater(() -> {
            if (env.isSuccess()) {
                if (avatarEditor.isSaving() && env.getBoolean("avatarUpdated", false)) avatarEditor.saved(env.getString("avatar"));
                else avatarEditor.loadSaved(env.getString("avatar"));
                DoodleDialog.showToast(app.getPrimaryStage(), "✔ " + (env.getContent() != null ? env.getContent() : "Cập nhật thành công!"));
                if (oldPassField != null) oldPassField.clear();
                if (newPassField != null) newPassField.clear();
                if (confirmPassField != null) confirmPassField.clear();

                String updatedDisplayName = env.getString("displayName");
                if (updatedDisplayName != null && !updatedDisplayName.isEmpty()) {
                    app.setCurrentDisplayName(updatedDisplayName);
                }
            } else {
                if (avatarEditor.isSaving() && env.getBoolean("avatarUpdated", false)) avatarEditor.saveFailed("Lưu thất bại. Bạn có thể chỉnh ảnh và thử lại.");
                DoodleDialog.error(app.getPrimaryStage(), "Cập nhật thất bại", env.getContent() != null ? env.getContent() : "Không thể cập nhật thông tin.");
            }
        });
    }

    public void handleMatchHistoryResponse(Envelope env) {
        Platform.runLater(() -> {
            matchHistoryList.clear();
            if (env.getData() != null && env.getData().has("history")) {
                JsonNode histNode = env.getData().get("history");
                histNode.forEach(h -> {
                    MatchHistoryEntry entry = new MatchHistoryEntry();
                    entry.matchId = h.has("matchId") ? h.get("matchId").asInt() : 0;
                    entry.roomId = h.has("roomId") ? h.get("roomId").asText() : "";
                    entry.startedAt = h.has("startedAt") ? h.get("startedAt").asText() : "";
                    entry.pointsDrawn = h.has("pointsDrawn") ? h.get("pointsDrawn").asInt() : 0;
                    entry.pointsGuess = h.has("pointsGuess") ? h.get("pointsGuess").asInt() : 0;
                    entry.pointsAdjustment = h.path("pointsAdjustment").asInt(0);
                    entry.scoringVersion = h.path("scoringVersion").asInt(1);
                    entry.totalPoints = h.has("totalPoints") ? h.get("totalPoints").asInt() : 0;
                    entry.rank = h.has("rank") ? h.get("rank").asInt() : 1;
                    entry.totalPlayers = h.has("totalPlayers") ? h.get("totalPlayers").asInt() : 1;
                    matchHistoryList.add(entry);
                });
            }
        });
    }
}
