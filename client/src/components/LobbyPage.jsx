import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { useGame } from '../context/GameContext';
import wsService from '../services/websocket';
import { motion, AnimatePresence } from 'framer-motion';
import { Trophy, LogOut, Users, PlusCircle, Gamepad2, Search, UserPlus, UserCheck, UserX, X, Check, Bell, MessageSquare, Send, AlertTriangle, MessageCircle } from 'lucide-react';
import { playSound } from '../services/sound';
import { BrushStrokeLogo, DoodleIcon } from './ScribbleDecorations';

const containerVariants = {
  hidden: { opacity: 0 },
  show: { opacity: 1, transition: { staggerChildren: 0.1 } }
};
const itemVariants = {
  hidden: { opacity: 0, y: 20, scale: 0.95 },
  show: { opacity: 1, y: 0, scale: 1 }
};

export default function LobbyPage() {
  const { user, logout } = useAuth();
  const { rooms, navigateTo, roomInvites, setRoomInvites, showNotification } = useGame();
  
  const [newRoomName, setNewRoomName] = useState('');
  const [joinCode, setJoinCode] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  
  // Social Panel State
  const [friendTab, setFriendTab] = useState('friends');
  const [friends, setFriends] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [searchKeyword, setSearchKeyword] = useState('');

  // Floating Chat State
  const [chatThreads, setChatThreads] = useState([]);
  const [showThreadList, setShowThreadList] = useState(false);

  // Modals & Context Menu
  const [userToRemove, setUserToRemove] = useState(null);
  const [selectedFriend, setSelectedFriend] = useState(null); // Cho Profile Modal
  const [contextMenu, setContextMenu] = useState({ visible: false, x: 0, y: 0, friend: null });

  const getToken = () => localStorage.getItem('token');

  const fetchFriends = async () => {
    if (!user?.username) return;
    try {
      const [friendsRes, threadsRes] = await Promise.all([
        fetch(`http://localhost:9999/api/friends?username=${user.username}`, {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        }),
        fetch(`http://localhost:9999/api/friends/chat-threads?username=${user.username}`, {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        })
      ]);

      if (friendsRes.ok) {
        const data = await friendsRes.json();
        setFriends(data.friends || []);
        setPendingRequests(data.pending || []);
      }
      if (threadsRes.ok) {
        const data = await threadsRes.json();
        setChatThreads(data || []);
      }
    } catch (e) {
      console.error('Failed to fetch friends/threads', e);
    }
  };

  useEffect(() => {
    fetchFriends();
    
    const handleFriendUpdate = () => fetchFriends();
    window.addEventListener('friendUpdate', handleFriendUpdate);
    const interval = setInterval(fetchFriends, 15000);

    return () => {
      window.removeEventListener('friendUpdate', handleFriendUpdate);
      clearInterval(interval);
    };
  }, [user]);

  useEffect(() => {
    const closeMenu = () => setContextMenu(prev => ({ ...prev, visible: false }));
    document.addEventListener('click', closeMenu);
    return () => document.removeEventListener('click', closeMenu);
  }, []);

  const openChat = (friend) => {
    openChatWindow(friend.username);
    setShowThreadList(false);
    if (!chatMessages[friend.username]) {
      wsService.send('CHAT_HISTORY_REQUEST', { targetUsername: friend.username });
    }
  };

  const handleRightClick = (e, friend) => {
    e.preventDefault();
    setContextMenu({
      visible: true,
      x: e.clientX,
      y: e.clientY,
      friend
    });
  };

  const handleCreateRoom = (e) => {
    e.preventDefault();
    if (newRoomName.trim()) {
      wsService.send('ROOM_CREATE', { roomName: newRoomName.trim() });
      setNewRoomName('');
      setIsCreating(false);
      playSound('click');
    }
  };

  const handleJoinRoom = (roomId) => {
    wsService.send('ROOM_JOIN', { roomId });
    playSound('click');
  };

  const handleQuickJoin = (e) => {
    e.preventDefault();
    if (joinCode.trim()) {
      handleJoinRoom(joinCode.trim());
      setJoinCode('');
    }
  };

  const handleSendFriendRequest = async (targetUsername) => {
    if (!targetUsername) return;
    try {
      const res = await fetch('http://localhost:9999/api/friends/request', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Authorization': `Bearer ${getToken()}` },
        body: new URLSearchParams({ myUsername: user.username, targetUsername })
      });
      if (res.ok) {
        showNotification('Đã gửi lời mời kết bạn', 'success');
        if (friendTab === 'search') handleSearch();
        else fetchFriends();
      } else {
        const errorData = await res.json();
        showNotification(errorData.message || 'Lỗi gửi kết bạn', 'error');
      }
    } catch (e) {
      showNotification('Lỗi kết nối', 'error');
    }
  };

  const handleRespondFriendRequest = async (targetUsername, action) => {
    try {
      const res = await fetch('http://localhost:9999/api/friends/respond', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Authorization': `Bearer ${getToken()}` },
        body: new URLSearchParams({ myUsername: user.username, targetUsername, action })
      });
      if (res.ok) {
        showNotification(action === 'ACCEPT' ? 'Đã chấp nhận kết bạn' : 'Đã từ chối kết bạn', 'success');
        fetchFriends();
      } else {
        showNotification('Lỗi phản hồi kết bạn', 'error');
      }
    } catch (e) {
      showNotification('Lỗi kết nối', 'error');
    }
  };

  const handleRemoveFriend = async (friendUsername) => {
    try {
      const res = await fetch(`http://localhost:9999/api/friends?myUsername=${user.username}&friendUsername=${friendUsername}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${getToken()}` }
      });
      if (res.ok) {
        showNotification('Đã xóa bạn bè', 'success');
        fetchFriends();
      } else {
        showNotification('Lỗi xóa bạn', 'error');
      }
    } catch (e) {
      showNotification('Lỗi kết nối', 'error');
    }
  };

  const handleSearch = async (e) => {
    if (e) e.preventDefault();
    if (!searchKeyword.trim()) return;
    try {
      const res = await fetch(`http://localhost:9999/api/friends/search?query=${searchKeyword.trim()}&username=${user.username}`, {
        headers: { 'Authorization': `Bearer ${getToken()}` }
      });
      if (res.ok) {
        const data = await res.json();
        setSearchResults(data);
      }
    } catch (error) {
      console.error(error);
    }
  };

  const handleLogout = () => {
    logout();
    navigateTo('login');
    showNotification('Đăng xuất thành công!', 'success');
  };

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
      
      {/* HEADER */}
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 32px', borderBottom: '3px solid var(--ink)', background: 'var(--bg-panel)', zIndex: 10 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 15 }}>
          <BrushStrokeLogo text="LOBBY" fontSize="2rem" />
        </div>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: 30 }}>
          <div style={{ display: 'flex', gap: 20, color: 'var(--text-muted)', fontSize: '1rem', fontFamily: 'var(--font-accent)' }}>
            <div><Gamepad2 size={16} style={{display:'inline', verticalAlign:'text-bottom', color:'var(--accent-red)'}}/> &nbsp;&nbsp;{rooms.length} Phòng</div>
            <div><Users size={16} style={{display:'inline', verticalAlign:'text-bottom', color:'var(--accent-green)'}}/> &nbsp;&nbsp;{friends.filter(f=>f.isOnline).length} Bạn Online</div>
          </div>
          
          {/* User badge */}
          <div 
            className="user-badge" 
            style={{ cursor: 'pointer', padding: '8px 20px', transition: 'all 0.1s ease', transformOrigin: 'center' }}
            onClick={() => navigateTo('profile')}
            onMouseEnter={e => e.currentTarget.style.transform = 'scale(1.05)'}
            onMouseLeave={e => e.currentTarget.style.transform = 'scale(1)'}
          >
            <div className="badge-avatar" style={{ width: 40, height: 40, background: `url(https://api.dicebear.com/7.x/bottts/svg?seed=${user?.avatar || '1'}) center/cover`, backgroundColor: '#fff' }} />
            <div style={{ display: 'flex', flexDirection: 'column', marginLeft: 4 }}>
              <span className="badge-name" style={{ fontSize: '1.1rem' }}>{user?.displayName || user?.username}</span>
              <span className="badge-score" style={{ fontSize: '0.9rem' }}><Trophy size={12} style={{display:'inline'}}/> {user?.rankingScore || 0}</span>
            </div>
            <button 
              className="btn btn-secondary" 
              style={{ padding: 8, marginLeft: 16, border: 'none', background: 'transparent', color: 'var(--bg-surface)' }} 
              onClick={(e) => { e.stopPropagation(); handleLogout(); }} 
              title="Đăng xuất"
            >
              <LogOut size={20} />
            </button>
          </div>
        </div>
      </header>

      {/* MAIN GRID LAYOUT */}
      <main style={{ flex: 1, padding: 30, display: 'grid', gridTemplateColumns: '2.2fr 1fr', gap: 30, overflow: 'hidden' }}>
        
        {/* CỘT TRÁI: ROOM GRID */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 style={{ fontSize: '1.5rem', color: 'var(--ink)', display: 'flex', alignItems: 'center', gap: 10 }}>
              <Gamepad2 /> DANH SÁCH PHÒNG
            </h2>
            <form onSubmit={handleQuickJoin} style={{ display: 'flex', gap: 10 }}>
              <input type="text" className="input-field-boxed" placeholder="Nhập ID phòng..." value={joinCode} onChange={(e) => setJoinCode(e.target.value)} style={{ margin: 0, padding: '8px 15px', width: 150, fontSize: '1rem' }} />
              <button type="submit" className="btn btn-secondary" style={{ padding: '8px 15px' }}><Check size={16} /></button>
            </form>
          </div>

          <div style={{ flex: 1, overflowY: 'auto', paddingRight: 10, paddingBottom: 10 }}>
            <motion.div variants={containerVariants} initial="hidden" animate="show" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: 20 }}>
              
              {/* Thẻ Tạo Phòng Mới */}
              <motion.div variants={itemVariants} className="sketch-card doodle-menu-item" style={{ flexDirection: 'column', justifyContent: 'center', alignItems: 'center', padding: 30, minHeight: 140 }} onClick={() => setIsCreating(true)}>
                <PlusCircle size={40} color="var(--ink)" style={{ marginBottom: 10 }} />
                <span style={{ fontWeight: 600, color: 'var(--ink)', fontFamily: "var(--font-heading)", fontSize: '1.2rem', letterSpacing: 2 }}>TẠO PHÒNG MỚI</span>
              </motion.div>

              {/* Các phòng hiện có */}
              {rooms.map((room) => (
                <motion.div key={room.roomId} variants={itemVariants} className="sketch-card room-card" onClick={() => handleJoinRoom(room.roomId)} style={{ cursor: 'pointer', padding: 20, minHeight: 140, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <h3 style={{ fontSize: '1.3rem', margin: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: 'var(--ink)' }}>{room.roomName}</h3>
                    <span className={`room-status ${room.status === 'WAITING' ? 'waiting' : 'playing'}`} style={{ fontSize: '0.75rem', padding: '2px 10px' }}>
                      {room.status === 'WAITING' ? 'WAIT' : 'PLAY'}
                    </span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-muted)', fontSize: '1rem', marginTop: 'auto', paddingTop: 15, borderTop: '2px solid var(--ink)' }}>
                    <span><Users size={16} style={{ display: 'inline', verticalAlign: 'text-bottom' }}/> {room.playerCount}/10</span>
                    <span className="mono" style={{ color: 'var(--ink)' }}>#{room.roomId}</span>
                  </div>
                </motion.div>
              ))}
            </motion.div>
          </div>
        </div>

        {/* CỘT PHẢI: SOCIAL PANEL */}
        <div className="sketch-card" style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          
          <div style={{ display: 'flex', gap: 4, marginBottom: 15 }}>
            <button className={`btn ${friendTab === 'friends' ? 'btn-primary' : 'btn-secondary'}`} style={{ flex: 1, padding: '10px 0', fontSize: '0.9rem' }} onClick={() => { setFriendTab('friends'); playSound('tick'); }}>
              <UserCheck size={16} /> BẠN BÈ ({friends.length})
            </button>
            <button className={`btn ${friendTab === 'pending' ? 'btn-primary' : 'btn-secondary'}`} style={{ flex: 1, padding: '10px 0', fontSize: '0.9rem', position: 'relative' }} onClick={() => { setFriendTab('pending'); playSound('tick'); }}>
              <Bell size={16} /> LỜI MỜI
              {pendingRequests.length > 0 && (
                <span style={{ position: 'absolute', top: -6, right: -6, background: 'var(--accent-red)', color: '#fff', borderRadius: '50%', width: 20, height: 20, fontSize: '0.7rem', fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px solid var(--ink)' }}>{pendingRequests.length}</span>
              )}
            </button>
            <button className={`btn ${friendTab === 'search' ? 'btn-primary' : 'btn-secondary'}`} style={{ flex: 1, padding: '10px 0', fontSize: '0.9rem' }} onClick={() => { setFriendTab('search'); setSearchResults([]); playSound('tick'); }}>
              <Search size={16} /> TÌM
            </button>
          </div>

          <div style={{ flex: 1, overflowY: 'auto', paddingRight: 5 }}>
            
            {/* Tab BẠN BÈ */}
            {friendTab === 'friends' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {friends.length === 0 && <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: 20 }}>Chưa có bạn bè.</p>}
                {friends.map((f, i) => (
                  <motion.div key={i} variants={itemVariants} initial="hidden" animate="show"
                    onContextMenu={(e) => handleRightClick(e, f)}
                    onClick={(e) => handleRightClick(e, f)}
                    style={{
                      display: 'flex', alignItems: 'center', gap: 12, padding: '10px 15px', background: 'var(--bg-surface)', borderRadius: 12, cursor: 'context-menu',
                      border: `2px solid ${f.isOnline ? 'var(--accent-green)' : 'var(--ink)'}`,
                      transition: 'all 0.15s',
                    }}
                    onMouseEnter={(e) => { e.currentTarget.style.transform = 'translate(2px, 2px)'; e.currentTarget.style.boxShadow = 'none'; }}
                    onMouseLeave={(e) => { e.currentTarget.style.transform = 'translate(0, 0)'; e.currentTarget.style.boxShadow = 'var(--shadow-neo-sm)'; }}
                  >
                    <div style={{ position: 'relative' }}>
                      <div style={{ width: 36, height: 36, borderRadius: '50%', background: `url(https://api.dicebear.com/7.x/bottts/svg?seed=${f.avatar || '1'}) center/cover`, backgroundColor: '#fff', border: '2px solid var(--ink)' }} />
                      <div style={{ position: 'absolute', bottom: -1, right: -2, width: 10, height: 10, borderRadius: '50%', background: f.isOnline ? 'var(--accent-green)' : '#999', border: '2px solid var(--ink)' }} />
                    </div>
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 600, fontSize: '1rem', color: 'var(--ink)' }}>{f.displayName || f.username}</div>
                      <div style={{ fontSize: '0.85rem', color: f.isOnline ? 'var(--accent-green)' : 'var(--text-muted)', fontFamily: "'Caveat', cursive" }}>{f.isOnline ? '● Online' : '○ Offline'}</div>
                    </div>
                  </motion.div>
                ))}
                <div style={{ textAlign: 'center', marginTop: 20, fontSize: '0.9rem', color: 'var(--text-muted)', fontFamily: "'Caveat', cursive" }}>
                  Click vào bạn bè để tương tác ✏️
                </div>
              </div>
            )}

            {/* Các Tab khác */}
            {friendTab === 'pending' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {pendingRequests.length === 0 && <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: 20 }}>Không có lời mời nào.</p>}
                {pendingRequests.map((req, i) => (
                  <motion.div key={i} variants={itemVariants} initial="hidden" animate="show" style={{ padding: 12, background: 'var(--bg-surface)', borderRadius: 12, border: '2px solid var(--ink)' }}>
                    <div style={{ fontWeight: 600, fontSize: '1rem', color: 'var(--ink)' }}>{req.sender.displayName || req.sender.username}</div>
                    <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
                      <button className="btn btn-primary" style={{ flex: 1, padding: '6px' }} onClick={() => handleRespondFriendRequest(req.sender.username, 'ACCEPT')}><Check size={14} /></button>
                      <button className="btn btn-secondary" style={{ flex: 1, padding: '6px', borderColor: 'var(--accent-red)', color: 'var(--accent-red)' }} onClick={() => handleRespondFriendRequest(req.sender.username, 'REJECT')}><X size={14} /></button>
                    </div>
                  </motion.div>
                ))}
              </div>
            )}

            {friendTab === 'search' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                <form onSubmit={handleSearch} style={{ display: 'flex', gap: 10 }}>
                  <input type="text" className="input-field" placeholder="Tên người dùng..." value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)} style={{ margin: 0, padding: '10px 15px', flex: 1 }} />
                  <button type="submit" className="btn btn-primary" style={{ padding: '0 15px' }}><Search size={18} /></button>
                </form>
                {searchResults.map((u, i) => (
                  <motion.div key={i} variants={itemVariants} initial="hidden" animate="show" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: 12, background: 'var(--bg-surface)', borderRadius: 12, border: '2px solid var(--ink)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                      <div style={{ width: 36, height: 36, borderRadius: '50%', background: `url(https://api.dicebear.com/7.x/bottts/svg?seed=${u.avatar || '1'}) center/cover`, backgroundColor: '#fff', border: '2px solid var(--ink)' }} />
                      <div style={{ fontWeight: 600, fontSize: '1rem', color: 'var(--ink)' }}>{u.displayName || u.username}</div>
                    </div>
                    {u.friendStatus === 'NONE' && (
                      <button className="btn btn-primary" style={{ padding: '6px 12px', fontSize: '0.85rem' }} onClick={() => handleSendFriendRequest(u.username)}><UserPlus size={14} /></button>
                    )}
                    {u.friendStatus === 'PENDING' && <span style={{ color: 'var(--accent-orange)', fontSize: '0.9rem', fontFamily: "'Caveat', cursive" }}>Đã gửi ✓</span>}
                  </motion.div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>

      {/* TẠO PHÒNG MODAL */}
      <AnimatePresence>
        {isCreating && (
          <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <motion.div initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.9, opacity: 0 }} className="glass-card" style={{ width: 400 }}>
              <h2 style={{ marginBottom: 20, color: 'var(--ink)' }}>✏️ Tạo phòng mới</h2>
              <form onSubmit={handleCreateRoom}>
                <input type="text" className="input-field" placeholder="Tên phòng..." value={newRoomName} onChange={(e) => setNewRoomName(e.target.value)} autoFocus maxLength={30} />
                <div style={{ display: 'flex', gap: 15, marginTop: 20 }}>
                  <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setIsCreating(false)}>Hủy</button>
                  <button type="submit" className="btn btn-primary" style={{ flex: 1 }} disabled={!newRoomName.trim()}>Tạo phòng</button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* INVITATION MODAL */}
      <AnimatePresence>
        {roomInvites.length > 0 && (
          <div style={{ position: 'fixed', top: 20, right: 20, zIndex: 2000, display: 'flex', flexDirection: 'column', gap: 10 }}>
            {roomInvites.map((invite, i) => (
              <motion.div key={i} initial={{ x: 300, opacity: 0 }} animate={{ x: 0, opacity: 1 }} exit={{ x: 300, opacity: 0 }} className="glass-card" style={{ width: 320, padding: 20, background: 'var(--bg-surface)', borderLeft: '6px solid var(--accent-blue)' }}>
                <h4 style={{ margin: 0, marginBottom: 5, color: 'var(--ink)' }}>📩 Lời mời chơi game!</h4>
                <p style={{ fontSize: '1rem', color: 'var(--text-muted)', marginBottom: 15 }}><strong>{invite.senderName}</strong> mời bạn tham gia phòng <strong>{invite.roomName}</strong></p>
                <div style={{ display: 'flex', gap: 10 }}>
                  <button className="btn btn-secondary" style={{ flex: 1, padding: 8 }} onClick={() => setRoomInvites(prev => prev.filter((_, idx) => idx !== i))}>Bỏ qua</button>
                  <button className="btn btn-primary" style={{ flex: 1, padding: 8 }} onClick={() => { handleJoinRoom(invite.roomId); setRoomInvites(prev => prev.filter((_, idx) => idx !== i)); }}>Tham gia</button>
                </div>
              </motion.div>
            ))}
          </div>
        )}
      </AnimatePresence>

      {/* CONTEXT MENU (RIGHT CLICK) */}
      <AnimatePresence>
        {contextMenu.visible && contextMenu.friend && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.95 }}
            style={{
              position: 'fixed',
              top: contextMenu.y,
              left: contextMenu.x,
              background: 'var(--bg-panel)',
              border: '3px solid var(--ink)',
              borderRadius: 8,
              padding: '6px 0',
              zIndex: 3000,
              boxShadow: 'var(--shadow-neo)',
              minWidth: 160
            }}
          >
            <div style={{ padding: '8px 16px', borderBottom: '2px solid var(--ink)', marginBottom: 4 }}>
              <div style={{ fontWeight: 600, fontSize: '1rem', fontFamily: "'Bangers', cursive", color: 'var(--ink)' }}>{contextMenu.friend.displayName || contextMenu.friend.username}</div>
            </div>
            
            <button className="context-menu-item" onClick={() => { setSelectedFriend(contextMenu.friend); setContextMenu({visible:false}); }}>
              <Trophy size={14} /> Xem hồ sơ
            </button>
            <button className="context-menu-item" onClick={() => { openChat(contextMenu.friend); setContextMenu({visible:false}); }}>
              <MessageSquare size={14} /> Nhắn tin
            </button>
            <div style={{ height: 2, background: 'var(--ink)', margin: '4px 0' }} />
            <button className="context-menu-item" style={{ color: 'var(--accent-red)' }} onClick={() => { setUserToRemove(contextMenu.friend); setContextMenu({visible:false}); }}>
              <UserX size={14} /> Xóa bạn bè
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* UNFRIEND CONFIRMATION */}
      <AnimatePresence>
        {userToRemove && (
          <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 4000, display: 'flex', alignItems: 'center', justifyContent: 'center' }} onClick={() => setUserToRemove(null)}>
            <motion.div initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.9, opacity: 0 }} onClick={e => e.stopPropagation()} className="glass-card" style={{ width: 320, textAlign: 'center', padding: 24 }}>
              <AlertTriangle size={48} color="var(--accent-red)" style={{ margin: '0 auto 16px' }} />
              <h3 style={{ fontSize: '1.4rem', marginBottom: 12, color: 'var(--ink)' }}>Xóa bạn bè</h3>
              <p style={{ color: 'var(--text-muted)', marginBottom: 24, fontSize: '1rem' }}>Bạn có chắc muốn xóa <strong>{userToRemove.displayName || userToRemove.username}</strong>?</p>
              <div style={{ display: 'flex', gap: 12 }}>
                <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setUserToRemove(null)}>Hủy</button>
                <button className="btn btn-primary" style={{ flex: 1, background: 'var(--accent-red)', color: '#fff' }} onClick={() => { handleRemoveFriend(userToRemove.username); setUserToRemove(null); }}>Đồng ý</button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* PROFILE MODAL (READONLY) */}
      <AnimatePresence>
        {selectedFriend && (
          <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 4000, display: 'flex', alignItems: 'center', justifyContent: 'center' }} onClick={() => setSelectedFriend(null)}>
            <motion.div initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.9, opacity: 0 }} onClick={e => e.stopPropagation()} className="glass-card" style={{ width: 360, textAlign: 'center', padding: 32 }}>
              <div style={{ position: 'relative', width: 100, height: 100, margin: '0 auto 20px' }}>
                <div style={{ width: '100%', height: '100%', borderRadius: '50%', background: `url(https://api.dicebear.com/7.x/bottts/svg?seed=${selectedFriend.avatar || '1'}) center/cover`, backgroundColor: '#fff', border: `3px solid ${selectedFriend.isOnline ? 'var(--accent-green)' : 'var(--ink)'}` }} />
              </div>
              <h2 style={{ fontSize: '1.8rem', marginBottom: 4, color: 'var(--ink)' }}>{selectedFriend.displayName || selectedFriend.username}</h2>
              <p style={{ color: selectedFriend.isOnline ? 'var(--accent-green)' : 'var(--text-muted)', marginBottom: 20, fontFamily: "'Caveat', cursive", fontSize: '1.2rem' }}>{selectedFriend.isOnline ? '● Đang online' : '○ Ngoại tuyến'}</p>
              <div style={{ background: 'var(--bg-base)', borderRadius: 12, padding: 16, marginBottom: 24, display: 'flex', justifyContent: 'center', gap: 24, border: '2px solid var(--ink)' }}>
                <div>
                  <div style={{ fontSize: '0.9rem', color: 'var(--ink)', marginBottom: 4, fontFamily: "'Bangers', cursive", letterSpacing: 1 }}>ĐIỂM HẠNG</div>
                  <div style={{ fontSize: '1.3rem', fontWeight: 'bold', color: 'var(--ink)' }}><Trophy size={16} style={{display:'inline'}}/> {selectedFriend.rankingScore}</div>
                </div>
              </div>
              <button className="btn btn-secondary" style={{ width: '100%' }} onClick={() => setSelectedFriend(null)}>Đóng</button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Giao diện Chat đã được chuyển sang ChatWidget toàn cục */}

    </div>
  );
}
