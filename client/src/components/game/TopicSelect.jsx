import { useGame } from '../../context/GameContext';
import wsService from '../../services/websocket';
import { useAuth } from '../../context/AuthContext';
import { motion } from 'framer-motion';
import { Sparkles } from 'lucide-react';
import { playSound } from '../../services/sound';

export default function TopicSelect() {
  const { topicOptions, selectedTopic, playerOrder } = useGame();
  const { user } = useAuth();
  
  const isDrawer = playerOrder[0] === user?.username;

  const handleSelectTopic = (topic) => {
    if (!selectedTopic && isDrawer) {
      wsService.send('TOPIC_SELECT', { topic });
      playSound('correct');
    }
  };

  if (selectedTopic) {
    return (
      <div className="glass-card" style={{ textAlign: 'center', padding: '80px 20px' }}>
        <motion.div initial={{ scale: 0.5, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} transition={{ type: 'spring' }}>
          <h2 style={{ marginBottom: 24, color: 'var(--ink)' }}>✅ CHỦ ĐỀ ĐÃ ĐƯỢC CHỌN</h2>
          <div style={{ fontSize: '3rem', fontFamily: "'Bangers', cursive", color: 'var(--ink)', letterSpacing: 6, background: 'var(--bg-base)', display: 'inline-block', padding: '10px 30px', border: '3px solid var(--ink)', borderRadius: 12, boxShadow: 'var(--shadow-neo)' }}>
            {selectedTopic}
          </div>
          <p style={{ marginTop: 24, color: 'var(--text-muted)', fontFamily: "'Caveat', cursive", fontSize: '1.3rem' }}>CHUẨN BỊ VẼ... ✏️</p>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="glass-card">
      <div style={{ textAlign: 'center', marginBottom: 40 }}>
        <h2 style={{ color: 'var(--ink)' }}>{isDrawer ? '✏️ CHỌN MỘT CHỦ ĐỀ' : `⏳ ĐANG CHỜ ${playerOrder[0]} CHỌN...`}</h2>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 24, maxWidth: 900, margin: '0 auto' }}>
        {topicOptions.map((topic, index) => (
          <motion.div 
            key={index} 
            whileHover={isDrawer ? { scale: 1.05, y: -10 } : {}}
            whileTap={isDrawer ? { scale: 0.95 } : {}}
            initial={{ rotateY: 90, opacity: 0 }}
            animate={{ rotateY: 0, opacity: 1 }}
            transition={{ delay: index * 0.15, duration: 0.5 }}
            onClick={() => handleSelectTopic(topic)}
            style={{ 
              background: 'var(--bg-surface)', border: '3px solid var(--ink)', 
              borderRadius: 'var(--radius-lg)', padding: '40px 20px', textAlign: 'center',
              cursor: isDrawer ? 'pointer' : 'default', opacity: isDrawer ? 1 : 0.5,
              boxShadow: 'var(--shadow-neo)'
            }}
          >
            <Sparkles size={40} color="var(--accent-orange)" style={{ margin: '0 auto 16px' }} />
            <div style={{ fontSize: '1.5rem', fontFamily: "'Bangers', cursive", color: 'var(--ink)', letterSpacing: 2 }}>{isDrawer ? topic : '???'}</div>
          </motion.div>
        ))}
      </div>
    </div>
  );
}
