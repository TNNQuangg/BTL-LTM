package com.nhom8.server;

import com.nhom8.server.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserRoleTest {
    @Test
    void publicUserConstructionNeverGrantsAdminRoleByUsername() {
        assertEquals("USER", new User("admin", "bcrypt-hash", "Normal user").getRole());
        assertEquals("USER", new User("ordinary", "bcrypt-hash", "Other user").getRole());
    }
}
