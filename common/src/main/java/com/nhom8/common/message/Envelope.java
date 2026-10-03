package com.nhom8.common.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Lớp phong bì thông điệp (Envelope) — mọi tin nhắn WebSocket
 * đều được đóng gói dưới dạng JSON theo cấu trúc này.
 *
 * Cấu trúc JSON:
 * {
 *   "type": "LOGIN",
 *   "sender": "username",
 *   "success": true,
 *   "content": "Đăng nhập thành công",
 *   "data": { ... }
 * }
 *
 * Tương thích ngược với cấu trúc Message cũ (type, sender, success, content, data).
 */
public class Envelope {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Loại thông điệp */
    private MessageType type;

    /** Tên người gửi (username) */
    private String sender;

    /** Cờ kết quả: true = thành công */
    private boolean success;

    /** Nội dung text chính (thông báo, lỗi, đáp án...) */
    private String content;

    /** Dữ liệu JSON dạng key-value (tương thích Map<String, Object> cũ) */
    private JsonNode data;

    public Envelope() {
        this.data = MAPPER.createObjectNode();
    }

    public Envelope(MessageType type) {
        this.type = type;
        this.data = MAPPER.createObjectNode();
    }

    public Envelope(MessageType type, String sender) {
        this.type = type;
        this.sender = sender;
        this.data = MAPPER.createObjectNode();
    }

    // === Factory methods ===

    public static Envelope success(MessageType type, String content) {
        Envelope env = new Envelope(type);
        env.setSuccess(true);
        env.setContent(content);
        return env;
    }

    public static Envelope error(MessageType type, String errorMessage) {
        Envelope env = new Envelope(type);
        env.setSuccess(false);
        env.setContent(errorMessage);
        return env;
    }

    public static Envelope error(String errorMessage) {
        return error(MessageType.ERROR, errorMessage);
    }

    // === Fluent data setters ===

    public Envelope put(String key, Object value) {
        if (data == null || !data.isObject()) {
            data = MAPPER.createObjectNode();
        }
        ((ObjectNode) data).putPOJO(key, value);
        return this;
    }

    public Envelope putString(String key, String value) {
        if (data == null || !data.isObject()) {
            data = MAPPER.createObjectNode();
        }
        ((ObjectNode) data).put(key, value);
        return this;
    }

    public Envelope putInt(String key, int value) {
        if (data == null || !data.isObject()) {
            data = MAPPER.createObjectNode();
        }
        ((ObjectNode) data).put(key, value);
        return this;
    }

    public Envelope putBoolean(String key, boolean value) {
        if (data == null || !data.isObject()) {
            data = MAPPER.createObjectNode();
        }
        ((ObjectNode) data).put(key, value);
        return this;
    }

    // === Serialization ===

    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi serialize Envelope: " + e.getMessage(), e);
        }
    }

    public static Envelope fromJson(String json) {
        try {
            return MAPPER.readValue(json, Envelope.class);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi parse Envelope: " + e.getMessage(), e);
        }
    }

    // === Getters / Setters ===

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public JsonNode getData() { return data; }
    public void setData(JsonNode data) { this.data = data; }

    /**
     * Tiện ích lấy giá trị String từ data.
     */
    public String getString(String key) {
        if (data != null && data.has(key)) {
            return data.get(key).asText(null);
        }
        return null;
    }

    /**
     * Tiện ích lấy giá trị int từ data.
     */
    public int getInt(String key, int defaultValue) {
        if (data != null && data.has(key)) {
            return data.get(key).asInt(defaultValue);
        }
        return defaultValue;
    }

    /**
     * Tiện ích lấy giá trị boolean từ data.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        if (data != null && data.has(key)) {
            return data.get(key).asBoolean(defaultValue);
        }
        return defaultValue;
    }

    /**
     * Tương thích ngược với hàm get() của Message cũ.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (data != null && data.has(key)) {
            JsonNode node = data.get(key);
            if (node.isTextual()) return (T) node.asText();
            if (node.isInt()) return (T) Integer.valueOf(node.asInt());
            if (node.isBoolean()) return (T) Boolean.valueOf(node.asBoolean());
            if (node.isDouble()) return (T) Double.valueOf(node.asDouble());
            if (node.isLong()) return (T) Long.valueOf(node.asLong());
            return (T) node;
        }
        return null;
    }

    /**
     * Tương thích ngược với getOrDefault.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public String toString() {
        return "Envelope{type=" + type + ", sender='" + sender + "', success=" + success +
                ", content='" + content + "'}";
    }
}
