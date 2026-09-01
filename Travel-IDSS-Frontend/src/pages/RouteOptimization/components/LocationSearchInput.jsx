// src/pages/RouteOptimization/components/LocationSearchInput.jsx
import { useState, useEffect, useRef } from 'react';
import routeService from '../../../services/routeService';

export default function LocationSearchInput({ label, placeholder, value, onSelect, onClear }) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [loading, setLoading] = useState(false);
  const [activeIdx, setActiveIdx] = useState(-1);
  const containerRef = useRef(null);
  const debounceRef = useRef(null);

  // Keep query in sync with external value (e.g. on clear)
  useEffect(() => {
    if (!value) setQuery('');
  }, [value]);

  // Close dropdown on outside click
  useEffect(() => {
    const handleClick = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setShowDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const handleChange = (e) => {
    const val = e.target.value;
    setQuery(val);
    setActiveIdx(-1);

    if (val.trim().length < 2) {
      setResults([]);
      setShowDropdown(false);
      return;
    }

    // Debounce search
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(async () => {
      setLoading(true);
      try {
        const res = await routeService.searchLocations(val.trim());
        setResults(res.data || []);
        setShowDropdown(true);
      } catch {
        setResults([]);
      } finally {
        setLoading(false);
      }
    }, 300);
  };

  const handleSelect = (loc) => {
    setQuery(loc.name);
    setShowDropdown(false);
    onSelect(loc);
  };

  const handleKeyDown = (e) => {
    if (!showDropdown || results.length === 0) return;
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIdx((prev) => (prev + 1) % results.length);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIdx((prev) => (prev <= 0 ? results.length - 1 : prev - 1));
    } else if (e.key === 'Enter' && activeIdx >= 0) {
      e.preventDefault();
      handleSelect(results[activeIdx]);
    } else if (e.key === 'Escape') {
      setShowDropdown(false);
    }
  };

  return (
    <div className="form-group autocomplete-container" ref={containerRef}>
      <label>{label}</label>
      <div style={{ position: 'relative' }}>
        <input
          type="text"
          className="form-control"
          placeholder={placeholder}
          value={query}
          onChange={handleChange}
          onFocus={() => results.length > 0 && setShowDropdown(true)}
          onKeyDown={handleKeyDown}
        />
        {value && (
          <button
            onClick={() => {
              setQuery('');
              setResults([]);
              onClear();
            }}
            style={{
              position: 'absolute',
              right: '10px',
              top: '50%',
              transform: 'translateY(-50%)',
              background: 'none',
              border: 'none',
              color: '#94a3b8',
              cursor: 'pointer',
              fontSize: '1.1rem',
              lineHeight: 1,
            }}
            title="Clear"
          >
            ✕
          </button>
        )}
      </div>

      {loading && (
        <div style={{ padding: '0.5rem 0', fontSize: '0.8rem', color: '#94a3b8' }}>
          Searching...
        </div>
      )}

      {showDropdown && results.length > 0 && (
        <div className="autocomplete-dropdown">
          {results.map((loc, idx) => (
            <div
              key={loc.id}
              className={`autocomplete-item ${idx === activeIdx ? 'active' : ''}`}
              onClick={() => handleSelect(loc)}
              onMouseEnter={() => setActiveIdx(idx)}
            >
              <span className="item-name">{loc.name}</span>
              <span className="item-desc">
                {[loc.type, loc.district, loc.province].filter(Boolean).join(' · ')}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
