package com.nhom8.vehinhdoany.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom8.vehinhdoany.model.Match;

/**
 * Repository quản lý thực thể Match (thay thế MatchDAO).
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Integer> {
}
