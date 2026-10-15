package com.nhom8.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nhom8.server.model.ChatMessage;
import com.nhom8.server.model.User;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    @Query("SELECT c FROM ChatMessage c WHERE " +
            "(c.sender = :user1 AND c.receiver = :user2) OR " +
            "(c.sender = :user2 AND c.receiver = :user1) " +
            "ORDER BY c.createdAt ASC")
    List<ChatMessage> findChatHistory(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT DISTINCT u FROM User u WHERE " +
           "u IN (SELECT c.sender FROM ChatMessage c WHERE c.receiver = :me) OR " +
           "u IN (SELECT c.receiver FROM ChatMessage c WHERE c.sender = :me)")
    List<User> findChattedUsers(@Param("me") User me);

    long countByReceiverAndSenderAndReadStatusFalse(User receiver, User sender);

    @Modifying
    @Query("UPDATE ChatMessage c SET c.readStatus = true WHERE c.receiver = :receiver AND c.sender = :sender AND c.readStatus = false")
    int markConversationRead(@Param("receiver") User receiver, @Param("sender") User sender);
}
