package com.nhom8.server.service;

import java.util.Collection;

/** Công thức điểm thuần, chỉ nhận các mốc thời gian do máy chủ cung cấp. */
public final class ScoringService {
    public static final int VERSION = 2;
    public static final long GUESS_DURATION_MS = 30_000;
    public static final long DEFAULT_EXPOSURE_MS = 10_000;

    private ScoringService() {}

    public static int calculateGuessScore(long startedAt, long acceptedAt, long durationMs) {
        if (durationMs <= 0 || acceptedAt < startedAt) return 0;
        long elapsedMs = acceptedAt - startedAt;
        if (elapsedMs < 0 || elapsedMs >= durationMs) return 0;
        long points = 20 + Math.round(80.0 * (durationMs - elapsedMs) / durationMs);
        return (int) Math.max(20, Math.min(100, points));
    }

    public static int calculateDrawingScore(int eligibleCount, Collection<Integer> correctScores) {
        if (eligibleCount <= 0) return 0;
        if (correctScores.isEmpty()) return -20;
        if (correctScores.size() > eligibleCount) throw new IllegalArgumentException("Số người đúng vượt số người đủ điều kiện.");
        int sum = correctScores.stream().mapToInt(Integer::intValue).sum();
        long points = Math.round(70.0 * correctScores.size() / eligibleCount + 30.0 * sum / (100 * eligibleCount));
        return (int) Math.max(0, Math.min(100, points));
    }
}
