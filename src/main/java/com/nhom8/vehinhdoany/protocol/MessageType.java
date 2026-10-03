package com.nhom8.vehinhdoany.protocol;

/**
 * Liệt kê toàn bộ các loại thông điệp (message type) trong giao thức
 * truyền thông giữa Client (React.js) và Server thông qua WebSocket JSON.
 */
public enum MessageType {

    // === Xác thực tài khoản ===
    AUTH,
    LOGIN_REQUEST, LOGIN_RESPONSE,
    REGISTER_REQUEST, REGISTER_RESPONSE,

    // === Thông tin cá nhân ===
    UPDATE_PROFILE_REQUEST, UPDATE_PROFILE_RESPONSE,

    // === Sảnh chờ ===
    PLAYER_LIST, ROOM_LIST,

    // === Quản lý phòng chơi ===
    CREATE_ROOM, JOIN_ROOM, LEAVE_ROOM, READY,
    ROOM_UPDATE, ROOM_RESPONSE,
    ROOM_UPDATE_SETTINGS, ROOM_KICK_PLAYER,
    ROOM_CHAT_SEND, ROOM_CHAT_RECEIVE,

    // === Luồng trò chơi ===
    GAME_START,
    TOPIC_OPTIONS, TOPIC_SELECT, TOPIC_CONFIRMED,
    DRAW_START, DRAW_DATA, DRAW_SUBMIT,
    GUESS_START, PAINTING_DISPLAY, GUESS_SUBMIT, GUESS_RESULT,
    GUESS_RANKING_UPDATE, GUESS_LOG,

    // === Đồng hồ đếm ngược ===
    TIMER_UPDATE,

    // === Tính điểm và tổng kết ===
    ROUND_RESULT, GAME_RESULT,

    // === Bảng xếp hạng ===
    LEADERBOARD_REQUEST, LEADERBOARD_RESPONSE,

    // === Hệ thống ===
    PLAYER_DISCONNECTED, ROOM_DISSOLVED,

    // === Hệ thống bạn bè ===
    FRIEND_REQUEST_SEND,        // Client → Server: gửi lời mời kết bạn
    FRIEND_REQUEST_RESPOND,     // Client → Server: chấp nhận/từ chối lời mời
    FRIEND_REMOVE,              // Client → Server: xóa bạn
    FRIEND_LIST_REQUEST,        // Client → Server: yêu cầu danh sách bạn bè
    FRIEND_LIST_RESPONSE,       // Server → Client: trả danh sách bạn bè
    FRIEND_REQUEST_NOTIFY,      // Server → Client: thông báo có lời mời mới
    FRIEND_PENDING_LIST,        // Server → Client: danh sách lời mời đang chờ
    FRIEND_UPDATE,              // Server → Client: cập nhật trạng thái bạn bè
    SEARCH_USER_REQUEST,        // Client → Server: tìm kiếm user theo tên
    SEARCH_USER_RESPONSE,       // Server → Client: kết quả tìm kiếm

    // === Mời vào phòng ===
    ROOM_INVITE_SEND,           // Client → Server: mời bạn vào phòng
    ROOM_INVITE_NOTIFY,         // Server → Client: thông báo bạn được mời
    ROOM_INVITE_RESPOND,        // Client → Server: chấp nhận/từ chối lời mời phòng

    // === Nhắn tin riêng ===
    DIRECT_MESSAGE_SEND,        // Client -> Server: gửi tin nhắn riêng
    DIRECT_MESSAGE_RECEIVE,     // Server -> Client: nhận tin nhắn riêng
    CHAT_HISTORY_REQUEST,       // Client -> Server: yêu cầu lịch sử chat
    CHAT_HISTORY_RESPONSE,      // Server -> Client: trả lịch sử chat

    // === Admin ===
    ADMIN_GET_USERS, ADMIN_USERS_RESPONSE,
    ADMIN_UPDATE_SCORE, ADMIN_DISSOLVE_ROOM, ADMIN_GET_ROOMS,

    /** Thông điệp lỗi chung */
    ERROR
}
