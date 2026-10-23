package com.nhom8.server;

import com.nhom8.server.service.ScoringService;
import com.nhom8.server.game.GuessingWindow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ScoringServiceTest {
    @ParameterizedTest @CsvSource({"0,100", "2000,95", "3000,92", "5000,87", "10000,73", "15000,60", "20000,47", "25000,33", "29000,23", "29999,20"})
    void scoresUseElapsedMilliseconds(long elapsed, int points) {
        assertEquals(points, ScoringService.calculateGuessScore(1000, 1000 + elapsed, 30000));
    }
    @Test void rejectsInvalidWindowsAndRoundsHalfUp() {
        assertEquals(0, ScoringService.calculateGuessScore(0, 30000, 30000));
        assertEquals(0, ScoringService.calculateGuessScore(0, 30001, 30000));
        assertEquals(0, ScoringService.calculateGuessScore(1, 0, 30000));
        assertEquals(0, ScoringService.calculateGuessScore(0, 0, 0));
        assertEquals(100, ScoringService.calculateGuessScore(0, 1, 160)); // 99,5 → 100.
        assertEquals(99, ScoringService.calculateGuessScore(0, 2, 160));
        assertEquals(47, ScoringService.calculateGuessScore(0, 19999, 30000));
        assertEquals(46, ScoringService.calculateGuessScore(0, 20063, 30000));
    }
    @Test void drawingExamplesAndBoundaries() {
        assertEquals(88, ScoringService.calculateDrawingScore(4, List.of(60,60,60,60)));
        assertEquals(66, ScoringService.calculateDrawingScore(4, List.of(100,60,20)));
        assertEquals(44, ScoringService.calculateDrawingScore(4, List.of(60,60)));
        assertEquals(22, ScoringService.calculateDrawingScore(4, List.of(60)));
        assertEquals(-20, ScoringService.calculateDrawingScore(4, List.of()));
        assertEquals(0, ScoringService.calculateDrawingScore(0, List.of()));
        assertEquals(100, ScoringService.calculateDrawingScore(2, List.of(100,100)));
    }
    @Test void cumulativeExposureExcludesAuthorAndPreservesCorrectDisconnectedGuesser() {
        GuessingWindow window = new GuessingWindow(0, 30000, 10000, Map.of("a",true,"b",true,"c",false));
        window.connectionChanged("a", false, 4000);
        window.connectionChanged("a", true, 20000);
        assertEquals(9999, window.connectedMillis("a", 25999));
        assertFalse(window.eligibleGuessers(25999).contains("a"));
        assertTrue(window.eligibleGuessers(26000).contains("a"));
        assertEquals(100, window.awardCorrect("b", 0));
        window.permanentlyLeft("b", 1000);
        assertTrue(window.eligibleGuessers(30000).contains("b"));
        assertFalse(window.eligibleGuessers(30000).contains("c"));
        assertFalse(window.eligibleGuessers(30000).contains("artist"));
        assertEquals(0, window.awardCorrect("b", 2000));
    }
    @Test void candidatesWithoutOpportunityDoNotCauseEarlyFinishOrFailurePenalty() {
        GuessingWindow window = new GuessingWindow(0, 30000, 10000, Map.of("a",true,"b",false));
        assertEquals(95, window.awardCorrect("a", 2000));
        assertFalse(window.everyoneCorrect(2000));
        window.permanentlyLeft("b", 3000);
        assertTrue(window.everyoneCorrect(3000));
        assertEquals(99, ScoringService.calculateDrawingScore(window.eligibleGuessers(3000).size(), window.correctScores()));
    }
    @Test void disconnectedOrUnknownPlayersCannotAwardPoints() {
        GuessingWindow window = new GuessingWindow(0, 30000, 10000, Map.of("a",false));
        assertEquals(0, window.awardCorrect("a", 100));
        assertEquals(0, window.awardCorrect("forged", 100));
        window.connectionChanged("a", true, 5000);
        assertEquals(87, window.awardCorrect("a", 5000));
        assertEquals(0, window.awardCorrect("a", 6000));
        assertEquals(1, window.correctScores().size());
    }
}
