import React from 'react';
import { Film, LogIn, Ticket, LogOut } from 'lucide-react';

export default function Navbar({ token, username, onOpenBookings, onLogout, onOpenAuth }) {
  return (
    <nav className="navbar">
      <div className="logo-badge">
        <Film size={26} color="#e50914" />
        CinePass
      </div>

      <div className="nav-actions">
        {token ? (
          <>
            <span style={{ fontSize: '0.9rem', color: '#94a3b8' }}>
              Welcome, <b style={{ color: '#fff' }}>{username}</b>
            </span>
            <button className="btn-secondary" onClick={onOpenBookings}>
              <Ticket size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />
              My Bookings
            </button>
            <button className="btn-secondary" onClick={onLogout}>
              <LogOut size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />
              Logout
            </button>
          </>
        ) : (
          <button className="btn-primary" onClick={onOpenAuth}>
            <LogIn size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />
            Sign In
          </button>
        )}
      </div>
    </nav>
  );
}
