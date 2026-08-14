import React, { createContext, useContext, useState, useEffect } from 'react';
import { loginUser, validateToken } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('auth_token') || null);
  const [user, setUser] = useState(localStorage.getItem('user_name') || null);
  const [isAuthenticated, setIsAuthenticated] = useState(Boolean(token));
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const checkTokenValidity = async () => {
      if (token) {
        try {
          const res = await validateToken(token);
          if (res && res.valid) {
            setIsAuthenticated(true);
            if (res.username) setUser(res.username);
          } else {
            logout();
          }
        } catch (err) {
          console.warn('Token validation failed on startup:', err.message);
          // Keep existing token for offline dev or fallback if needed
        }
      }
    };
    checkTokenValidity();
  }, [token]);

  const login = async (username, password) => {
    setLoading(true);
    try {
      const data = await loginUser(username, password);
      if (data && data.jwtToken && data.valid) {
        const fullToken = data.tokenType ? `${data.tokenType} ${data.jwtToken}` : `Bearer ${data.jwtToken}`;
        setToken(fullToken);
        setUser(username);
        setIsAuthenticated(true);
        localStorage.setItem('auth_token', fullToken);
        localStorage.setItem('user_name', username);
        setLoading(false);
        return { success: true };
      } else {
        setLoading(false);
        return { success: false, message: 'Invalid username or password' };
      }
    } catch (error) {
      setLoading(false);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to authenticate with Server (Port 8084)'
      };
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    setIsAuthenticated(false);
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_name');
  };

  return (
    <AuthContext.Provider value={{ token, user, isAuthenticated, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
