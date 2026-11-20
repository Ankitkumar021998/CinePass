import React from 'react';

export default function BookingModal({
  movie,
  seatCount,
  setSeatCount,
  showtimes,
  loading,
  onConfirm,
  onClose,
}) {
  if (!movie) return null;

  const matchedSt = showtimes.find((st) => {
    const stMovieImdb = st.movie?.imdb || st.movieImdb || st.movie?.id;
    const stMovieTitle = st.movie?.title;
    const selectedIdentifier = movie.imdb || movie.id;
    return (
      (selectedIdentifier && stMovieImdb === selectedIdentifier) ||
      (movie.title && stMovieTitle?.toLowerCase() === movie.title?.toLowerCase())
    );
  });

  const currentPrice = matchedSt?.price || 220;
  const totalAmount = seatCount * currentPrice;

  return (
    <div className="modal-overlay">
      <div className="modal-card">
        <div className="modal-header">
          <h3>{movie.title}</h3>
          <button className="btn-secondary" onClick={onClose}>✕</button>
        </div>

        <p style={{ textAlign: 'center', fontSize: '0.8rem', color: '#64748b' }}>ALL EYES THIS WAY</p>
        <div className="cinema-screen" />

        <p style={{ textAlign: 'center', color: '#94a3b8', marginBottom: 8 }}>Select Number of Seats</p>
        <div className="seat-selector">
          <button className="counter-btn" onClick={() => setSeatCount(Math.max(1, seatCount - 1))}>-</button>
          <span style={{ fontSize: '1.4rem', fontWeight: 700 }}>{seatCount}</span>
          <button className="counter-btn" onClick={() => setSeatCount(Math.min(10, seatCount + 1))}>+</button>
        </div>

        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            padding: '16px 0',
            borderTop: '1px solid rgba(255,255,255,0.08)',
          }}
        >
          <span>Total Amount:</span>
          <b style={{ color: '#e50914', fontSize: '1.2rem' }}>₹{totalAmount}</b>
        </div>

        <button className="btn-primary" style={{ width: '100%' }} onClick={onConfirm} disabled={loading}>
          {loading ? 'Reserving...' : `Confirm & Pay ₹${totalAmount}`}
        </button>
      </div>
    </div>
  );
}
