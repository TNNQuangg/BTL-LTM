package com.nhom8.vehinhdoany.service;

import org.springframework.stereotype.Service;

import com.nhom8.vehinhdoany.model.User;
import com.nhom8.vehinhdoany.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaderboardService {

    private final UserRepository userRepository;

    public LeaderboardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> getLeaderboard() {
        List<User> topPlayers = userRepository.findTop50ByOrderByRankingScoreDesc();
        List<Map<String, Object>> leaderboardData = new ArrayList<>();

        int rank = 1;
        for (User user : topPlayers) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", rank++);
            entry.put("username", user.getUsername());
            entry.put("displayName", user.getDisplayName());
            entry.put("rankingScore", user.getRankingScore());
            entry.put("avatar", user.getAvatar());
            leaderboardData.add(entry);
        }
        return leaderboardData;
    }
}
