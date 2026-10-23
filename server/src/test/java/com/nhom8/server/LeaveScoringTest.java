package com.nhom8.server;

import com.nhom8.server.model.User;
import com.nhom8.server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class LeaveScoringTest {
    @Autowired UserRepository users;
    @Autowired TestEntityManager entities;
    @Test void penaltyAndRewardsAreAppliedTogether() {
        User leaver = users.saveAndFlush(new User("leave_one", "password", "Leave One"));
        User remaining = users.saveAndFlush(new User("stay_one", "password", "Stay One"));
        assertEquals(2, users.applyLeaveScores(leaver.getUserId(), List.of(remaining.getUserId()), 20));
        entities.clear();
        assertEquals(-20, users.findById(leaver.getUserId()).orElseThrow().getRankingScore());
        assertEquals(20, users.findById(remaining.getUserId()).orElseThrow().getRankingScore());
    }
}
