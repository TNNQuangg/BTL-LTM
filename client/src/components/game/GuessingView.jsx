import { useRef, useState, useEffect } from 'react';
import wsService from '../../services/websocket';
import { useGame } from '../../context/GameContext';
import { useAuth } from '../../context/AuthContext';
import { motion, AnimatePresence } from 'framer-motion';
import { Send, Eye, Trophy, MessageSquare, Pencil } from 'lucide-react';
import { playSound } from '../../services/sound';

export default function GuessingView() {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const { currentPainting, hint, guessLogs, rankings, paintingInfo, roomPlayers } = useGame();
  const { user } = useAuth();
  const [guessInput, setGuessInput] = useState('');
  
  const drawnDataCount = useRef(0);

  useEffect(() => {
    const canvas = canvasRef.current;
    const container = containerRef.current;
    if (canvas && container) {
      canvas.width = container.clientWidth;
      canvas.height = container.clientHeight;
      const ctx = canvas.getContext('2d');
      ctx.fillStyle = '#FFFFFF';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
    }
    drawnDataCount.current = 0;
  }, []);

  useEffect(() => {
    if (currentPainting && currentPainting.drawData && canvasRef.current) {
      const ctx = canvasRef.current.getContext('2d');
      const newData = currentPainting.drawData.slice(drawnDataCount.current);
      
      newData.forEach(data => {
        if (data.clear) {
          ctx.fillStyle = '#FFFFFF';
          ctx.fillRect(0, 0, canvasRef.current.width, canvasRef.current.height);
          return;
        }
        if (!data.xPoints || data.xPoints.length === 0) return;
        ctx.beginPath();
        ctx.moveTo(data.xPoints[0], data.yPoints[0]);
        for (let i = 1; i < data.xPoints.length; i++) {
          ctx.lineTo(data.xPoints[i], data.yPoints[i]);
        }
        const colorHex = '#' + data.colorRGB.toString(16).padStart(6, '0');
        ctx.strokeStyle = data.isEraser ? '#FFFFFF' : colorHex;
        ctx.lineWidth = data.strokeWidth;
        ctx.stroke();
      });
      drawnDataCount.current = currentPainting.drawData.length;
    }
  }, [currentPainting]);

  const chatEndRef = useRef(null);
  useEffect(() => { chatEndRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [guessLogs]);

  const handleGuessSubmit = (e) => {
    e.preventDefault();
    if (guessInput.trim()) {
      wsService.send('GUESS_SUBMIT', {}, guessInput.trim());
      setGuessInput('');
      playSound('tick');
    }
  };

  const isMyPainting = paintingInfo.painter === user?.username;
  const hasGuessedCorrectly = guessLogs.some(log => log.username === user?.username && log.correct);
  const painterDisplayName = roomPlayers.find(p => p.username === paintingInfo.painter)?.displayName || paintingInfo.painter;
  
  // Play sound when I guess right
  useEffect(() => {
    if (hasGuessedCorrectly) {
      playSound('correct');
    }
  }, [hasGuessedCorrectly]);

  return (
    <div style={{ flex: 1, display: 'flex', gap: 20, height: '100%' }}>
      
      {/* CỘT TRÁI: BẢNG VẼ & THÔNG TIN */}
      <div style={{ flex: '1 1 0', display: 'flex', flexDirection: 'column', gap: 15, minWidth: 0 }}>
        <div className="glass-card" style={{ padding: '15px 25px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 15 }}>
            <div style={{ position: 'relative' }}>
              <div style={{ width: 48, height: 48, borderRadius: '50%', background: `url(https://api.dicebear.com/7.x/bottts/svg?seed=${paintingInfo.painter || '1'}) center/cover`, backgroundColor: '#fff', border: '3px solid var(--ink)' }} />
              <div style={{ position: 'absolute', bottom: -5, right: -5, background: 'var(--accent-red)', borderRadius: '50%', width: 24, height: 24, display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px solid var(--ink)' }}>
                <Pencil size={12} color="#fff" />
              </div>
            </div>
            <div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontFamily: "'Caveat', cursive", letterSpacing: 1 }}>🎨 ĐANG VẼ</div>
              <div style={{ fontSize: '1.2rem', fontFamily: "'Bangers', cursive", color: 'var(--ink)', letterSpacing: 2 }}>{painterDisplayName}</div>
            </div>
          </div>

          <div style={{ textAlign: 'right', background: 'var(--bg-base)', padding: '10px 20px', borderRadius: 12, border: '2px solid var(--ink)', boxShadow: 'var(--shadow-neo-sm)' }}>
            <div style={{ color: 'var(--ink)', fontSize: '0.85rem', fontFamily: "'Caveat', cursive", marginBottom: 5 }}>🔍 GỢI Ý</div>
            <div className="mono" style={{ fontSize: '1.8rem', letterSpacing: 8, color: 'var(--ink)', fontFamily: "'Bangers', cursive" }}>{hint}</div>
          </div>
        </div>

        <div 
          ref={containerRef}
          style={{ 
            flex: 1,
            background: '#ffffff', 
            borderRadius: 24, 
            overflow: 'hidden',
            boxShadow: 'var(--shadow-neo)',
            border: '4px solid var(--ink)'
          }}
        >
          <canvas ref={canvasRef} style={{ width: '100%', height: '100%', display: 'block', cursor: 'default' }} />
        </div>
      </div>

      {/* CỘT PHẢI: BẢNG XẾP HẠNG & CHAT */}
      <div style={{ width: 350, display: 'flex', flexDirection: 'column', gap: 15 }}>
        
        {/* LEADERBOARD */}
        <div className="glass-card" style={{ padding: 15 }}>
          <h4 style={{ marginBottom: 12, color: 'var(--ink)', display: 'flex', alignItems: 'center', gap: 8, fontSize: '1rem', fontFamily: "'Bangers', cursive", letterSpacing: 2 }}>
            <Trophy size={16} /> 🏆 BẢNG XẾP HẠNG
          </h4>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, maxHeight: 150, overflowY: 'auto', paddingRight: 5 }}>
            <AnimatePresence>
              {rankings.map((rank, index) => {
                const rankDisplayName = roomPlayers.find(p => p.username === rank.username)?.displayName || rank.username;
                const isTop = index === 0;
                return (
                  <motion.div 
                    layout initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} key={rank.username}
                    style={{ 
                      display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                      padding: '8px 12px', background: isTop ? 'var(--bg-base)' : 'var(--bg-surface)', 
                      borderRadius: 8, borderLeft: `4px solid ${isTop ? 'var(--accent-orange)' : 'var(--ink)'}`,
                      border: '2px solid var(--ink)'
                    }}
                  >
                    <span style={{ fontWeight: 600, fontSize: '0.95rem', color: 'var(--ink)' }}>#{index + 1} {rankDisplayName}</span>
                    <span style={{ color: 'var(--accent-green)', fontWeight: 700, fontSize: '0.9rem', fontFamily: "'Bangers', cursive" }}>+{rank.pointsGuess || rank.pointsDrawn} pts</span>
                  </motion.div>
                );
              })}
            </AnimatePresence>
            {rankings.length === 0 && <div style={{ color: 'var(--text-muted)', fontSize: '0.95rem', textAlign: 'center', padding: 10, fontFamily: "'Caveat', cursive" }}>Chưa ai đoán đúng... ✏️</div>}
          </div>
        </div>

        {/* CHAT / GUESS STREAM */}
        <div className="glass-card" style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: 15, overflow: 'hidden' }}>
          <h4 style={{ marginBottom: 12, color: 'var(--ink)', display: 'flex', alignItems: 'center', gap: 8, fontSize: '1rem', fontFamily: "'Bangers', cursive", letterSpacing: 2 }}>
            <MessageSquare size={16} /> 💬 LIVE CHAT
          </h4>
          
          <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 8, paddingRight: 5, marginBottom: 15 }}>
            {guessLogs.map((log, index) => {
              const logDisplayName = roomPlayers.find(p => p.username === log.username)?.displayName || log.username;
              return (
                <motion.div 
                  initial={{ opacity: 0, x: -10 }} animate={{ opacity: 1, x: 0 }} key={index} 
                  style={{ 
                    padding: '8px 12px', borderRadius: 12, fontSize: '1rem',
                    background: log.correct ? '#dcfce7' : 'transparent',
                    border: log.correct ? '2px solid var(--accent-green)' : 'none',
                  }}
                >
                  <span style={{ fontWeight: 700, color: log.correct ? 'var(--accent-green)' : 'var(--text-muted)' }}>{logDisplayName}</span>
                  <span style={{ color: 'var(--ink)', margin: '0 5px' }}>:</span>
                  <span style={{ color: log.correct ? 'var(--accent-green)' : 'var(--ink)', fontWeight: log.correct ? 700 : 400 }}>
                    {log.correct ? `ĐÃ ĐOÁN ĐÚNG! 🎉` : log.content}
                  </span>
                </motion.div>
              );
            })}
            <div ref={chatEndRef} />
          </div>

          {/* CHAT INPUT */}
          <div style={{ background: 'var(--bg-surface)', padding: 5, borderRadius: 12, border: '2px solid var(--ink)' }}>
            {!isMyPainting ? (
              <form onSubmit={handleGuessSubmit} style={{ display: 'flex' }}>
                <input 
                  type="text" 
                  className="input-field" 
                  placeholder={hasGuessedCorrectly ? "BẠN ĐÃ ĐOÁN ĐÚNG! ✅" : "Nhập dự đoán..."} 
                  value={guessInput} 
                  onChange={(e) => setGuessInput(e.target.value)} 
                  disabled={hasGuessedCorrectly} 
                  autoFocus 
                  style={{ flex: 1, margin: 0, border: 'none', background: 'transparent', padding: '10px 15px', boxShadow: 'none' }} 
                />
                <button 
                  type="submit" 
                  className="btn btn-primary" 
                  disabled={!guessInput.trim() || hasGuessedCorrectly} 
                  style={{ padding: '0 20px', borderRadius: 8 }}
                >
                  <Send size={18} />
                </button>
              </form>
            ) : (
              <div style={{ padding: '12px', textAlign: 'center', color: 'var(--ink)', fontFamily: "'Bangers', cursive", fontSize: '1rem', letterSpacing: 2 }}>
                <Eye size={18} style={{ display: 'inline', verticalAlign: 'text-bottom', marginRight: 5 }}/> 🎨 BẠN ĐANG VẼ
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

// NOTE: Missing pencil import on top, wait, Pencil is imported from lucide-react? No, I need to add Pencil to the import.
