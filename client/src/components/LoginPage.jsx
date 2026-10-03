import { useState, useEffect } from 'react';
import { useGame } from '../context/GameContext';
import { useAuth } from '../context/AuthContext';
import wsService from '../services/websocket';
import { motion } from 'framer-motion';
import { LogIn, UserPlus, Zap } from 'lucide-react';
import { playSound } from '../services/sound';
import { BrushStrokeLogo, SpeechBubble, MascotSimon } from './ScribbleDecorations';

export default function LoginPage() {
  const { connected, navigateTo } = useGame();
  const { login } = useAuth();
  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState(localStorage.getItem('scribe_username') || '');

  const [password, setPassword] = useState(localStorage.getItem('scribe_password') || '');
  const [autoLoginAttempted, setAutoLoginAttempted] = useState(false);

  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (!autoLoginAttempted) {
      const savedUser = localStorage.getItem('scribe_username');
      const savedPass = localStorage.getItem('scribe_password');
      if (savedUser && savedPass) {
        setUsername(savedUser);
        setPassword(savedPass);
        handleAutoLogin(savedUser, savedPass);
      }
      setAutoLoginAttempted(true);
    }
  }, [autoLoginAttempted]);

  const handleAutoLogin = async (usr, pwd) => {
    try {
      const res = await fetch(`http://localhost:9999/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: usr, password: pwd })
      });
      const data = await res.json();
      if (res.ok) {
        if (data.token) {
          localStorage.setItem('token', data.token);
        }
        login({
          userId: data.userId,
          username: data.username,
          displayName: data.displayName,
          rankingScore: data.rankingScore,
          role: data.role,
          avatar: data.avatar
        });
        navigateTo('lobby');
      }
    } catch (err) {
      console.error('Auto login failed:', err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username || !password) return;
    playSound('tick');
    setErrorMsg('');
    
    try {
      const endpoint = isLogin ? '/api/auth/login' : '/api/auth/register';
      const payload = isLogin ? { username, password } : { username, password };
      const res = await fetch(`http://localhost:9999${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      
      if (res.ok) {
        if (isLogin) {
          localStorage.setItem('scribe_username', username);
          localStorage.setItem('scribe_password', password);
          if (data.token) {
            localStorage.setItem('token', data.token);
          }
          login({
            userId: data.userId,
            username: data.username,
            displayName: data.displayName,
            rankingScore: data.rankingScore,
            role: data.role,
            avatar: data.avatar
          });
          navigateTo('lobby');
          // You could also add a global notification here or in GameContext
        } else {
          setIsLogin(true); // switch to login after register
          // Ideally show success message, we can just set it as errorMsg but green or something, but alert works for simple UI
          alert(data.message);
        }
      } else {
        setErrorMsg(data.message || 'Có lỗi xảy ra');
      }
    } catch (err) {
      setErrorMsg('Không thể kết nối đến máy chủ');
    }
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', padding: 20, gap: 60, flexWrap: 'wrap' }}>
      {/* Left side: Mascot & Welcome Bubble */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20 }}>
        <MascotSimon size={220} />
        <SpeechBubble tailDirection="right" style={{ width: 280, textAlign: 'center' }}>
          <p style={{ fontFamily: "var(--font-accent)", fontSize: '1.2rem', margin: 0, color: 'var(--text-muted)' }}>welcome to</p>
          <div style={{ margin: '12px 0' }}>
            <BrushStrokeLogo text="SCRIBBLE IT!" fontSize="1.8rem" />
          </div>
          <p style={{ fontFamily: "var(--font-accent)", fontSize: '1.1rem', margin: 0 }}>my name is SIMON</p>
          <p style={{ fontFamily: "var(--font-body)", fontSize: '1.05rem', marginTop: 12 }}>
            Let's draw and guess together. Enter your username to start playing!
          </p>
        </SpeechBubble>
      </div>

      {/* Right side: Login Form */}
      <motion.div 
        className="sketch-card" 
        style={{ width: '100%', maxWidth: 440 }}
        initial={{ y: 50, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ type: 'spring', bounce: 0.4, duration: 0.8 }}
      >
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <BrushStrokeLogo text="PLAY NOW" fontSize="2.5rem" />
        </div>

        <div style={{ display: 'flex', gap: 12, marginBottom: 28 }}>
          <button 
            className={`btn ${isLogin ? 'btn-primary' : 'btn-secondary'}`} 
            style={{ flex: 1, padding: '14px 0' }} 
            onClick={() => { setIsLogin(true); playSound('tick'); }}
          >
            <LogIn size={20} /> ĐĂNG NHẬP
          </button>
          <button 
            className={`btn ${!isLogin ? 'btn-primary' : 'btn-secondary'}`} 
            style={{ flex: 1, padding: '14px 0' }} 
            onClick={() => { setIsLogin(false); playSound('tick'); }}
          >
            <UserPlus size={20} /> ĐĂNG KÝ
          </button>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <input
            type="text"
            className="input-field"
            placeholder="TÊN ĐĂNG NHẬP"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
          <input
            type="password"
            className="input-field"
            placeholder="MẬT KHẨU"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          {errorMsg && <p style={{ color: 'var(--accent-red)', textAlign: 'center', margin: 0 }}>{errorMsg}</p>}
          
          <motion.button 
            type="submit" 
            className="btn btn-success" 
            style={{ width: '100%', padding: 18, marginTop: 12, fontSize: '1.4rem', fontFamily: "var(--font-heading)", letterSpacing: 2 }}
            disabled={!connected || !username || !password}
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
          >
            <Zap size={24} /> {isLogin ? 'VÀO GAME' : 'TẠO TÀI KHOẢN'}
          </motion.button>
        </form>

        {!connected && (
          <motion.p 
            animate={{ opacity: [0.5, 1, 0.5] }} 
            transition={{ repeat: Infinity, duration: 1.5 }}
            style={{ color: 'var(--accent-red)', textAlign: 'center', marginTop: 24, fontSize: '1.1rem', fontFamily: "var(--font-accent)" }}
          >
            ✏️ ĐANG KẾT NỐI TỚI MÁY CHỦ...
          </motion.p>
        )}
      </motion.div>
    </div>
  );
}
