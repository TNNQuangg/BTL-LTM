import { useState, useEffect } from 'react';
import wsService from '../services/websocket';
import { useGame } from '../context/GameContext';
import { motion } from 'framer-motion';
import { Trophy, ChevronLeft, Medal } from 'lucide-react';
import { playSound } from '../services/sound';
import { BrushStrokeLogo } from './ScribbleDecorations';

export default function LeaderboardPage() {
  const { navigateTo } = useGame();
  const [leaderboard, setLeaderboard] = useState([]);

  useEffect(() => {
    fetch('http://localhost:9999/api/leaderboard')
      .then(res => res.json())
      .then(data => {
        setLeaderboard(data);
      })
      .catch(err => console.error(err));
  }, []);

  const getMedalEmoji = (index) => {
    if (index === 0) return '🥇';
    if (index === 1) return '🥈';
    if (index === 2) return '🥉';
    return '';
  };

  const getMedalBorder = (index) => {
    if (index === 0) return 'var(--accent-orange)';
    if (index === 1) return 'var(--ink)';
    if (index === 2) return 'var(--accent-red)';
    return 'var(--ink)';
  };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '40px 20px' }}>
      <button className="btn btn-secondary" style={{ marginBottom: 40 }} onClick={() => { navigateTo('lobby'); playSound('tick'); }}>
        <ChevronLeft size={20} /> TRỞ VỀ SẢNH
      </button>

      <div className="sketch-card">
        <div style={{ textAlign: 'center', marginBottom: 40 }}>
          <BrushStrokeLogo text="BẢNG XẾP HẠNG" fontSize="2.5rem" />
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {leaderboard.length === 0 ? (
            <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 40, fontFamily: "var(--font-accent)", fontSize: '1.3rem' }}>
              CHƯA CÓ DỮ LIỆU... ✏️
            </div>
          ) : (
            leaderboard.map((user, index) => (
              <motion.div 
                key={user.userId} 
                initial={{ opacity: 0, x: -50 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: index * 0.1 }}
                whileHover={{ scale: 1.02, x: 10 }}
                style={{ 
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '20px 24px',
                  background: 'var(--bg-surface)', borderRadius: '255px 15px 225px 15px / 15px 225px 15px 255px', border: '3px solid var(--ink)',
                  borderLeftWidth: index < 3 ? '6px' : '3px', borderLeftColor: index < 3 ? getMedalBorder(index) : 'var(--ink)',
                  boxShadow: 'none'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
                  <div style={{ width: 40, textAlign: 'center', fontWeight: 800, fontSize: '1.4rem', color: 'var(--ink)', fontFamily: "var(--font-heading)" }}>
                    {index < 3 ? <span style={{ fontSize: '1.8rem' }}>{getMedalEmoji(index)}</span> : `#${index + 1}`}
                  </div>
                  <div className="avatar-ring" style={{ width: 48, height: 48, fontSize: '1.2rem', margin: 0 }}>
                    {(user.displayName || user.username).charAt(0).toUpperCase()}
                  </div>
                  <span style={{ fontWeight: 700, fontSize: '1.2rem', color: 'var(--ink)' }}>
                    {user.displayName || user.username}
                  </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 32, textAlign: 'right' }}>
                  <div>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }} className="mono">SỐ TRẬN</div>
                    <div style={{ fontWeight: 700, color: 'var(--ink)' }}>{user.matchesPlayed}</div>
                  </div>
                  <div>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }} className="mono">ĐIỂM SỐ</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--accent-green)', fontFamily: "var(--font-heading)" }}>{user.rankingScore}</div>
                  </div>
                </div>
              </motion.div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
