import { useState, useRef, useEffect } from 'react';
import { useGame } from '../context/GameContext';
import { useAuth } from '../context/AuthContext';
import wsService from '../services/websocket';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Send, Users, Minus } from 'lucide-react';

export default function ChatWidget() {
  const { activeChatWindows } = useGame();
  
  return (
    <div style={{
      position: 'fixed',
      bottom: 0,
      right: 20,
      display: 'flex',
      flexDirection: 'row-reverse',
      gap: 15,
      zIndex: 9999,
      pointerEvents: 'none', // So we can click through the empty space
    }}>
      <AnimatePresence>
        {activeChatWindows.map((id) => (
          <ChatWindow key={id} id={id} />
        ))}
      </AnimatePresence>
    </div>
  );
}

function ChatWindow({ id }) {
  const { closeChatWindow, chatMessages, roomChat } = useGame();
  const { user } = useAuth();
  const [minimized, setMinimized] = useState(false);
  const [chatInput, setChatInput] = useState('');
  const scrollRef = useRef(null);

  const isRoom = id === 'ROOM';
  const messages = isRoom ? roomChat : (chatMessages[id] || []);
  
  // Find display name for private chat if possible (or just use id)
  const displayName = isRoom ? 'Kênh Chat Phòng' : id;

  useEffect(() => {
    if (!minimized && scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, minimized]);

  const handleSendMessage = (e) => {
    e.preventDefault();
    if (!chatInput.trim()) return;

    if (isRoom) {
      wsService.send('ROOM_CHAT_SEND', { content: chatInput.trim() });
    } else {
      wsService.send('DIRECT_MESSAGE_SEND', {
        targetUsername: id,
        content: chatInput.trim()
      });
    }
    setChatInput('');
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 50 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: 50 }}
      style={{
        width: 320,
        pointerEvents: 'auto', // Re-enable pointer events for the chat box
        display: 'flex',
        flexDirection: 'column',
        alignSelf: 'flex-end',
      }}
    >
      {/* Header */}
      <div 
        onClick={() => setMinimized(!minimized)}
        style={{ 
          padding: '10px 15px', 
          background: isRoom ? 'var(--accent-green)' : 'var(--accent-orange)', 
          border: '3px solid var(--ink)', 
          borderBottom: minimized ? '3px solid var(--ink)' : 'none',
          borderRadius: minimized ? '14px' : '14px 14px 0 0', 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          cursor: 'pointer',
          boxShadow: 'var(--shadow-neo-sm)'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          {isRoom ? <Users size={18} color="var(--ink)" /> : (
            <div style={{ width: 24, height: 24, borderRadius: '50%', background: `url(https://api.dicebear.com/7.x/bottts/svg?seed=${id}) center/cover`, backgroundColor: '#fff', border: '2px solid var(--ink)' }} />
          )}
          <div style={{ fontWeight: 700, fontSize: '1rem', color: 'var(--ink)', fontFamily: "'Bangers', cursive", letterSpacing: 1 }}>
            {displayName}
          </div>
        </div>
        <div style={{ display: 'flex', gap: 5 }}>
          <button className="btn btn-secondary" style={{ padding: 4, border: 'none', background: 'transparent', boxShadow: 'none' }}>
            {minimized ? null : <Minus size={16} />}
          </button>
          <button 
            className="btn btn-secondary" 
            style={{ padding: 4, border: 'none', background: 'transparent', boxShadow: 'none' }} 
            onClick={(e) => {
              e.stopPropagation();
              closeChatWindow(id);
            }}
          >
            <X size={18} />
          </button>
        </div>
      </div>
      
      {/* Body (Messages & Input) */}
      {!minimized && (
        <div style={{
          background: 'var(--bg-panel)',
          border: '3px solid var(--ink)',
          borderTop: 'none',
          borderRadius: '0 0 14px 14px',
          boxShadow: 'var(--shadow-neo-sm)',
          display: 'flex',
          flexDirection: 'column',
          height: 350
        }}>
          {/* Messages */}
          <div ref={scrollRef} style={{ flex: 1, overflowY: 'auto', padding: 15, display: 'flex', flexDirection: 'column', gap: 10, background: 'var(--bg-surface)' }}>
            {messages.map((msg, i) => {
              const isSelf = isRoom ? (msg.username === user?.username) : msg.isSelf;
              const showSender = isRoom && !isSelf;
              
              return (
                <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: isSelf ? 'flex-end' : 'flex-start' }}>
                  {showSender && <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: 2, marginLeft: 4, fontWeight: 600 }}>{msg.displayName}</span>}
                  <div style={{
                    background: isSelf ? 'var(--ink)' : 'var(--bg-base)',
                    color: isSelf ? 'var(--bg-surface)' : 'var(--ink)', 
                    padding: '8px 12px', 
                    borderRadius: 12,
                    borderBottomRightRadius: isSelf ? 2 : 12, 
                    borderBottomLeftRadius: isSelf ? 12 : 2,
                    maxWidth: '85%', 
                    wordBreak: 'break-word', 
                    fontSize: '0.95rem',
                    border: '2px solid var(--ink)'
                  }}>
                    {msg.content}
                  </div>
                </div>
              );
            })}
            {messages.length === 0 && (
              <div style={{ textAlign: 'center', color: 'var(--text-muted)', marginTop: 40, fontSize: '0.9rem', fontFamily: "'Caveat', cursive" }}>
                Chưa có tin nhắn nào... ✏️
              </div>
            )}
          </div>

          {/* Input */}
          <form onSubmit={handleSendMessage} style={{ padding: 10, borderTop: '3px solid var(--ink)', display: 'flex', gap: 8, background: 'var(--bg-panel)', borderRadius: '0 0 12px 12px' }}>
            <input 
              type="text" 
              className="input-field" 
              placeholder="Nhập tin nhắn..." 
              value={chatInput} 
              onChange={(e) => setChatInput(e.target.value)} 
              style={{ flex: 1, margin: 0, padding: '8px 12px', fontSize: '0.95rem', boxShadow: 'none' }} 
            />
            <button type="submit" className="btn btn-primary" style={{ padding: '0 12px' }} disabled={!chatInput.trim()}>
              <Send size={16} />
            </button>
          </form>
        </div>
      )}
    </motion.div>
  );
}
