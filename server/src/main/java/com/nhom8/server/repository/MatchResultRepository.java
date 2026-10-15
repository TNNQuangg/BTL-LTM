package com.nhom8.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nhom8.server.model.MatchResult;
import com.nhom8.server.model.User;

import java.util.List;

/**
 * Repository quản lý thực thể MatchResult (thay thế MatchResultDAO).
 */
@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Integer> {

    @Query("SELECT mr FROM MatchResult mr JOIN FETCH mr.match m WHERE mr.user = :user ORDER BY m.startedAt DESC")
    List<MatchResult> findByUserWithMatchOrderByStartedAtDesc(@Param("user") User user);

    List<MatchResult> findByMatch_MatchId(Integer matchId);
}
