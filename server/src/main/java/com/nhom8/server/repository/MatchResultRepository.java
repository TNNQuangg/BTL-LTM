package com.nhom8.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom8.server.model.MatchResult;

/**
 * Repository quản lý thực thể MatchResult (thay thế MatchResultDAO).
 */
@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Integer> {
}
