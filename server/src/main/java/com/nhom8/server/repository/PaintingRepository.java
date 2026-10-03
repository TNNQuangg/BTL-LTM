package com.nhom8.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom8.server.model.Painting;

/**
 * Repository quản lý thực thể Painting (thay thế PaintingDAO).
 */
@Repository
public interface PaintingRepository extends JpaRepository<Painting, Integer> {
}
