import React, { useState } from 'react';
import { X, LogIn, UserPlus, Lock, Mail, User as UserIcon, Loader2 } from 'lucide-react';
import { api, setAuthToken, setStoredUser } from '../api';

export default function AuthModal({ onClose, onSuccess }) {
  const [mode, setMode] = useState('login'); // 'login' or 'register'
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    setLoading(true);

    try {
      if (mode === 'login') {
        const res = await api.login(username, password);
        if (res.token) {
          setAuthToken(res.token);
          const userObj = res.user || { username: username };
          setStoredUser(userObj);
          onSuccess(userObj);
          onClose();
        } else {
          setError('Invalid login response');
        }
      } else {
        // Register
        if (!email.includes('@')) {
          setError('Please provide a valid email address');
          setLoading(false);
          return;
        }
        await api.register(username, email, password);
        setSuccessMessage('Account created successfully! Logging you in...');
        
        // Auto-login after registration
        const loginRes = await api.login(username, password);
        if (loginRes.token) {
          setAuthToken(loginRes.token);
          const userObj = loginRes.user || { username, email };
          setStoredUser(userObj);
          onSuccess(userObj);
          setTimeout(() => onClose(), 600);
        } else {
          setMode('login');
        }
      }
    } catch (err) {
      console.error(err);
      setError(err.message || 'Authentication failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content auth-modal-card" onClick={(e) => e.stopPropagation()}>
        <button className="modal-close-btn" onClick={onClose}>
          <X size={20} />
        </button>

        <div className="auth-header">
          <h2>{mode === 'login' ? 'Welcome Back' : 'Create Account'}</h2>
          <p>{mode === 'login' ? 'Sign in to report and manage items' : 'Join Lostoria to report and recover lost items'}</p>
        </div>

        {/* Tab switch */}
        <div className="auth-tabs">
          <button
            type="button"
            className={`auth-tab ${mode === 'login' ? 'active' : ''}`}
            onClick={() => {
              setMode('login');
              setError('');
            }}
          >
            <LogIn size={16} />
            <span>Sign In</span>
          </button>
          <button
            type="button"
            className={`auth-tab ${mode === 'register' ? 'active' : ''}`}
            onClick={() => {
              setMode('register');
              setError('');
            }}
          >
            <UserPlus size={16} />
            <span>Register</span>
          </button>
        </div>

        {error && <div className="form-error-banner">{error}</div>}
        {successMessage && <div className="form-success-banner">{successMessage}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label className="form-label">{mode === 'login' ? 'Username or Email' : 'Username'}</label>
            <div className="input-icon-wrapper">
              <UserIcon size={18} className="input-icon" />
              <input
                type="text"
                className="form-input with-icon"
                placeholder={mode === 'login' ? 'Enter username or email' : 'Choose a username'}
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
          </div>

          {mode === 'register' && (
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <div className="input-icon-wrapper">
                <Mail size={18} className="input-icon" />
                <input
                  type="email"
                  className="form-input with-icon"
                  placeholder="your.email@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Password</label>
            <div className="input-icon-wrapper">
              <Lock size={18} className="input-icon" />
              <input
                type="password"
                className="form-input with-icon"
                placeholder="Enter password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? (
              <>
                <Loader2 size={18} className="animate-spin" />
                <span>{mode === 'login' ? 'Signing in...' : 'Creating account...'}</span>
              </>
            ) : (
              <span>{mode === 'login' ? 'Sign In to Lostoria' : 'Create Free Account'}</span>
            )}
          </button>
        </form>

        <div className="auth-footer-hint">
          {mode === 'login' ? (
            <p>
              Don't have an account?{' '}
              <button type="button" className="link-btn" onClick={() => setMode('register')}>
                Register now
              </button>
            </p>
          ) : (
            <p>
              Already have an account?{' '}
              <button type="button" className="link-btn" onClick={() => setMode('login')}>
                Sign in here
              </button>
            </p>
          )}
        </div>
      </div>
    </div>
  );
}