package com.nhom8.server.model;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Thực thể kết quả trận đấu của từng người chơi.
 * Bảng trung gian (quan hệ nhiều-nhiều) giữa bảng người chơi (users)
 * và bảng trận đấu (matches), lưu trữ điểm số mà mỗi người chơi đạt được.
 */
@Entity
@Table(name = "match_results")
public class MatchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Integer resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "points_drawn")
    private Integer pointsDrawn = 0;

    @Column(name = "points_guess")
    private Integer pointsGuess = 0;

    @Column(name = "points_adjustment", nullable = false, columnDefinition = "integer default 0")
    private Integer pointsAdjustment = 0;

    public MatchResult() {}

    public MatchResult(Match match, User user) {
        this.match = match;
        this.user = user;
    }

    public Integer getResultId() { return resultId; }
    public void setResultId(Integer resultId) { this.resultId = resultId; }
    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Integer getPointsDrawn() { return pointsDrawn; }
    public void setPointsDrawn(Integer pointsDrawn) { this.pointsDrawn = pointsDrawn; }
    public Integer getPointsGuess() { return pointsGuess; }
    public void setPointsGuess(Integer pointsGuess) { this.pointsGuess = pointsGuess; }

    public int getTotalPoints() {
        return getGameplayPoints() + getPointsAdjustment();
    }

    public int getGameplayPoints() { return (pointsDrawn == null ? 0 : pointsDrawn) + (pointsGuess == null ? 0 : pointsGuess); }
    public int getPointsAdjustment() { return pointsAdjustment == null ? 0 : pointsAdjustment; }
    public void addAdjustmentPoints(int points) { pointsAdjustment = getPointsAdjustment() + points; }

    public void addGuessPoints(int points) {
        this.pointsGuess = (this.pointsGuess != null ? this.pointsGuess : 0) + points;
    }

    public void addDrawnPoints(int points) {
        this.pointsDrawn = (this.pointsDrawn != null ? this.pointsDrawn : 0) + points;
    }

    @Override
    public String toString() {
        return "MatchResult{resultId=" + resultId + ", userId=" + (user != null ? user.getUserId() : "null") +
                ", pointsDrawn=" + pointsDrawn + ", pointsGuess=" + pointsGuess + ", total=" + getTotalPoints() + '}';
    }
}
