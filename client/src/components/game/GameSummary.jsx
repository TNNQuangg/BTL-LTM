import { useGame } from '../../context/GameContext';
import { motion } from 'framer-motion';

export default function GameSummary() {
  const { gameResult, returnToLobby, roomPlayers } = useGame();

  if (!gameResult) return null;

  const results = [...(gameResult.results || [])].sort((a, b) => (b.pointsDrawn + b.pointsGuess) - (a.pointsDrawn + a.pointsGuess));
  
  const getDisplayName = (username) => {
    return roomPlayers.find(p => p.username === username)?.displayName || username;
  };

  const top1 = results[0];
  const top2 = results[1];
  const top3 = results[2];

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto', textAlign: 'center' }}>
      <motion.h1 
        initial={{ scale: 0.5, opacity: 0 }} animate={{ scale: 1, opacity: 1 }}
        style={{ fontSize: '3rem', marginBottom: 40, color: 'var(--ink)', fontFamily: "'Bangers', cursive", letterSpacing: 5 }}
      >
        🏆 TỔNG KẾT TRẬN ĐẤU
      </motion.h1>

      <div className="podium-container">
        {top2 && (
          <motion.div className="podium-bar podium-2" initial={{ height: 0 }} animate={{ height: 180 }} transition={{ duration: 1, delay: 0.5 }}>
            <div style={{ position: 'absolute', top: -50, fontFamily: "'Bangers', cursive", fontSize: '1.2rem', color: 'var(--ink)' }}>{getDisplayName(top2.username)}</div>
            <div style={{ fontSize: '2rem', fontFamily: "'Bangers', cursive", color: 'var(--ink)' }}>🥈 #2</div>
            <div style={{ fontFamily: "'Caveat', cursive", fontSize: '1.1rem', color: 'var(--ink)' }}>{top2.pointsDrawn + top2.pointsGuess} PTS</div>
          </motion.div>
        )}
        
        {top1 && (
          <motion.div className="podium-bar podium-1" initial={{ height: 0 }} animate={{ height: 250 }} transition={{ duration: 1, delay: 1 }}>
            <div style={{ position: 'absolute', top: -60, fontFamily: "'Bangers', cursive", fontSize: '1.5rem', color: 'var(--ink)' }}>{getDisplayName(top1.username)}</div>
            <div style={{ fontSize: '3rem', fontFamily: "'Bangers', cursive", color: 'var(--ink)' }}>🥇 #1</div>
            <div style={{ fontFamily: "'Caveat', cursive", fontSize: '1.3rem', color: 'var(--ink)' }}>{top1.pointsDrawn + top1.pointsGuess} PTS</div>
          </motion.div>
        )}

        {top3 && (
          <motion.div className="podium-bar podium-3" initial={{ height: 0 }} animate={{ height: 140 }} transition={{ duration: 1, delay: 0.2 }}>
            <div style={{ position: 'absolute', top: -40, fontFamily: "'Bangers', cursive", fontSize: '1rem', color: 'var(--ink)' }}>{getDisplayName(top3.username)}</div>
            <div style={{ fontSize: '1.8rem', fontFamily: "'Bangers', cursive", color: 'var(--ink)' }}>🥉 #3</div>
            <div style={{ fontFamily: "'Caveat', cursive", fontSize: '1rem', color: 'var(--ink)' }}>{top3.pointsDrawn + top3.pointsGuess} PTS</div>
          </motion.div>
        )}
      </div>

      <motion.div initial={{ opacity: 0, y: 50 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 2 }} className="glass-card" style={{ padding: 0, overflow: 'hidden', marginBottom: 32 }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr style={{ background: 'var(--bg-base)' }}>
              <th style={{ padding: '16px 24px', fontFamily: "'Bangers', cursive", letterSpacing: 1, color: 'var(--ink)' }}>HẠNG</th>
              <th style={{ padding: '16px 24px', fontFamily: "'Bangers', cursive", letterSpacing: 1, color: 'var(--ink)' }}>NGƯỜI CHƠI</th>
              <th style={{ padding: '16px 24px', textAlign: 'center', fontFamily: "'Bangers', cursive", letterSpacing: 1, color: 'var(--ink)' }}>ĐIỂM VẼ</th>
              <th style={{ padding: '16px 24px', textAlign: 'center', fontFamily: "'Bangers', cursive", letterSpacing: 1, color: 'var(--ink)' }}>ĐIỂM ĐOÁN</th>
              <th style={{ padding: '16px 24px', textAlign: 'right', fontFamily: "'Bangers', cursive", letterSpacing: 1, color: 'var(--accent-green)' }}>TỔNG CỘNG</th>
            </tr>
          </thead>
          <tbody>
            {results.map((r, index) => {
              const total = r.pointsDrawn + r.pointsGuess;
              return (
                <tr key={r.username} style={{ borderTop: '2px solid var(--ink)' }}>
                  <td style={{ padding: '16px 24px', fontFamily: "'Bangers', cursive", color: 'var(--ink)' }}>#{index + 1}</td>
                  <td style={{ padding: '16px 24px', color: 'var(--ink)' }}>{getDisplayName(r.username)}</td>
                  <td style={{ padding: '16px 24px', textAlign: 'center', color: 'var(--ink)' }}>{r.pointsDrawn}</td>
                  <td style={{ padding: '16px 24px', textAlign: 'center', color: 'var(--ink)' }}>{r.pointsGuess}</td>
                  <td style={{ padding: '16px 24px', textAlign: 'right', fontFamily: "'Bangers', cursive", color: 'var(--accent-green)' }}>{total}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </motion.div>

      <motion.button initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 2.5 }} className="btn btn-primary" style={{ padding: '16px 40px', fontSize: '1.3rem', fontFamily: "'Bangers', cursive", letterSpacing: 3 }} onClick={returnToLobby}>
        QUAY LẠI LOBBY
      </motion.button>
    </div>
  );
}
