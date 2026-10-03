package com.nhom8.vehinhdoany.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nhom8.vehinhdoany.dto.FriendRequest;
import com.nhom8.vehinhdoany.dto.FriendRespondRequest;
import com.nhom8.vehinhdoany.service.FriendService;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping("")
    public ResponseEntity<?> getFriends(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        response.put("friends", friendService.getFriendList(username));
        response.put("pending", friendService.getPendingRequests(username));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody FriendRequest request) {
        try {
            friendService.sendFriendRequest(request);
            return ResponseEntity.ok(Map.of("message", "Đã gửi lời mời kết bạn."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/respond")
    public ResponseEntity<?> respondRequest(@RequestBody FriendRespondRequest request) {
        try {
            friendService.respondFriendRequest(request);
            return ResponseEntity.ok(Map.of("message", "Phản hồi lời mời thành công."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("")
    public ResponseEntity<?> removeFriend(@RequestParam String myUsername, @RequestParam String friendUsername) {
        friendService.removeFriend(myUsername, friendUsername);
        return ResponseEntity.ok(Map.of("message", "Xóa bạn bè thành công."));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query, @RequestParam String username) {
        return ResponseEntity.ok(friendService.searchUsers(query, username));
    }

    @GetMapping("/chat-threads")
    public ResponseEntity<?> getChatThreads(@RequestParam String username) {
        return ResponseEntity.ok(friendService.getChatThreads(username));
    }
}
