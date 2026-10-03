package com.nhom8.vehinhdoany.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể trận đấu trong hệ thống trò chơi "Vẽ Hình Đoán Ý".
 * Lưu trữ thông tin về phòng chơi, thời điểm bắt đầu và kết thúc trận đấu.
 * Mỗi trận đấu có mối quan hệ một-nhiều với bảng kết quả (MatchResult)
 * và bảng tranh vẽ (Painting).
 */
@Entity
@Table(name = "matches")
public class Match implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Integer matchId;

    @Column(name = "room_id", nullable = false, length = 50)
    private String roomId;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MatchResult> results = new ArrayList<>();

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Painting> paintings = new ArrayList<>();

    public Match() {
    }

    public Match(String roomId) {
        this.roomId = roomId;
        this.startedAt = LocalDateTime.now();
    }

    public Integer getMatchId() { return matchId; }
    public void setMatchId(Integer matchId) { this.matchId = matchId; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public List<MatchResult> getResults() { return results; }
    public void setResults(List<MatchResult> results) { this.results = results; }
    public List<Painting> getPaintings() { return paintings; }
    public void setPaintings(List<Painting> paintings) { this.paintings = paintings; }

    public void endMatch() {
        this.endedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Match{matchId=" + matchId + ", roomId='" + roomId + "', startedAt=" + startedAt + ", endedAt=" + endedAt + '}';
    }
}
