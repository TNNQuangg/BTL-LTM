import { AuthProvider } from './context/AuthContext';
import { GameProvider, useGame } from './context/GameContext';
import { AnimatePresence, motion } from 'framer-motion';
import LoginPage from './components/LoginPage';
import LobbyPage from './components/LobbyPage';
import RoomPage from './components/RoomPage';
import GamePage from './components/GamePage';
import LeaderboardPage from './components/LeaderboardPage';
import AdminDashboard from './components/AdminDashboard';
import ProfilePage from './components/ProfilePage';
import Notification from './components/Notification';
import ChatWidget from './components/ChatWidget';
import { ScribbleCircles } from './components/ScribbleDecorations';
import './index.css';

const pageVariants = {
  initial: { opacity: 0, y: 20, scale: 0.98 },
  animate: { opacity: 1, y: 0, scale: 1, transition: { duration: 0.4, ease: 'easeOut' } },
  exit: { opacity: 0, y: -20, scale: 0.98, transition: { duration: 0.2 } }
};

function AppContent() {
  const { currentPage, notification } = useGame();

  return (
    <div className="app">
      <ScribbleCircles count={30} />
      
      <AnimatePresence mode="wait">
        <motion.div
          key={currentPage}
          variants={pageVariants}
          initial="initial"
          animate="animate"
          exit="exit"
          style={{ width: '100%', minHeight: '100vh' }}
        >
          {currentPage === 'login' && <LoginPage />}
          {currentPage === 'lobby' && <LobbyPage />}
          {currentPage === 'room' && <RoomPage />}
          {currentPage === 'game' && <GamePage />}
          {currentPage === 'leaderboard' && <LeaderboardPage />}
          {currentPage === 'admin' && <AdminDashboard />}
          {currentPage === 'profile' && <ProfilePage />}
        </motion.div>
      </AnimatePresence>

      <AnimatePresence>
        {notification && <Notification message={notification.message} type={notification.type} />}
      </AnimatePresence>
      
      {/* Khung chat toàn cục */}
      <ChatWidget />
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <GameProvider>
        <AppContent />
      </GameProvider>
    </AuthProvider>
  );
}

export default App;
