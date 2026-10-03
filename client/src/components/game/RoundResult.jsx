import { useGame } from '../../context/GameContext';
import { motion } from 'framer-motion';
import { Trophy, CheckCircle, Paintbrush } from 'lucide-react';

export default function RoundResult() {
  const { roundResult, roomPlayers } = useGame();

  if (!roundResult) return null;

  const painterDisplayName = roomPlayers.find(p => p.username === roundResult.painterUsername)?.displayName || roundResult.painterUsername;

  return (
    <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <motion.div 
        initial={{ opacity: 0, scale: 0.9, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        className="glass-card" 
        style={{ width: 600, textAlign: 'center', padding: '40px 30px', border: '3px solid var(--ink)', boxShadow: 'var(--shadow-neo)' }}
      >
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 20 }}>
          <div style={{ background: 'var(--bg-base)', padding: 15, borderRadius: '50%', border: '3px solid var(--ink)', boxShadow: 'var(--shadow-neo-sm)' }}>
            <Paintbrush size={40} color="var(--ink)" />
          </div>
        </div>
        
        <h2 style={{ marginBottom: 10, color: 'var(--text-muted)', fontSize: '1rem', letterSpacing: 2, fontFamily: "'Caveat', cursive" }}>KẾT THÚC LƯỢT VẼ CỦA</h2>
        <h3 style={{ fontSize: '2rem', marginBottom: 30, color: 'var(--ink)', fontFamily: "'Bangers', cursive", letterSpacing: 3 }}>{painterDisplayName}</h3>
        
        <div style={{ display: 'inline-block', padding: '20px 40px', background: 'var(--bg-base)', border: '3px solid var(--ink)', borderRadius: 20, marginBottom: 40, boxShadow: 'var(--shadow-neo)' }}>
          <div style={{ fontSize: '1rem', color: 'var(--ink)', marginBottom: 8, fontFamily: "'Caveat', cursive", letterSpacing: 2 }}>✅ ĐÁP ÁN CHÍNH XÁC</div>
          <div style={{ fontSize: '2.5rem', fontFamily: "'Bangers', cursive", color: 'var(--ink)', letterSpacing: 5 }}>
            {roundResult.topic}
          </div>
        </div>

        <div style={{ textAlign: 'left', background: 'var(--bg-surface)', padding: 25, borderRadius: 16, border: '2px solid var(--ink)' }}>
          <h4 style={{ marginBottom: 15, color: 'var(--ink)', display: 'flex', alignItems: 'center', gap: 10, fontFamily: "'Bangers', cursive", letterSpacing: 2 }}>
            <Trophy size={20} /> 🏆 ĐIỂM SỐ VÒNG NÀY
          </h4>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {roundResult.rankings.map((rank, index) => {
              const rankDisplayName = roomPlayers.find(p => p.username === rank.username)?.displayName || rank.username;
              const isPainter = rank.username === roundResult.painterUsername;
              return (
                <div key={index} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 15px', background: 'var(--bg-panel)', borderRadius: 8, border: '2px solid var(--ink)' }}>
                  <span style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: 8, color: 'var(--ink)' }}>
                    {rankDisplayName} 
                    {isPainter && <span style={{ fontSize: '0.75rem', background: 'var(--bg-base)', color: 'var(--ink)', padding: '2px 8px', borderRadius: 10, fontFamily: "'Bangers', cursive", border: '1px solid var(--ink)' }}>🎨 VẼ</span>}
                  </span>
                  <span style={{ color: 'var(--accent-green)', fontWeight: 800, display: 'flex', alignItems: 'center', gap: 5, fontFamily: "'Bangers', cursive" }}>
                    <CheckCircle size={16} /> +{rank.pointsGuess || rank.pointsDrawn} pts
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      </motion.div>
    </div>
  );
}
