import React from 'react';
import { Ticket } from 'lucide-react';
import { FALLBACK_POSTER } from '../constants/config';

export default function MovieCard({ movie, fallbackImage, onBookClick }) {
  return (
    <div className="movie-card">
      <img
        src={movie.poster || fallbackImage}
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
          onClick={() => onBookClick(movie)}
        >
          <Ticket size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />
          Book Seats
        </button>
      </div>
    </div>
  );
}
