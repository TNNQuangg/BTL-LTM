package com.nhom8.client.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

/** Bong bóng chat phân biệt tin nhắn của người dùng với tin nhắn của người khác. */
public final class ChatMessageCell extends ListCell<String> {
    @Override
    protected void updateItem(String message, boolean empty) {
        super.updateItem(message, empty);
        setText(null);
        if (empty || message == null) {
            setGraphic(null);
            setStyle("-fx-background-color: transparent; -fx-padding: 3 6;");
            return;
        }

        boolean mine = message.startsWith("Bạn: ");
        boolean system = message.startsWith("⚡ ") || message.startsWith("🏆 ");
        HBox row = new HBox();
        row.setAlignment(system ? Pos.CENTER : mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        Label bubble = new Label(message);
        bubble.setWrapText(true);
        bubble.setMaxWidth(330);
        bubble.setStyle("-fx-font-family: 'Patrick Hand'; -fx-font-size: 15px; -fx-padding: 7 11;"
                + (system
                ? "-fx-background-color: #FFF3C4; -fx-text-fill: #735D00; -fx-background-radius: 10;"
                : mine
                ? "-fx-background-color: #DCEEFF; -fx-text-fill: #173B5E; -fx-background-radius: 12 12 3 12; -fx-border-color: #9BC7ED; -fx-border-radius: 12 12 3 12;"
                : "-fx-background-color: white; -fx-text-fill: #262626; -fx-background-radius: 12 12 12 3; -fx-border-color: #E4E4E4; -fx-border-radius: 12 12 12 3;"));
        row.getChildren().add(bubble);
        setGraphic(row);
        setStyle("-fx-background-color: transparent; -fx-padding: 3 6;");
    }
}
