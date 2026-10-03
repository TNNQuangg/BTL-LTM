import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useGame } from '../context/GameContext';
import wsService from '../services/websocket';
import { motion, AnimatePresence } from 'framer-motion';
import { LogOut, Play, CheckCircle, UserPlus, X, Send, Crown, Users, Settings, UserMinus } from 'lucide-react';
import { playSound } from '../services/sound';
import { BrushStrokeLogo } from './ScribbleDecorations';

export default function RoomPage() {
  const { user } = useAuth();
  const { currentRoom, roomPlayers, leaveRoom } = useGame();
  const [showInvite, setShowInvite] = useState(false);
  const [friends, setFriends] = useState([]);
  
  const me = roomPlayers.find(p => p.username === user?.username);
  const isHost = me?.isHost;
  const isReady = me?.isReady;

  const invitableFriends = friends.filter(f => f.online && !f.inRoom);

  const handleToggleReady = () => {
    wsService.send('READY', { isReady: !isReady });
    playSound('tick');
  };

  const handleToggleInvite = async () => {
    const newShowInvite = !showInvite;
    setShowInvite(newShowInvite);
    playSound('tick');
    if (newShowInvite && user?.username) {
      try {
        const token = localStorage.getItem('token');
        const res = await fetch(`http://localhost:9999/api/friends?username=${user.username}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (res.ok) {
          const data = await res.json();
          setFriends(data);
        }
      } catch (err) {
        console.error('Failed to fetch friends', err);
      }
    }
  };

  const handleStartGame = () => {
    if (isHost && roomPlayers.length >= 2) {
      wsService.send('READY', { isReady: true });
      playSound('start');
    }
  };

  const handleInviteFriend = (username) => {
    wsService.send('ROOM_INVITE_SEND', { targetUsername: username });
    playSound('tick');
  };

  const handleKick = (username) => {
    wsService.send('ROOM_KICK_PLAYER', { targetUsername: username });
    playSound('tick');
  };

  const handleUpdateSetting = (key, value) => {
    wsService.send('ROOM_UPDATE_SETTINGS', { [key]: value });
    playSound('tick');
  };

  if (!currentRoom) return null;

  // Max 10 players, fill empty slots
  const MAX_PLAYERS = 10;
  const slots = Array(MAX_PLAYERS).fill(null);
  roomPlayers.forEach((p, index) => {
    if (index < MAX_PLAYERS) slots[index] = p;
  });

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', padding: 40 }}>
      
      {/* HEADER PHÒNG CHỜ */}
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 40, borderBottom: '3px solid var(--ink)', paddingBottom: 20 }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 15 }}>
            <BrushStrokeLogo text={currentRoom.roomName} fontSize="2.5rem" />
            <span className="mono" style={{ background: 'var(--bg-surface)', border: '2px solid var(--ink)', color: 'var(--ink)', padding: '4px 12px', borderRadius: '255px 15px 225px 15px / 15px 225px 15px 255px', fontSize: '1rem', boxShadow: 'none' }}>
              ID: {currentRoom.roomId}
            </span>
          </div>
          <p style={{ color: 'var(--text-muted)', marginTop: 5, display: 'flex', alignItems: 'center', gap: 5, fontSize: '1.1rem' }}>
            <Users size={16} /> Đang chờ người chơi ({roomPlayers.length}/{MAX_PLAYERS})
          </p>
        </div>

        <div style={{ display: 'flex', gap: 15 }}>
          <button className="btn btn-secondary" onClick={handleToggleInvite}>
            <UserPlus size={20} /> MỜI BẠN BÈ
          </button>
          <button className="btn btn-secondary" onClick={() => { leaveRoom(); playSound('tick'); }} style={{ borderColor: 'var(--accent-red)', color: 'var(--accent-red)' }}>
            <LogOut size={20} /> THOÁT PHÒNG
          </button>
        </div>
      </header>

      {/* LƯỚI NGƯỜI CHƠI & SETTINGS */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 350px', gap: 40, flex: 1 }}>
        
        {/* PLAYER SLOTS */}
        <div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 20 }}>
            {slots.map((player, index) => (
              <motion.div 
                key={player ? player.username : `empty-${index}`}
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: index * 0.05 }}
                style={{
                  aspectRatio: '1',
                  background: player ? 'var(--bg-surface)' : 'var(--bg-panel)',
                  border: `3px solid ${player ? (player.isHost ? 'var(--accent-orange)' : player.isReady ? 'var(--accent-green)' : 'var(--ink)') : 'var(--ink)'}`,
                  borderRadius: '255px 15px 225px 15px / 15px 225px 15px 255px',
                  display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
                  position: 'relative',
                  overflow: 'visible',
                  boxShadow: 'none',
                  borderStyle: player ? 'solid' : 'dashed'
                }}
              >
                {player ? (
                  <>
                    {player.isHost && (
                      <div style={{ position: 'absolute', top: 10, right: 10, color: 'var(--accent-orange)' }}>
                        <Crown size={20} fill="var(--accent-orange)" />
                      </div>
                    )}
                    <div 
                      style={{ 
                        width: 70, height: 70, borderRadius: '50%', marginBottom: 15,
                        background: `url(https://api.dicebear.com/7.x/bottts/svg?seed=${player.avatar || '1'}) center/cover`,
                        backgroundColor: '#fff',
                        border: `3px solid ${player.isHost ? 'var(--accent-orange)' : player.isReady ? 'var(--accent-green)' : 'var(--ink)'}`,
                        boxShadow: 'var(--shadow-neo-sm)'
                      }} 
                    />
                    <div style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--ink)', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap', maxWidth: '90%' }}>
                      {player.displayName || player.username}
                    </div>
                    <div style={{ fontSize: '0.9rem', color: player.isHost ? 'var(--accent-orange)' : player.isReady ? 'var(--accent-green)' : 'var(--text-muted)', marginTop: 5, fontFamily: "var(--font-heading)", letterSpacing: 2 }}>
                      {player.isHost ? '👑 CHỦ PHÒNG' : player.isReady ? '✅ SẴN SÀNG' : '⏳ ĐANG CHỜ'}
                    </div>
                    {isHost && !player.isHost && (
                      <button 
                        onClick={() => handleKick(player.username)}
                        style={{ position: 'absolute', top: 10, left: 10, background: 'var(--accent-red)', color: '#fff', border: '2px solid var(--ink)', borderRadius: '50%', padding: 4, cursor: 'pointer' }}
                        title="Đuổi khỏi phòng"
                      >
                        <UserMinus size={14} />
                      </button>
                    )}
                  </>
                ) : (
                  <div style={{ color: 'var(--text-muted)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10 }}>
                    <UserPlus size={40} />
                    <span style={{ fontSize: '0.9rem', fontFamily: "var(--font-accent)" }}>Trống</span>
                  </div>
                )}
              </motion.div>
            ))}
          </div>

          {/* ACTION BUTTON */}
          <div style={{ display: 'flex', justifyContent: 'center', marginTop: 60 }}>
            {isHost ? (
              <motion.button 
                whileHover={{ scale: 1.05 }} 
                whileTap={{ scale: 0.95 }}
                className="btn btn-primary" 
                style={{ padding: '20px 80px', fontSize: '1.8rem', fontFamily: "var(--font-heading)", letterSpacing: 4, background: 'var(--accent-green)', color: 'var(--ink)' }}
                onClick={handleStartGame}
                disabled={roomPlayers.length < 2 || !roomPlayers.every(p => p.isHost || p.isReady)}
              >
                <Play size={28} fill="currentColor" /> BẮT ĐẦU TRẬN ĐẤU
              </motion.button>
            ) : (
              <motion.button 
                whileHover={{ scale: 1.05 }} 
                whileTap={{ scale: 0.95 }}
                className={`btn ${isReady ? 'btn-secondary' : 'btn-success'}`}
                style={{ padding: '20px 80px', fontSize: '1.8rem', fontFamily: "var(--font-heading)", letterSpacing: 4 }}
                onClick={handleToggleReady}
              >
                <CheckCircle size={28} /> {isReady ? 'HỦY SẴN SÀNG' : 'SẴN SÀNG'}
              </motion.button>
            )}
          </div>
        </div>

        {/* CỘT PHẢI: CÀI ĐẶT & MỜI BẠN */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div className="sketch-card" style={{ padding: 25 }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: 10, color: 'var(--ink)', marginBottom: 20, borderBottom: '2px solid var(--ink)', paddingBottom: 10 }}>
              <Settings size={20} /> CÀI ĐẶT PHÒNG
            </h3>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 15 }}>
              <span style={{ color: 'var(--text-muted)' }}>Số vòng:</span>
              {isHost ? (
                <select className="sketch-input" style={{ width: 120, padding: 5 }} value={currentRoom.maxRounds} onChange={(e) => handleUpdateSetting('maxRounds', parseInt(e.target.value))}>
                  <option value={3}>3 Vòng</option>
                  <option value={5}>5 Vòng</option>
                  <option value={7}>7 Vòng</option>
                </select>
              ) : (
                <span style={{ fontWeight: 600, fontFamily: "var(--font-heading)" }}>{currentRoom.maxRounds} Vòng</span>
              )}
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 15 }}>
              <span style={{ color: 'var(--text-muted)' }}>Thời gian vẽ:</span>
              {isHost ? (
                <select className="sketch-input" style={{ width: 120, padding: 5 }} value={currentRoom.drawTime} onChange={(e) => handleUpdateSetting('drawTime', parseInt(e.target.value))}>
                  <option value={45}>45s</option>
                  <option value={60}>60s</option>
                  <option value={90}>90s</option>
                  <option value={120}>120s</option>
                </select>
              ) : (
                <span style={{ fontWeight: 600, fontFamily: "var(--font-heading)" }}>{currentRoom.drawTime}s</span>
              )}
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--text-muted)' }}>Ngôn ngữ:</span>
              {isHost ? (
                <select className="sketch-input" style={{ width: 120, padding: 5 }} value={currentRoom.language} onChange={(e) => handleUpdateSetting('language', e.target.value)}>
                  <option value="vi">Tiếng Việt</option>
                  <option value="en">English</option>
                </select>
              ) : (
                <span style={{ fontWeight: 600, fontFamily: "var(--font-heading)" }}>{currentRoom.language === 'vi' ? 'Tiếng Việt' : 'English'}</span>
              )}
            </div>
          </div>

          <AnimatePresence>
            {showInvite && (
              <motion.div
                initial={{ opacity: 0, x: 50 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 50 }}
                className="sketch-card"
                style={{ flex: 1, padding: 20, display: 'flex', flexDirection: 'column' }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                  <h3 style={{ color: 'var(--ink)', fontSize: '1.2rem', margin: 0, display: 'flex', alignItems: 'center', gap: 8 }}>
                    <UserPlus size={18} /> Danh sách bạn bè
                  </h3>
                  <button className="btn btn-secondary" style={{ padding: 6 }} onClick={() => setShowInvite(false)}>
                    <X size={16} />
                  </button>
                </div>

                <div style={{ flex: 1, overflowY: 'auto' }}>
                  {invitableFriends.length === 0 ? (
                    <p style={{ color: 'var(--text-muted)', textAlign: 'center', marginTop: 40, fontSize: '1rem', fontFamily: "var(--font-accent)" }}>
                      Không có bạn bè online để mời. ✏️
                    </p>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                      {invitableFriends.map((f, i) => (
                        <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: 12, background: 'var(--bg-surface)', borderRadius: 12, border: '2px solid var(--ink)' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <div style={{ width: 32, height: 32, borderRadius: '50%', background: `url(https://api.dicebear.com/7.x/bottts/svg?seed=${f.avatar || '1'}) center/cover`, backgroundColor: '#fff', border: '2px solid var(--ink)' }} />
                            <span style={{ fontWeight: 600, fontSize: '1rem' }}>{f.displayName || f.username}</span>
                          </div>
                          <button className="btn btn-primary" style={{ padding: '6px 12px', fontSize: '0.9rem' }} onClick={() => handleInviteFriend(f.username)}>
                            Mời
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
}
