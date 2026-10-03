import { createContext, useContext, useState, useCallback } from 'react';
import wsService from '../services/websocket';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const login = useCallback((userData) => {
    setUser({ ...userData, role: userData.role || 'USER', avatar: userData.avatar || '1' });
    setIsLoggedIn(true);
    if (!wsService.isConnected()) {
      wsService.connect();
    }
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    setIsLoggedIn(false);
    localStorage.removeItem('scribe_username');
    localStorage.removeItem('scribe_password');
    localStorage.removeItem('token');
    wsService.disconnect();
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoggedIn, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
