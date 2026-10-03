package com.nhom8.common.message;

/**
 * Liệt kê toàn bộ các loại thông điệp (message type) trong giao thức
 * truyền thông giữa Client (JavaFX) và Server thông qua WebSocket JSON.
 *
 * Module: common (dùng chung cho cả server và client).
 */
public enum MessageType {

    // === Xác thực tài khoản ===
    LOGIN,
    LOGIN_RESULT,
    REGISTER,
    LOGOUT,

    // === Thông tin cá nhân ===
    UPDATE_PROFILE,
    PROFILE_RESULT,

    // === Sảnh chờ (Lobby) ===
    ONLINE_LIST,
    PLAYER_LIST,
    STATUS_UPDATE,
    PLAYER_DISCONNECTED,

    // === Quản lý phòng chơi ===
    ROOM_CREATE,
    ROOM_LIST,
    ROOM_JOIN,
    JOIN_ROOM,
    ROOM_LEAVE,
    ROOM_UPDATE,
    ROOM_RESPONSE,
    READY,
    HOST_CHANGED,
    ROOM_DISBANDED,
    ROOM_DISSOLVED,
    ROOM_CHAT,
    ROOM_CHAT_RECEIVE,
    ROOM_KICK,
    ROOM_SETTINGS,
    ROOM_UPDATE_SETTINGS,

    // === Mời vào phòng ===
    ROOM_INVITE_SEND,
    ROOM_INVITE_NOTIFY,
    ROOM_INVITE_RESPOND,

    // === Luồng trò chơi ===
    MATCH_START,
    GAME_START,
    TOPIC_OPTIONS,
    TOPIC_SELECT,
    TOPIC_CONFIRMED,
    DRAW_PHASE_START,
    DRAW_START,
    STROKE_BATCH,
    DRAW_SUBMIT,
    GUESS_PHASE_START,
    GUESS_START,
    PAINTING_DISPLAY,
    GUESS_SUBMIT,
    GUESS_RESULT,
    GUESS_LOG,
    GUESS_PHASE_END,
    MATCH_SUMMARY,
    GAME_RESULT,

    // === Đồng hồ đếm ngược ===
    TIMER_UPDATE,

    // === Tính điểm và tổng kết ===
    ROUND_RESULT,

    // === Bảng xếp hạng ===
    LEADERBOARD,
    GUESS_RANKING_UPDATE,

    // === Hệ thống bạn bè ===
    FRIEND_REQUEST,
    FRIEND_RESPONSE,
    FRIEND_LIST,
    FRIEND_REMOVE,
    FRIEND_UPDATE,
    FRIEND_PENDING_LIST,
    FRIEND_REQUEST_NOTIFY,

    // === Tìm kiếm người chơi ===
    SEARCH_PLAYER,

    // === Nhắn tin riêng (Chat 1-1) ===
    CHAT_PRIVATE,
    CHAT_HISTORY,
    CHAT_HISTORY_RESPONSE,
    DIRECT_MESSAGE_RECEIVE,

    // === Lịch sử trận đấu ===
    MATCH_HISTORY,

    // === Hồ sơ cá nhân ===
    PROFILE,

    // === Admin ===
    ADMIN_USERS_RESPONSE,
    ADMIN_UPDATE_SCORE,
    ADMIN_DISSOLVE_ROOM,

    // === Lỗi chung ===
    ERROR
}
