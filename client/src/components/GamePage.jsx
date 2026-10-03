import { useGame } from '../context/GameContext';
import { motion, AnimatePresence } from 'framer-motion';
import { useEffect, useRef } from 'react';
import TopicSelect from './game/TopicSelect';
import DrawingCanvas from './game/DrawingCanvas';
import GuessingView from './game/GuessingView';
import RoundResult from './game/RoundResult';
import GameSummary from './game/GameSummary';
import { playSound } from '../services/sound';
import { LogOut, Clock } from 'lucide-react';

export default function GamePage() {
  const { gamePhase, timer, timerPhase, currentRoom, returnToLobby } = useGame();
  const prevTimerRef = useRef(timer);

  useEffect(() => {
    if (timer > 0 && timer <= 5 && timer !== prevTimerRef.current) {
      playSound('tick');
    }
    if (timer === 0 && prevTimerRef.current > 0) {
      playSound('end');
    }
    prevTimerRef.current = timer;
  }, [timer]);

  if (!gamePhase) return null;

  // Max timer for progress bar calculation
  let maxTime = 60;
  if (gamePhase === 'topicSelect') maxTime = 15;
  if (gamePhase === 'roundResult') maxTime = 5;
  if (gamePhase === 'summary') maxTime = 10;
  const progressPercent = Math.max(0, (timer / maxTime) * 100);

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
      
      {/* HEADER SLIM */}
      {gamePhase !== 'summary' && (
        <header style={{ background: 'var(--bg-panel)', borderBottom: '3px solid var(--ink)', padding: '10px 20px', zIndex: 10 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            
            {/* THÔNG TIN PHÒNG */}
            <div style={{ flex: 1, display: 'flex', alignItems: 'center', gap: 15 }}>
              <span className="mono" style={{ background: 'var(--bg-base)', color: 'var(--ink)', padding: '4px 10px', borderRadius: '255px 15px 225px 15px / 15px 225px 15px 255px', fontSize: '1rem', border: '2px solid var(--ink)' }}>
                {currentRoom?.roomName}
              </span>
              <span style={{ color: 'var(--text-muted)', fontSize: '1rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, fontFamily: "var(--font-accent)" }}>
                {timerPhase}
              </span>
            </div>

            {/* STATUS */}
            <div style={{ flex: 1, textAlign: 'center' }}>
              <h2 style={{ fontSize: '1.4rem', margin: 0, color: 'var(--ink)', letterSpacing: 3, fontFamily: "var(--font-heading)" }}>
                {gamePhase === 'topicSelect' && '✏️ CHỌN CHỦ ĐỀ'}
                {gamePhase === 'drawing' && '🎨 ĐANG VẼ'}
                {gamePhase === 'guessing' && '🔍 ĐANG ĐOÁN'}
                {gamePhase === 'roundResult' && '📊 KẾT QUẢ VÒNG'}
              </h2>
            </div>

            {/* THOÁT TRẬN */}
            <div style={{ flex: 1, display: 'flex', justifyContent: 'flex-end' }}>
              <button className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '0.9rem', borderColor: 'var(--accent-red)', color: 'var(--accent-red)' }} onClick={returnToLobby}>
                <LogOut size={14} style={{ marginRight: 6, display: 'inline' }} /> THOÁT
              </button>
            </div>
          </div>

          {/* PROGRESS BAR */}
          <div style={{ position: 'relative', height: 8, background: 'var(--bg-surface)', marginTop: 10, borderRadius: '255px 15px 225px 15px / 15px 225px 15px 255px', overflow: 'hidden', border: '3px solid var(--ink)' }}>
            <motion.div 
              style={{ 
                height: '100%', 
                background: timer <= 10 ? 'var(--accent-red)' : 'var(--accent-green)',
              }}
              animate={{ width: `${progressPercent}%` }}
              transition={{ duration: 1, ease: 'linear' }}
            />
          </div>
          {/* SỐ GIÂY */}
          <div style={{ textAlign: 'center', marginTop: -15, position: 'relative', zIndex: 11 }}>
            <span style={{ 
              background: 'var(--bg-panel)', padding: '0 10px', fontSize: '1rem', fontWeight: 700, 
              color: timer <= 10 ? 'var(--accent-red)' : 'var(--ink)', display: 'inline-flex', alignItems: 'center', gap: 4,
              fontFamily: "var(--font-heading)", letterSpacing: 2,
              border: '2px solid var(--ink)', borderRadius: '255px 15px 225px 15px / 15px 225px 15px 255px'
            }}>
              <Clock size={14} /> {timer}s
            </span>
          </div>
        </header>
      )}

      {/* GAME CONTENT AREA */}
      <div style={{ flex: 1, display: 'flex', padding: 20, gap: 20, overflow: 'hidden' }}>
        <AnimatePresence mode="wait">
          <motion.div
            key={gamePhase}
            initial={{ opacity: 0, scale: 0.98 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.98 }}
            transition={{ duration: 0.3 }}
            style={{ width: '100%', height: '100%', display: 'flex' }}
          >
            {gamePhase === 'topicSelect' && <TopicSelect />}
            {gamePhase === 'drawing' && <DrawingCanvas />}
            {gamePhase === 'guessing' && <GuessingView />}
            {gamePhase === 'roundResult' && <RoundResult />}
            {gamePhase === 'summary' && <GameSummary />}
          </motion.div>
        </AnimatePresence>
      </div>
    </div>
  );
}
