import React, { useState, useEffect } from 'react';
import { Film, LogIn, Ticket, LogOut, Printer, X } from 'lucide-react';

const API_BASE_URL = 'http://localhost:8080';
const FALLBACK_POSTER = 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=500&q=80';

export default function App() {
  const [token, setToken] = useState(localStorage.getItem('cinepass_token') || '');
  const [username, setUsername] = useState(localStorage.getItem('cinepass_user') || '');
  const [movies, setMovies] = useState([]);
  const [showtimes, setShowtimes] = useState([]);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [isSignUp, setIsSignUp] = useState(false);
  const [authData, setAuthData] = useState({ username: '', password: '', name: '', email: '' });
  
  const [selectedMovie, setSelectedMovie] = useState(null);
  const [seatCount, setSeatCount] = useState(1);
  const [loading, setLoading] = useState(false);

  // User-specific booking state
  const [confirmedBooking, setConfirmedBooking] = useState(null);
  const [showMyBookings, setShowMyBookings] = useState(false);

  // Current logged-in user ki booking read karna
  useEffect(() => {
    if (username) {
      const saved = localStorage.getItem(`cinepass_booking_${username}`);
      setConfirmedBooking(saved ? JSON.parse(saved) : null);
    } else {
      setConfirmedBooking(null);
    }
  }, [username]);

  const defaultMovies = [
    { imdb: 'tt0111161', title: 'The Shawshank Redemption', poster: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=500&q=80', genre: 'Drama • 1994' },
    { imdb: 'tt0068646', title: 'The Godfather', poster: FALLBACK_POSTER, genre: 'Crime • 1972' },
    { imdb: 'tt0468569', title: 'The Dark Knight', poster: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=500&q=80', genre: 'Action • 2008' },
    { imdb: 'tt1375666', title: 'Inception', poster: 'https://images.unsplash.com/photo-1518676590629-3dcbd9c5a5c9?auto=format&fit=crop&w=500&q=80', genre: 'Sci-Fi • 2010' }
  ];

  useEffect(() => {
    fetch(`${API_BASE_URL}/public/movies`)
      .then(res => res.ok ? res.json() : [])
      .then(data => setMovies(data.length > 0 ? data : defaultMovies))
      .catch(() => {
        fetch(`${API_BASE_URL}/api/movies`, {
          headers: token ? { 'Authorization': `Bearer ${token}` } : {}
        })
          .then(res => res.ok ? res.json() : [])
          .then(data => setMovies(data.length > 0 ? data : defaultMovies))
          .catch(() => setMovies(defaultMovies));
      });

    fetch(`${API_BASE_URL}/public/showtimes`)
      .then(res => res.ok ? res.json() : [])
      .then(data => setShowtimes(data))
      .catch(() => {
        fetch(`${API_BASE_URL}/api/showtimes`, {
          headers: token ? { 'Authorization': `Bearer ${token}` } : {}
        })
          .then(res => res.ok ? res.json() : [])
          .then(data => setShowtimes(data))
          .catch(() => setShowtimes([]));
      });
  }, [token]);

  const handleAuth = async (e) => {
    e.preventDefault();
    setLoading(true);
    const endpoint = isSignUp ? '/auth/signup' : '/auth/authenticate';

    try {
      const res = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(authData)
      });

      const data = await res.json();
      if (res.ok && data.accessToken) {
        setToken(data.accessToken);
        setUsername(authData.username);
        localStorage.setItem('cinepass_token', data.accessToken);
        localStorage.setItem('cinepass_user', authData.username);
        
        const userSavedBooking = localStorage.getItem(`cinepass_booking_${authData.username}`);
        setConfirmedBooking(userSavedBooking ? JSON.parse(userSavedBooking) : null);

        setShowAuthModal(false);
      } else {
        alert(data.message || 'Authentication failed.');
      }
    } catch (err) {
      alert('Unable to connect to the backend server.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    setToken('');
    setUsername('');
    setConfirmedBooking(null);
    setShowMyBookings(false);
    setAuthData({ username: '', password: '', name: '', email: '' }); 
    localStorage.removeItem('cinepass_token');
    localStorage.removeItem('cinepass_user');
  };

  const handleBooking = async () => {
    if (!token) {
      setShowAuthModal(true);
      return;
    }

    setLoading(true);
    const selectedIdentifier = selectedMovie.imdb || selectedMovie.id;
    const matchedShowTime = showtimes.find((st) => {
      const stMovieImdb = st.movie?.imdb || st.movieImdb || st.movie?.id;
      const stMovieTitle = st.movie?.title;

      return (
        (selectedIdentifier && stMovieImdb === selectedIdentifier) ||
        (selectedMovie.title && stMovieTitle?.toLowerCase() === selectedMovie.title?.toLowerCase())
      );
    });

    const validShowTimeId = matchedShowTime
      ? (matchedShowTime.id || matchedShowTime.showTimeId)
      : (showtimes.length > 0 ? (showtimes[0].id || showtimes[0].showTimeId) : 1);

    const pricePerSeat = (matchedShowTime && matchedShowTime.price) ? Number(matchedShowTime.price) : 300;

    try {
      const response = await fetch(`${API_BASE_URL}/api/tickets`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          showTimeId: Number(validShowTimeId),
          numberOfSeats: Number(seatCount)
        })
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Booking failed');
      }

      const result = await response.json();

      const currentUserName = username || 'User';
      const moviePoster = selectedMovie.poster || FALLBACK_POSTER;

      const newBooking = {
        id: result?.id,
        bookingId: result?.bookingReference || ('CP-' + Math.floor(100000 + Math.random() * 900000)),
        movieTitle: selectedMovie.title,
        poster: moviePoster,
        seats: result?.numberOfSeats || seatCount,
        total: result?.totalPrice || (seatCount * pricePerSeat),
        bookingTime: new Date().toLocaleString(),
        cinemaHall: 'Screen 02 • Audi Luxe',
        user: currentUserName
      };

      setConfirmedBooking(newBooking);
      localStorage.setItem(`cinepass_booking_${currentUserName}`, JSON.stringify(newBooking));
      setSelectedMovie(null);
      setShowMyBookings(true);
    } catch (err) {
      alert('Error: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelSeats = async () => {
    if (!confirmedBooking) return;

    const currentSeats = Number(confirmedBooking.seats);

    if (currentSeats <= 1) {
      const confirmFull = window.confirm("You have only 1 seat booked. Do you want to cancel the entire booking?");
      if (confirmFull) {
        await cancelEntireTicket();
      }
      return;
    }

    const input = window.prompt(
      `You currently have \({currentSeats} seats.\nHow many seats do you want to cancel? (1 to\){currentSeats - 1}):`,
"1"
);

    if (input === null) return;
    const countToCancel = parseInt(input, 10);

    if (isNaN(countToCancel) || countToCancel <= 0 || countToCancel >= currentSeats) {
      alert(`Please enter a number between 1 and ${currentSeats - 1}`);
      return;
    }

    setLoading(true);

    try {
      const ticketId = confirmedBooking.id;
      const response = await fetch(`${API_BASE_URL}/api/tickets/${ticketId}/cancel-seats?seatsToCancel=${countToCancel}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        const errorText = await response.text();
        alert(`Failed: ${errorText || 'Server error'}`);
        setLoading(false);
        return;
      }

      const updatedTicket = await response.json();

      const remainingSeats = updatedTicket.numberOfSeats || (currentSeats - countToCancel);

      // Single seat rate calculate karein purane total price se
      const ratePerSeat = confirmedBooking.total ? (Number(confirmedBooking.total) / currentSeats) : 300;
      const newTotalPrice = (updatedTicket.totalPrice !== undefined && updatedTicket.totalPrice !== null)
        ? updatedTicket.totalPrice
        : (remainingSeats * ratePerSeat);

      const updatedBooking = {
        ...confirmedBooking,
        seats: remainingSeats,
        total: newTotalPrice
      };

      setConfirmedBooking(updatedBooking);
      localStorage.setItem(`cinepass_booking_${username}`, JSON.stringify(updatedBooking));

      alert(`Successfully cancelled ${countToCancel} seat(s). You still have${remainingSeats} seat(s) confirmed!`);
    } catch (err) {
      alert('Cancellation error: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const cancelEntireTicket = async () => {
    try {
      setLoading(true);
      await fetch(`${API_BASE_URL}/api/tickets/${confirmedBooking.id}/cancel`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      localStorage.removeItem(`cinepass_booking_${username}`);
      setConfirmedBooking(null);
      setShowMyBookings(false);
      alert('Entire booking cancelled.');
    } catch (err) {
      alert('Error: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const openMyBookings = () => {
    if (!confirmedBooking) {
      alert("You haven't any active booked ticket. Please book a movie first!");
      return;
    }
    setShowMyBookings(true);
  };

  return (
    <div>
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
              <button className="btn-secondary" onClick={openMyBookings}>
                <Ticket size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />
                My Bookings
              </button>
              <button className="btn-secondary" onClick={handleLogout}>
                <LogOut size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />
                Logout
              </button>
            </>
          ) : (
            <button className="btn-primary" onClick={() => setShowAuthModal(true)}>
              <LogIn size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />
              Sign In
            </button>
          )}
        </div>
      </nav>

      <main className="movie-grid" style={{ paddingTop: '40px' }}>
        {movies.map((movie, idx) => (
          <div key={idx} className="movie-card">
            <img 
              src={movie.poster || defaultMovies[idx % defaultMovies.length].poster} 
              alt={movie.title} 
              className="movie-poster"
              onError={(e) => {
                e.target.onerror = null;
                e.target.src = FALLBACK_POSTER;
              }}
            />
            <div className="movie-info">
              <h3 className="movie-title">{movie.title}</h3>
              <p className="movie-meta">{movie.genre || '2D / Dolby Atmos • Cinema'}</p>
              <button 
                className="btn-primary" 
                style={{ width: '100%', marginTop: 'auto' }}
                onClick={() => {
                  setSelectedMovie(movie);
                  setSeatCount(1);
                }}
              >
                <Ticket size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />
                Book Seats
              </button>
            </div>
          </div>
        ))}
      </main>

      {selectedMovie && (
        <div className="modal-overlay">
          <div className="modal-card">
            <div className="modal-header">
              <h3>{selectedMovie.title}</h3>
              <button className="btn-secondary" onClick={() => setSelectedMovie(null)}>✕</button>
            </div>

            <p style={{ textAlign: 'center', fontSize: '0.8rem', color: '#64748b' }}>ALL EYES THIS WAY</p>
            <div className="cinema-screen" />

            <p style={{ textAlign: 'center', color: '#94a3b8', marginBottom: 8 }}>Select Number of Seats</p>
            <div className="seat-selector">
              <button className="counter-btn" onClick={() => setSeatCount(Math.max(1, seatCount - 1))}>-</button>
              <span style={{ fontSize: '1.4rem', fontWeight: 700 }}>{seatCount}</span>
              <button className="counter-btn" onClick={() => setSeatCount(Math.min(10, seatCount + 1))}>+</button>
            </div>

            {(() => {
              const matchedSt = showtimes.find((st) => {
                const stMovieImdb = st.movie?.imdb || st.movieImdb || st.movie?.id;
                const stMovieTitle = st.movie?.title;
                const selectedIdentifier = selectedMovie.imdb || selectedMovie.id;
                return (
                  (selectedIdentifier && stMovieImdb === selectedIdentifier) ||
                  (selectedMovie.title && stMovieTitle?.toLowerCase() === selectedMovie.title?.toLowerCase())
                );
              });
              const currentPrice = matchedSt?.price || 220;

              return (
                <>
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px 0', borderTop: '1px solid rgba(255,255,255,0.08)' }}>
                    <span>Total Amount:</span>
                    <b style={{ color: '#e50914', fontSize: '1.2rem' }}>₹{seatCount * currentPrice}</b>
                  </div>

                  <button className="btn-primary" style={{ width: '100%' }} onClick={handleBooking} disabled={loading}>
                    {loading ? 'Reserving...' : `Confirm & Pay ₹${seatCount * currentPrice}`}
                  </button>
                </>
              );
            })()}
          </div>
        </div>
      )}

      {showMyBookings && confirmedBooking && (
        <div className="modal-overlay">
          <div 
            className="boarding-pass-card"
            style={{
              backgroundImage: `linear-gradient(rgba(15, 23, 42, 0.82), rgba(15, 23, 42, 0.88)), url(${confirmedBooking.poster || FALLBACK_POSTER})`,
              backgroundSize: 'cover',
              backgroundPosition: 'center',
              backgroundRepeat: 'no-repeat'
            }}
          >
            <div className="boarding-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Film size={22} color="#e50914" />
                <span style={{ fontWeight: 800, letterSpacing: '1px' }}>CINEPASS DIGITAL BOARDING PASS</span>
              </div>
              <button className="close-btn-ghost" onClick={() => setShowMyBookings(false)}>
                <X size={18} />
              </button>
            </div>

            <div className="boarding-body" style={{ background: 'transparent' }}>
              <div className="boarding-left">
                <span className="badge-confirmed">CONFIRMED RESERVATION</span>
                <h1 className="pass-movie-title">{confirmedBooking.movieTitle}</h1>
                <p className="pass-meta">{confirmedBooking.cinemaHall}</p>

                <div className="pass-grid">
                  <div>
                    <span className="pass-label">PASSENGER / GUEST</span>
                    <span className="pass-val">{confirmedBooking.user}</span>
                  </div>
                  <div>
                    <span className="pass-label">RESERVATION REF</span>
                    <span className="pass-val" style={{ color: '#e50914' }}>{confirmedBooking.bookingId}</span>
                  </div>
                  <div>
                    <span className="pass-label">SEATS</span>
                    <span className="pass-val">{confirmedBooking.seats} Seat(s)</span>
                  </div>
                  <div>
                    <span className="pass-label">TOTAL PAID</span>
                    <span className="pass-val">₹{confirmedBooking.total}</span>
                  </div>
                </div>

                <div className="pass-footer-note">
                  Present this digital QR boarding pass at the cinema entrance kiosk for instant validation.
                </div>
              </div>

              <div className="boarding-right">
                <div className="qr-wrapper">
                  <img 
                    src={`https://api.qrserver.com/v1/create-qr-code/?size=140x140&data=CINEPASS:${confirmedBooking.bookingId}:${confirmedBooking.user}`}
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
                onClick={handleCancelSeats}
                disabled={loading}
                style={{
                  background: '#dc2626',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '8px',
                  padding: '10px 18px',
                  fontWeight: 600,
                  cursor: 'pointer',
                  marginRight: '8px'
                }}
              >
                {loading ? 'Updating...' : 'Cancel Seats'}
              </button>
              <button className="btn-primary" onClick={() => setShowMyBookings(false)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {showAuthModal && (
        <div className="modal-overlay">
          <div className="modal-card">
            <div className="modal-header">
              <h3>{isSignUp ? 'Create Account' : 'Sign In'}</h3>
              <button className="btn-secondary" onClick={() => setShowAuthModal(false)}>✕</button>
            </div>

            <form onSubmit={handleAuth}>
              {isSignUp && (
                <>
                  <div className="form-group">
                    <label>Full Name</label>
                    <input type="text" required value={authData.name} onChange={e => setAuthData({...authData, name: e.target.value})} />
                  </div>
                  <div className="form-group">
                    <label>Email Address</label>
                    <input type="email" required value={authData.email} onChange={e => setAuthData({...authData, email: e.target.value})} />
                  </div>
                </>
              )}

              <div className="form-group">
                <label>Username</label>
                <input type="text" required value={authData.username} onChange={e => setAuthData({...authData, username: e.target.value})} />
              </div>

              <div className="form-group">
                <label>Password</label>
                <input type="password" required value={authData.password} onChange={e => setAuthData({...authData, password: e.target.value})} />
              </div>

              <button className="btn-primary" style={{ width: '100%', marginTop: 8 }} disabled={loading}>
                {loading ? 'Processing...' : (isSignUp ? 'Sign Up' : 'Login')}
              </button>
            </form>

            <p style={{ textAlign: 'center', marginTop: 16, fontSize: '0.85rem', color: '#94a3b8' }}>
              {isSignUp ? 'Already have an account?' : "Don't have an account?"}{' '}
              <span style={{ color: '#e50914', cursor: 'pointer', fontWeight: 600 }} onClick={() => setIsSignUp(!isSignUp)}>
                {isSignUp ? 'Sign In' : 'Sign Up'}
              </span>
            </p>
          </div>
        </div>
      )}
    </div>
  );
}