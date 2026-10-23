package com.nhom8.client.ui;

import com.nhom8.common.dto.AvatarData;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/** Hiển thị thống nhất ảnh tải lên và các biểu tượng đại diện cũ. */
public final class AvatarView {
    public static final String[] ICONS = {"🎨", "🦊", "🐱", "🐶", "🚀", "👑", "⚡", "🍀"};
    private static final Map<String, Image> CACHE = new LinkedHashMap<>(32, .75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Image> entry) {
            return size() > 64;
        }
    };

    private AvatarView() {}

    public static Image image(String value) {
        if (value == null || !value.startsWith(AvatarData.PNG_PREFIX)
                || value.length() > AvatarData.MAX_TEXT_LENGTH) return null;
        Image cached = CACHE.get(value);
        if (cached != null) return cached;
        try {
            Image image = new Image(new ByteArrayInputStream(Base64.getDecoder()
                    .decode(value.substring(AvatarData.PNG_PREFIX.length()))));
            if (!image.isError() && image.getWidth() > 0 && image.getHeight() > 0) {
                CACHE.put(value, image);
                return image;
            }
        } catch (IllegalArgumentException ignored) {}
        return null;
    }

    public static StackPane create(String value, double size) {
        StackPane pane = new StackPane();
        pane.setMinSize(size, size);
        pane.setPrefSize(size, size);
        pane.setMaxSize(size, size);
        pane.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 999;");
        Image image = image(value);
        if (image != null) {
            double side = Math.min(image.getWidth(), image.getHeight());
            ImageView view = new ImageView(image);
            view.setViewport(new Rectangle2D((image.getWidth() - side) / 2,
                    (image.getHeight() - side) / 2, side, side));
            view.setFitWidth(size);
            view.setFitHeight(size);
            pane.getChildren().add(view);
        } else {
            int index = 0;
            try { index = Math.max(0, Math.min(ICONS.length - 1, Integer.parseInt(value) - 1)); }
            catch (RuntimeException ignored) {}
            Label icon = new Label(ICONS[index]);
            icon.setStyle("-fx-font-family: 'Segoe UI Emoji'; -fx-font-size: " + (size * .52) + "px;");
            pane.getChildren().add(icon);
        }
        pane.setClip(new Circle(size / 2, size / 2, size / 2));
        return pane;
    }
}
