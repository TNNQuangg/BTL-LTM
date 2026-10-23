package com.nhom8.server;

import com.nhom8.server.game.Room;
import com.nhom8.server.service.GameService;
import com.nhom8.server.ws.ClientSession;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomPlayerLimitsTest {
    private ClientSession player(int number) {
        WebSocketSession socket = mock(WebSocketSession.class);
        when(socket.isOpen()).thenReturn(true);
        ClientSession client = new ClientSession(socket);
        client.setUsername("player" + number);
        return client;
    }

    @Test void twoReadyPlayersCannotStartAndThreeCanBecomeReady() {
        GameService game = mock(GameService.class);
        Room room = new Room("limits", "Giới hạn phòng", player(1), game);
        room.addPlayer(player(2));
        room.setPlayerReady("player1", true);
        room.setPlayerReady("player2", true);
        assertFalse(room.allPlayersReady());
        room.getGameSession().startGame();
        assertEquals(Room.RoomStatus.WAITING, room.getStatus());
        verifyNoInteractions(game);

        room.addPlayer(player(3));
        assertFalse(room.allPlayersReady());
        room.setPlayerReady("player3", true);
        assertTrue(room.allPlayersReady());
        room.removePlayer(room.getPlayer("player3"));
        assertFalse(room.allPlayersReady());
    }

    @Test void acceptsEightPlayersAndRejectsNinth() {
        Room room = new Room("capacity", "Tám người", player(1), mock(GameService.class));
        for (int i = 2; i <= 7; i++) room.addPlayer(player(i));
        assertFalse(room.isFull());
        room.addPlayer(player(8));
        assertEquals(8, room.getPlayerCount());
        assertTrue(room.isFull());
        room.addPlayer(player(9));
        assertEquals(8, room.getPlayerCount());
        assertNull(room.getPlayer("player9"));
        for (int i = 1; i <= 8; i++) room.setPlayerReady("player" + i, true);
        assertTrue(room.allPlayersReady());
    }
}
