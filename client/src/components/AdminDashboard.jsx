import { useState, useEffect } from 'react';
import wsService from '../services/websocket';
import { useGame } from '../context/GameContext';
import { useAuth } from '../context/AuthContext';
import { motion } from 'framer-motion';
import { ShieldAlert, Users, Swords, Settings, XCircle, LogOut } from 'lucide-react';
import { playSound } from '../services/sound';
import { BrushStrokeLogo } from './ScribbleDecorations';

export default function AdminDashboard() {
  const { navigateTo, rooms, showNotification } = useGame();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigateTo('login');
    showNotification('Đăng xuất thành công!', 'success');
  };
  
  const [users, setUsers] = useState([]);
  const [activeTab, setActiveTab] = useState('users');

  useEffect(() => {
    // Only fetch if admin
    if (user?.role === 'ADMIN') {
      wsService.send('ADMIN_GET_USERS');
    }
    
    const cleanup = wsService.on('ADMIN_USERS_RESPONSE', (msg) => {
      if (msg.success && msg.data.users) {
        setUsers(msg.data.users);
      }
    });

    return cleanup;
  }, [user]);

  const handleUpdateScore = (targetUser, currentScore) => {
    const newScore = prompt(`Nhập điểm mới cho ${targetUser}:`, currentScore);
    if (newScore !== null && !isNaN(newScore)) {
      wsService.send('ADMIN_UPDATE_SCORE', { targetUser, newScore: parseInt(newScore, 10) });
      playSound('tick');
      // Re-fetch users
      setTimeout(() => wsService.send('ADMIN_GET_USERS'), 500);
    }
  };

  const handleDissolveRoom = (roomId) => {
    if (window.confirm(`Bạn có chắc chắn muốn giải tán phòng ${roomId}?`)) {
      wsService.send('ADMIN_DISSOLVE_ROOM', { roomId });
      playSound('tick');
    }
  };

  if (user?.role !== 'ADMIN') {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <h2 style={{ color: 'var(--accent-red)' }}>🚫 KHÔNG CÓ QUYỀN TRUY CẬP</h2>
        <button className="btn btn-secondary" onClick={() => navigateTo('lobby')}>Quay lại</button>
      </div>
    );
  }

  return (
    <div style={{ padding: '40px', maxWidth: 1400, margin: '0 auto' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 40, borderBottom: '3px solid var(--ink)', paddingBottom: 20 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <BrushStrokeLogo text="ADMIN DASHBOARD" fontSize="2rem" />
        </div>
        <div style={{ display: 'flex', gap: 16 }}>
          <button className="btn btn-secondary" onClick={() => navigateTo('lobby')}>SẢNH CHỜ</button>
          <button className="btn btn-primary" onClick={handleLogout}><LogOut size={18} /> ĐĂNG XUẤT</button>
        </div>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: '250px 1fr', gap: 32 }}>
        
        {/* Sidebar */}
        <div className="sketch-card" style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 12 }}>
          <button 
            className={`btn ${activeTab === 'users' ? 'btn-primary' : 'btn-secondary'}`} 
            style={{ width: '100%', justifyContent: 'flex-start' }}
            onClick={() => setActiveTab('users')}
          >
            <Users size={20} /> NGƯỜI CHƠI ({users.length})
          </button>
          <button 
            className={`btn ${activeTab === 'rooms' ? 'btn-primary' : 'btn-secondary'}`} 
            style={{ width: '100%', justifyContent: 'flex-start' }}
            onClick={() => setActiveTab('rooms')}
          >
            <Swords size={20} /> PHÒNG ({rooms.length})
          </button>
        </div>

        {/* Content */}
        <div className="sketch-card">
          {activeTab === 'users' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
              <h2 style={{ marginBottom: 24, color: 'var(--ink)' }}>📋 QUẢN LÝ NGƯỜI CHƠI</h2>
              <table style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '3px solid var(--ink)', color: 'var(--ink)' }}>
                    <th style={{ padding: 12, fontFamily: "var(--font-heading)", letterSpacing: 1 }}>ID</th>
                    <th style={{ padding: 12, fontFamily: "var(--font-heading)", letterSpacing: 1 }}>USERNAME</th>
                    <th style={{ padding: 12, fontFamily: "var(--font-heading)", letterSpacing: 1 }}>ĐIỂM (RANK)</th>
                    <th style={{ padding: 12, fontFamily: "var(--font-heading)", letterSpacing: 1 }}>QUYỀN</th>
                    <th style={{ padding: 12, textAlign: 'right', fontFamily: "var(--font-heading)", letterSpacing: 1 }}>HÀNH ĐỘNG</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(u => (
                    <tr key={u.userId} style={{ borderBottom: '2px solid var(--ink)' }}>
                      <td style={{ padding: 12, fontFamily: "var(--font-accent)", fontSize: '1.1rem' }}>#{u.userId}</td>
                      <td style={{ padding: 12, fontWeight: 'bold', color: 'var(--ink)' }}>{u.username}</td>
                      <td style={{ padding: 12, color: 'var(--accent-green)', fontWeight: 'bold', fontFamily: "var(--font-heading)" }}>{u.rankingScore}</td>
                      <td style={{ padding: 12 }}>
                        <span style={{ padding: '4px 8px', borderRadius: '255px 15px 225px 15px / 15px 225px 15px 255px', fontSize: '0.9rem', background: u.role === 'ADMIN' ? 'var(--bg-base)' : 'var(--bg-surface)', color: 'var(--ink)', border: '2px solid var(--ink)', fontFamily: "var(--font-heading)" }}>
                          {u.role === 'ADMIN' ? '👑 ' : ''}{u.role}
                        </span>
                      </td>
                      <td style={{ padding: 12, textAlign: 'right' }}>
                        <button className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '0.9rem' }} onClick={() => handleUpdateScore(u.username, u.rankingScore)}>
                          <Settings size={14} /> SỬA ĐIỂM
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </motion.div>
          )}

          {activeTab === 'rooms' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
              <h2 style={{ marginBottom: 24, color: 'var(--ink)' }}>🎮 QUẢN LÝ PHÒNG CHƠI</h2>
              {rooms.length === 0 ? (
                <p style={{ color: 'var(--text-muted)', fontFamily: "var(--font-accent)", fontSize: '1.2rem' }}>Không có phòng nào đang hoạt động. ✏️</p>
              ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: 16 }}>
                  {rooms.map(r => (
                    <div key={r.roomId} style={{ padding: 20, background: 'var(--bg-surface)', borderRadius: '255px 15px 225px 15px / 15px 225px 15px 255px', border: '3px solid var(--ink)', boxShadow: 'none' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12 }}>
                        <h3 style={{ color: 'var(--ink)' }}>{r.roomName}</h3>
                        <span className="mono" style={{ color: 'var(--text-muted)' }}>#{r.roomId}</span>
                      </div>
                      <div style={{ marginBottom: 16 }}>
                        Trạng thái: <strong style={{ color: r.status === 'PLAYING' ? 'var(--accent-red)' : 'var(--accent-green)' }}>{r.status}</strong><br/>
                        Số người: <strong>{r.playerCount}/10</strong>
                      </div>
                      <button className="btn btn-primary" style={{ width: '100%', background: 'var(--accent-red)', color: '#fff' }} onClick={() => handleDissolveRoom(r.roomId)}>
                        <XCircle size={16} /> GIẢI TÁN PHÒNG
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </motion.div>
          )}
        </div>
      </div>
    </div>
  );
}
