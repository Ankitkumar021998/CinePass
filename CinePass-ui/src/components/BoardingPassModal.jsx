import React from 'react';
import { Film, Printer, X } from 'lucide-react';
import { FALLBACK_POSTER } from '../constants/config';

export default function BoardingPassModal({ booking, loading, onCancelSeats, onClose }) {
  if (!booking) return null;

  return (
    <div className="modal-overlay">
      <div
        className="boarding-pass-card"
        style={{
          backgroundImage: `linear-gradient(rgba(15, 23, 42, 0.82), rgba(15, 23, 42, 0.88)), url(${booking.poster || FALLBACK_POSTER})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          backgroundRepeat: 'no-repeat',
        }}
      >
        <div className="boarding-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Film size={22} color="#e50914" />
            <span style={{ fontWeight: 800, letterSpacing: '1px' }}>CINEPASS DIGITAL BOARDING PASS</span>
          </div>
          <button className="close-btn-ghost" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <div className="boarding-body" style={{ background: 'transparent' }}>
          <div className="boarding-left">
            <span className="badge-confirmed">CONFIRMED RESERVATION</span>
            <h1 className="pass-movie-title">{booking.movieTitle}</h1>
            <p className="pass-meta">{booking.cinemaHall}</p>

            <div className="pass-grid">
              <div>
                <span className="pass-label">PASSENGER / GUEST</span>
                <span className="pass-val">{booking.user}</span>
              </div>
              <div>
                <span className="pass-label">RESERVATION REF</span>
                <span className="pass-val" style={{ color: '#e50914' }}>{booking.bookingId}</span>
              </div>
              <div>
                <span className="pass-label">SEATS</span>
                <span className="pass-val">{booking.seats} Seat(s)</span>
              </div>
              <div>
                <span className="pass-label">TOTAL PAID</span>
                <span className="pass-val">₹{booking.total}</span>
              </div>
            </div>

            <div className="pass-footer-note">
              Present this digital QR boarding pass at the cinema entrance kiosk for instant validation.
            </div>
          </div>

          <div className="boarding-right">
            <div className="qr-wrapper">
              <img
                src={`https://api.qrserver.com/v1/create-qr-code/?size=140x140&data=CINEPASS:${booking.bookingId}:${booking.user}`}
                alt="Ticket QR"
                style={{ width: '130px', height: '130px', borderRadius: '4px' }}
              />
              <span className="qr-caption">SCAN AT TURNSTILE</span>
            </div>
          </div>
        </div>

        <div className="boarding-actions">
          <button className="btn-secondary" onClick={() => window.print()}>
            <Printer size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />
            Print / Save Pass
          </button>
          <button
            onClick={onCancelSeats}
            disabled={loading}
            style={{
              background: '#dc2626',
              color: '#ffffff',
              border: 'none',
              borderRadius: '8px',
              padding: '10px 18px',
              fontWeight: 600,
              cursor: 'pointer',
              marginRight: '8px',
            }}
          >
            {loading ? 'Updating...' : 'Cancel Seats'}
          </button>
          <button className="btn-primary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
