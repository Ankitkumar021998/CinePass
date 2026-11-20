import React, { useState, useEffect } from 'react';
import { API_BASE_URL, FALLBACK_POSTER, DEFAULT_MOVIES } from './constants/config';
import Navbar from './components/Navbar';
import MovieCard from './components/MovieCard';
import BookingModal from './components/BookingModal';
import BoardingPassModal from './components/BoardingPassModal';
import AuthModal from './components/AuthModal';

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

  const [confirmedBooking, setConfirmedBooking] = useState(null);
  const [showMyBookings, setShowMyBookings] = useState(false);

  useEffect(() => {
    if (username) {
      const saved = localStorage.getItem(`cinepass_booking_${username}`);
      setConfirmedBooking(saved ? JSON.parse(saved) : null);
    } else {
      setConfirmedBooking(null);
    }
  }, [username]);

  useEffect(() => {
    fetch(`${API_BASE_URL}/public/movies`)
      .then((res) => (res.ok ? res.json() : []))
      .then((data) => setMovies(data.length > 0 ? data : DEFAULT_MOVIES))
      .catch(() => {
        fetch(`${API_BASE_URL}/api/movies`, {
          headers: token ? { Authorization: `Bearer ${token}` } : {},
        })
          .then((res) => (res.ok ? res.json() : []))
          .then((data) => setMovies(data.length > 0 ? data : DEFAULT_MOVIES))
          .catch(() => setMovies(DEFAULT_MOVIES));
      });

    fetch(`${API_BASE_URL}/public/showtimes`)
      .then((res) => (res.ok ? res.json() : []))
      .then((data) => setShowtimes(data))
      .catch(() => {
        fetch(`${API_BASE_URL}/api/showtimes`, {
          headers: token ? { Authorization: `Bearer ${token}` } : {},
        })
          .then((res) => (res.ok ? res.json() : []))
          .then((data) => setShowtimes(data))
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
        body: JSON.stringify(authData),
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
    } catch {
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
      ? matchedShowTime.id || matchedShowTime.showTimeId
      : showtimes.length > 0
      ? showtimes[0].id || showtimes[0].showTimeId
      : 1;

    const pricePerSeat = matchedShowTime?.price ? Number(matchedShowTime.price) : 300;

    try {
      const response = await fetch(`${API_BASE_URL}/api/tickets`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          showTimeId: Number(validShowTimeId),
          numberOfSeats: Number(seatCount),
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Booking failed');
      }

      const result = await response.json();
      const currentUserName = username || 'User';

      const newBooking = {
        id: result?.id,
        bookingId: result?.bookingReference || 'CP-' + Math.floor(100000 + Math.random() * 900000),
        movieTitle: selectedMovie.title,
        poster: selectedMovie.poster || FALLBACK_POSTER,
        seats: result?.numberOfSeats || seatCount,
        total: result?.totalPrice || seatCount * pricePerSeat,
        bookingTime: new Date().toLocaleString(),
        cinemaHall: 'Screen 02 • Audi Luxe',
        user: currentUserName,
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

  const cancelEntireTicket = async () => {
    try {
      setLoading(true);
      await fetch(`${API_BASE_URL}/api/tickets/${confirmedBooking.id}/cancel`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
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

  const handleCancelSeats = async () => {
    if (!confirmedBooking) return;

    const currentSeats = Number(confirmedBooking.seats);

    if (currentSeats <= 1) {
      if (window.confirm('You have only 1 seat booked. Do you want to cancel the entire booking?')) {
        await cancelEntireTicket();
      }
      return;
    }

    const input = window.prompt(
      `You currently have ${currentSeats} seats.\nHow many seats do you want to cancel? (1 to ${currentSeats - 1}):`,
      '1'
    );

    if (input === null) return;
    const countToCancel = parseInt(input, 10);

    if (isNaN(countToCancel) || countToCancel <= 0 || countToCancel >= currentSeats) {
      alert(`Please enter a number between 1 and ${currentSeats - 1}`);
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/tickets/${confirmedBooking.id}/cancel-seats?seatsToCancel=${countToCancel}`,
        {
          method: 'POST',
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        alert(`Failed: ${errorText || 'Server error'}`);
        return;
      }

      const updatedTicket = await response.json();
      const remainingSeats = updatedTicket.numberOfSeats || currentSeats - countToCancel;
      const ratePerSeat = confirmedBooking.total ? Number(confirmedBooking.total) / currentSeats : 300;
      const newTotalPrice =
        updatedTicket.totalPrice !== undefined && updatedTicket.totalPrice !== null
          ? updatedTicket.totalPrice
          : remainingSeats * ratePerSeat;

      const updatedBooking = {
        ...confirmedBooking,
        seats: remainingSeats,
        total: newTotalPrice,
      };

      setConfirmedBooking(updatedBooking);
      localStorage.setItem(`cinepass_booking_${username}`, JSON.stringify(updatedBooking));

      alert(`Successfully cancelled ${countToCancel} seat(s). You still have ${remainingSeats} seat(s) confirmed!`);
    } catch (err) {
      alert('Cancellation error: ' + err.message);
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
      <Navbar
        token={token}
        username={username}
        onOpenBookings={openMyBookings}
        onLogout={handleLogout}
        onOpenAuth={() => setShowAuthModal(true)}
      />

      <main className="movie-grid" style={{ paddingTop: '40px' }}>
        {movies.map((movie, idx) => (
          <MovieCard
            key={movie.imdb || movie.id || idx}
            movie={movie}
            fallbackImage={DEFAULT_MOVIES[idx % DEFAULT_MOVIES.length].poster}
            onBookClick={(selected) => {
              setSelectedMovie(selected);
              setSeatCount(1);
            }}
          />
        ))}
      </main>

      <BookingModal
        movie={selectedMovie}
        seatCount={seatCount}
        setSeatCount={setSeatCount}
        showtimes={showtimes}
        loading={loading}
        onConfirm={handleBooking}
        onClose={() => setSelectedMovie(null)}
      />

      {showMyBookings && (
        <BoardingPassModal
          booking={confirmedBooking}
          loading={loading}
          onCancelSeats={handleCancelSeats}
          onClose={() => setShowMyBookings(false)}
        />
      )}

      <AuthModal
        isOpen={showAuthModal}
        isSignUp={isSignUp}
        setIsSignUp={setIsSignUp}
        authData={authData}
        setAuthData={setAuthData}
        loading={loading}
        onSubmit={handleAuth}
        onClose={() => setShowAuthModal(false)}
      />
    </div>
  );
}
