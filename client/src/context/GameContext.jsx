import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import wsService from '../services/websocket';
import { useAuth } from './AuthContext';

const GameContext = createContext(null);

export function GameProvider({ children }) {
  const { login, isLoggedIn, user } = useAuth();
  const [currentPage, setCurrentPage] = useState('login');
  const [connected, setConnected] = useState(false);

  // Gửi AUTH sau khi websocket kết nối và user đã login
  useEffect(() => {
    if (connected && isLoggedIn && user) {
      wsService.send('AUTH', { username: user.username });
    }
  }, [connected, isLoggedIn, user]);

  // Lobby state
  const [onlinePlayers, setOnlinePlayers] = useState([]);
  const [rooms, setRooms] = useState([]);

  const [roomChat, setRoomChat] = useState([]);
  const [chatMessages, setChatMessages] = useState({});
  const [activeChatWindows, setActiveChatWindows] = useState([]);

  const openChatWindow = useCallback((id) => {
    setActiveChatWindows(prev => {
      if (prev.includes(id)) return prev;
      const newWindows = [...prev, id];
      if (newWindows.length > 3) newWindows.shift(); // Keep max 3 windows
      return newWindows;
    });
  }, []);

  const closeChatWindow = useCallback((id) => {
    setActiveChatWindows(prev => prev.filter(w => w !== id));
  }, []);

  // Room state
  const [currentRoom, setCurrentRoom] = useState(null);
  const [roomPlayers, setRoomPlayers] = useState([]);

  // Game state
  const [gamePhase, setGamePhase] = useState(null);
  const [timer, setTimer] = useState(0);
  const [timerPhase, setTimerPhase] = useState('');
  const [topicOptions, setTopicOptions] = useState([]);
  const [selectedTopic, setSelectedTopic] = useState(null);
  const [paintings, setPaintings] = useState([]);
  const [currentPainting, setCurrentPainting] = useState(null);
  const [hint, setHint] = useState('');
  const [guessLogs, setGuessLogs] = useState([]);
  const [rankings, setRankings] = useState([]);
  const [roundResult, setRoundResult] = useState(null);
  const [gameResult, setGameResult] = useState(null);
  const [playerOrder, setPlayerOrder] = useState([]);
  const [paintingInfo, setPaintingInfo] = useState({ index: 0, total: 0 });

  const [roomInvites, setRoomInvites] = useState([]);

  // Notification
  const [notification, setNotification] = useState(null);

  const showNotification = useCallback((message, type = 'info') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 4000);
  }, []);

  useEffect(() => {
    wsService.connect();

    const cleanups = [
      wsService.on('CONNECTION', (msg) => setConnected(msg.connected)),

      wsService.on('LOGIN_RESPONSE', (msg) => {
        if (msg.success) {
          login({ 
            userId: msg.data.userId, 
            username: msg.data.username, 
            displayName: msg.data.displayName,
            rankingScore: msg.data.rankingScore, 
            role: msg.data.role, 
            avatar: msg.data.avatar 
          });
          setCurrentPage('lobby');
          showNotification('Đăng nhập thành công!', 'success');
        } else {
          showNotification(msg.content, 'error');
        }
      }),

      wsService.on('REGISTER_RESPONSE', (msg) => {
        showNotification(msg.content, msg.success ? 'success' : 'error');
      }),

      wsService.on('PLAYER_LIST', (msg) => {
        setOnlinePlayers(msg.data.players || []);
      }),

      wsService.on('ROOM_LIST', (msg) => {
        setRooms(msg.data.rooms || []);
      }),

      wsService.on('ROOM_RESPONSE', (msg) => {
        if (msg.success) {
          setCurrentRoom({ 
            roomId: msg.data.roomId, 
            roomName: msg.data.roomName,
            maxRounds: msg.data.maxRounds || 3,
            drawTime: msg.data.drawTime || 60,
            language: msg.data.language || 'vi'
          });
          setRoomChat([]);
          openChatWindow('ROOM');
          setCurrentPage('room');
          showNotification(msg.content, 'success');
        } else {
          showNotification(msg.content, 'error');
        }
      }),

      wsService.on('ROOM_UPDATE', (msg) => {
        setRoomPlayers(msg.data.players || []);
        setCurrentRoom(prev => prev ? { ...prev, playerCount: msg.data.playerCount, status: msg.data.status } : prev);
      }),

      wsService.on('ROOM_UPDATE_SETTINGS', (msg) => {
        setCurrentRoom(prev => prev ? { 
          ...prev, 
          maxRounds: msg.data.maxRounds, 
          drawTime: msg.data.drawTime, 
          language: msg.data.language 
        } : prev);
      }),

      wsService.on('ROOM_CHAT_RECEIVE', (msg) => {
        setRoomChat(prev => [...prev, {
          username: msg.data.senderUsername,
          displayName: msg.data.senderDisplayName,
          content: msg.data.content,
          timestamp: msg.data.timestamp
        }]);
      }),

      wsService.on('GAME_START', (msg) => {
        setPlayerOrder(msg.data.playerOrder || []);
        setGamePhase('topicSelect');
        setCurrentPage('game');
        setGuessLogs([]);
        setRankings([]);
        setGameResult(null);
      }),

      wsService.on('TOPIC_OPTIONS', (msg) => {
        setTopicOptions(msg.data.topics || []);
        setGamePhase('topicSelect');
      }),

      wsService.on('TOPIC_CONFIRMED', (msg) => {
        setSelectedTopic(msg.data.topic);
        showNotification(msg.data.autoSelected ? 'Chủ đề được tự động chọn: ' + msg.data.topic : 'Đã chọn: ' + msg.data.topic, 'info');
      }),

      wsService.on('DRAW_START', (msg) => {
        setGamePhase('drawing');
      }),

      wsService.on('GUESS_START', (msg) => {
        setGamePhase('guessing');
        setGuessLogs([]);
        setRankings([]);
        setPaintingInfo({ index: msg.data.paintingIndex, total: msg.data.totalPaintings, painter: msg.data.painterUsername });
      }),

      wsService.on('PAINTING_DISPLAY', (msg) => {
        setCurrentPainting({ painter: msg.data.painterUsername, drawData: msg.data.drawData || [] });
        setHint(msg.data.hint || '');
      }),

      wsService.on('GUESS_RESULT', (msg) => {
        if (msg.data.correct) {
          showNotification(msg.content, 'success');
        }
      }),

      wsService.on('GUESS_LOG', (msg) => {
        setGuessLogs(prev => [...prev, { username: msg.data.guesserUsername, correct: msg.data.correct, elapsedMs: msg.data.elapsedMs, content: msg.content }]);
      }),

      wsService.on('GUESS_RANKING_UPDATE', (msg) => {
        setRankings(msg.data.rankings || []);
      }),

      wsService.on('TIMER_UPDATE', (msg) => {
        setTimer(msg.data.remaining);
        setTimerPhase(msg.data.phase);
      }),

      wsService.on('ROUND_RESULT', (msg) => {
        setRoundResult(msg.data);
        setGamePhase('roundResult');
      }),

      wsService.on('GAME_RESULT', (msg) => {
        setGameResult(msg.data);
        setGamePhase('summary');
        setCurrentPage('game');
      }),

      wsService.on('ROOM_DISSOLVED', (msg) => {
        setCurrentRoom(null);
        setCurrentPage('lobby');
        setGamePhase(null);
        showNotification(msg.content || 'Phòng đã bị giải tán.', 'warning');
      }),

      wsService.on('PLAYER_DISCONNECTED', (msg) => {
        showNotification(`${msg.data.username} đã rời phòng.`, 'warning');
      }),

      // === Friend System Listeners ===
      wsService.on('FRIEND_REQUEST_NOTIFY', (msg) => {
        showNotification(msg.content, 'info');
        // Let LobbyPage refresh friends if it wants to, or just notify
        window.dispatchEvent(new CustomEvent('friendUpdate'));
      }),

      wsService.on('FRIEND_UPDATE', (msg) => {
        showNotification(msg.content, msg.success ? 'success' : 'error');
        window.dispatchEvent(new CustomEvent('friendUpdate'));
      }),

      wsService.on('ROOM_INVITE_NOTIFY', (msg) => {
        setRoomInvites(prev => [...prev, {
          roomId: msg.data.roomId,
          roomName: msg.data.roomName,
          inviterUsername: msg.data.inviterUsername,
          inviterAvatar: msg.data.inviterAvatar,
        }]);
        showNotification(msg.content, 'info');
      }),

      wsService.on('DIRECT_MESSAGE_RECEIVE', (msg) => {
        const data = msg.data;
        const otherUser = data.isSelf ? data.targetUsername : data.senderUsername;
        setChatMessages(prev => {
          const history = prev[otherUser] || [];
          return { ...prev, [otherUser]: [...history, data] };
        });
        if (!data.isSelf) {
          showNotification(`${data.senderDisplayName} đã gửi một tin nhắn cho bạn`, 'info');
          playSound('tick');
          // Automatically open chat window if not already open
          setActiveChatWindows(prev => {
            if (prev.includes(data.senderUsername)) return prev;
            const newWindows = [...prev, data.senderUsername];
            if (newWindows.length > 3) newWindows.shift();
            return newWindows;
          });
        }
        window.dispatchEvent(new CustomEvent('friendUpdate'));
      }),

      wsService.on('CHAT_HISTORY_RESPONSE', (msg) => {
        setChatMessages(prev => ({
          ...prev,
          [msg.data.targetUsername]: msg.data.history
        }));
      }),
    ];

    return () => cleanups.forEach(cleanup => cleanup());
  }, []);

  const navigateTo = useCallback((page) => {
    setCurrentPage(page);
  }, []);

  const leaveRoom = useCallback(() => {
    wsService.send('LEAVE_ROOM');
    setCurrentRoom(null);
    setCurrentPage('lobby');
    setGamePhase(null);
    setSelectedTopic(null);
    closeChatWindow('ROOM');
  }, [closeChatWindow]);

  const returnToLobby = useCallback(() => {
    if (currentRoom) {
      wsService.send('LEAVE_ROOM');
    }
    setCurrentRoom(null);
    setCurrentPage('lobby');
    setGamePhase(null);
    setSelectedTopic(null);
    setGameResult(null);
    setRoundResult(null);
    closeChatWindow('ROOM');
  }, [currentRoom, closeChatWindow]);

  return (
    <GameContext.Provider value={{
      currentPage, navigateTo, connected,
      onlinePlayers, rooms,
      currentRoom, roomPlayers, leaveRoom, returnToLobby, roomChat, setRoomChat,
      chatMessages, activeChatWindows, openChatWindow, closeChatWindow,
      gamePhase, setGamePhase, timer, timerPhase,
      topicOptions, selectedTopic,
      paintings, currentPainting, hint,
      guessLogs, rankings, roundResult, gameResult,
      playerOrder, paintingInfo,
      notification, showNotification,
      roomInvites, setRoomInvites,
    }}>
      {children}
    </GameContext.Provider>
  );
}

export function useGame() {
  const context = useContext(GameContext);
  if (!context) throw new Error('useGame must be used within GameProvider');
  return context;
}
