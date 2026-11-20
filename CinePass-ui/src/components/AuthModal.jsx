import React from 'react';

export default function AuthModal({
  isOpen,
  isSignUp,
  setIsSignUp,
  authData,
  setAuthData,
  loading,
  onSubmit,
  onClose,
}) {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-card">
        <div className="modal-header">
          <h3>{isSignUp ? 'Create Account' : 'Sign In'}</h3>
          <button className="btn-secondary" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={onSubmit}>
          {isSignUp && (
            <>
              <div className="form-group">
                <label>Full Name</label>
                <input
                  type="text"
                  required
                  value={authData.name}
                  onChange={(e) => setAuthData({ ...authData, name: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Email Address</label>
                <input
                  type="email"
                  required
                  value={authData.email}
                  onChange={(e) => setAuthData({ ...authData, email: e.target.value })}
                />
              </div>
            </>
          )}

          <div className="form-group">
            <label>Username</label>
            <input
              type="text"
              required
              value={authData.username}
              onChange={(e) => setAuthData({ ...authData, username: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              required
              value={authData.password}
              onChange={(e) => setAuthData({ ...authData, password: e.target.value })}
            />
          </div>

          <button className="btn-primary" style={{ width: '100%', marginTop: 8 }} disabled={loading}>
            {loading ? 'Processing...' : isSignUp ? 'Sign Up' : 'Login'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 16, fontSize: '0.85rem', color: '#94a3b8' }}>
          {isSignUp ? 'Already have an account?' : "Don't have an account?"}{' '}
          <span
            style={{ color: '#e50914', cursor: 'pointer', fontWeight: 600 }}
            onClick={() => setIsSignUp(!isSignUp)}
          >
            {isSignUp ? 'Sign In' : 'Sign Up'}
          </span>
        </p>
      </div>
    </div>
  );
}
