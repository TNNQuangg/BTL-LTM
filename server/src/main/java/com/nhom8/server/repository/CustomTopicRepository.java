package com.nhom8.server.repository;

import com.nhom8.server.model.CustomTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomTopicRepository extends JpaRepository<CustomTopic, Long> {
    boolean existsByWordIgnoreCase(String word);
}
