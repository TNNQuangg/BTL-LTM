import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useGame } from '../context/GameContext';
import wsService from '../services/websocket';
import { motion } from 'framer-motion';
import { User, Image as ImageIcon, ChevronLeft, Save } from 'lucide-react';
import { playSound } from '../services/sound';
import { BrushStrokeLogo } from './ScribbleDecorations';

const AVATARS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'];

export default function ProfilePage() {
  const { user, login } = useAuth();
  const { navigateTo, showNotification } = useGame();
  
  const [selectedAvatar, setSelectedAvatar] = useState(String(user?.avatar || '1'));
  const [displayName, setDisplayName] = useState(user?.displayName || user?.username || '');

  const handleSave = async () => {
    playSound('tick');
    try {
      const res = await fetch(`http://localhost:9999/api/auth/profile`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          username: user?.username,
          avatar: selectedAvatar,
          displayName: displayName
        })
      });
      if (res.ok) {
        const updatedUser = await res.json();
        playSound('correct');
        login({ ...user, ...updatedUser });
        showNotification('Cập nhật hồ sơ thành công!', 'success');
        navigateTo('lobby');
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '40px 20px' }}>
      <button className="btn btn-secondary" style={{ marginBottom: 40 }} onClick={() => { navigateTo('lobby'); playSound('tick'); }}>
        <ChevronLeft size={20} /> QUAY LẠI SẢNH
      </button>

      <div className="sketch-card">
        <div style={{ marginBottom: 32, display: 'flex', alignItems: 'center', gap: 12 }}>
          <BrushStrokeLogo text="HỒ SƠ NGƯỜI CHƠI" fontSize="2rem" />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 40 }}>
          {/* Thông tin */}
          <div>
            <div style={{ marginBottom: 24 }}>
              <div style={{ color: 'var(--text-muted)', fontFamily: "var(--font-accent)", fontSize: '1rem' }}>USERNAME</div>
              <div style={{ fontSize: '2rem', fontFamily: "var(--font-heading)", color: 'var(--ink)', letterSpacing: 2 }}>{user?.username}</div>
            </div>
            
            <div style={{ marginBottom: 24 }}>
              <div style={{ color: 'var(--text-muted)', fontFamily: "var(--font-accent)", fontSize: '1rem' }}>TÊN HIỂN THỊ (TRONG GAME)</div>
              <input 
                type="text" 
                className="input-field" 
                style={{ marginTop: 8 }}
                value={displayName} 
                onChange={(e) => setDisplayName(e.target.value)} 
                placeholder="Nhập tên hiển thị (viết liền không dấu)"
              />
            </div>
            
            <div style={{ marginBottom: 24 }}>
              <div style={{ color: 'var(--text-muted)', fontFamily: "var(--font-accent)", fontSize: '1rem' }}>ID NGƯỜI CHƠI</div>
              <div style={{ fontSize: '1.2rem', fontFamily: "var(--font-heading)", color: 'var(--ink)' }}>#{user?.userId}</div>
            </div>

            <div style={{ marginBottom: 24 }}>
              <div style={{ color: 'var(--text-muted)', fontFamily: "var(--font-accent)", fontSize: '1rem' }}>ĐIỂM XẾP HẠNG</div>
              <div style={{ fontSize: '1.5rem', fontFamily: "var(--font-heading)", color: 'var(--accent-green)' }}>{user?.rankingScore} PTS</div>
            </div>

            <div>
              <div style={{ color: 'var(--text-muted)', fontFamily: "var(--font-accent)", fontSize: '1rem' }}>VAI TRÒ</div>
              <div style={{ display: 'inline-block', padding: '4px 12px', background: user?.role === 'ADMIN' ? 'var(--bg-base)' : 'var(--bg-surface)', color: 'var(--ink)', borderRadius: '255px 15px 225px 15px / 15px 225px 15px 255px', border: '3px solid var(--ink)', fontFamily: "var(--font-heading)", letterSpacing: 1 }}>
                {user?.role === 'ADMIN' ? '👑 ' : ''}{user?.role}
              </div>
            </div>
          </div>

          {/* Chọn Avatar */}
          <div>
            <h3 style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 8, color: 'var(--ink)' }}><ImageIcon size={20} /> CHỌN AVATAR</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 16, marginBottom: 32 }}>
              {AVATARS.map(avatarId => (
                <motion.div 
                  key={avatarId}
                  whileHover={{ scale: 1.1 }}
                  whileTap={{ scale: 0.9 }}
                  onClick={() => { setSelectedAvatar(String(avatarId)); playSound('tick'); }}
                  style={{ 
                    aspectRatio: '1', 
                    borderRadius: '50%', 
                    background: `url(https://api.dicebear.com/7.x/bottts/svg?seed=${avatarId}) center/cover`,
                    backgroundColor: '#fff',
                    border: String(selectedAvatar) === String(avatarId) ? '4px solid var(--accent-green)' : '3px solid var(--ink)',
                    boxShadow: 'none',
                    cursor: 'pointer'
                  }}
                />
              ))}
            </div>

            <button className="btn btn-primary" style={{ width: '100%', padding: 16, fontSize: '1.2rem', fontFamily: "var(--font-heading)", letterSpacing: 2 }} onClick={handleSave}>
              <Save size={20} /> LƯU THAY ĐỔI
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
