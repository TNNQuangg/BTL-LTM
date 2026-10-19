package com.nhom8.server.repository;

import com.nhom8.server.model.BuiltInTopicOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuiltInTopicOverrideRepository extends JpaRepository<BuiltInTopicOverride, Long> {
    Optional<BuiltInTopicOverride> findByOriginalWordIgnoreCase(String originalWord);
}
