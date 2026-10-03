package com.nhom8.vehinhdoany.protocol;

import java.util.HashMap;
import java.util.Map;


public class Message {

    /** Loại thông điệp, quyết định cách xử lý phía nhận */
    private MessageType type;

    /** Bảng dữ liệu key-value chứa các tham số đi kèm thông điệp */
    private Map<String, Object> data;

    /** Tên người gửi (username) */
    private String sender;

    /** Cờ đánh dấu kết quả xử lý: true = thành công, false = thất bại */
    private boolean success;

    /** Nội dung văn bản chính (đáp án, thông báo lỗi, v.v.) */
    private String content;

    public Message() {
        this.data = new HashMap<>();
    }

    public Message(MessageType type) {
        this.type = type;
        this.data = new HashMap<>();
    }

    public Message(MessageType type, String sender) {
        this.type = type;
        this.sender = sender;
        this.data = new HashMap<>();
    }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public Map<String, Object> getData() {
        if (data == null) data = new HashMap<>();
        return data;
    }
    public void setData(Map<String, Object> data) { this.data = data; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Message put(String key, Object value) {
        getData().put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Object value = getData().get(key);
        if (value == null) return null;
        if (value instanceof Double) {
            Double d = (Double) value;
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return (T) Integer.valueOf(d.intValue());
            }
        }
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    public boolean hasKey(String key) {
        return getData().containsKey(key);
    }

    public static Message success(MessageType type, String content) {
        Message msg = new Message(type);
        msg.setSuccess(true);
        msg.setContent(content);
        return msg;
    }

    public static Message error(MessageType type, String errorMessage) {
        Message msg = new Message(type);
        msg.setSuccess(false);
        msg.setContent(errorMessage);
        return msg;
    }

    public static Message systemError(String errorMessage) {
        return error(MessageType.ERROR, errorMessage);
    }

    @Override
    public String toString() {
        return "Message{type=" + type + ", sender='" + sender + "', success=" + success +
                ", content='" + content + "', dataKeys=" + getData().keySet() + '}';
    }
}
