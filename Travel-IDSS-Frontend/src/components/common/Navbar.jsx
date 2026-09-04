import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';

const navLinks = [
  { path: '/', label: 'Home' },
  { path: '/route-optimization', label: 'Route Optimization' },
  { path: '/resource-allocation', label: 'Resource Allocation' },
  { path: '/network-analysis', label: 'Network Analysis' },
  { path: '/decision-support', label: 'Decision Support' },
  { path: '/optimization', label: 'Optimization' },
];

export default function Navbar() {
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  // Close mobile menu on route change
  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  return (
    <header className="top-header">
      <Link to="/" className="header-brand">
        <div className="logo-icon">🌴</div>
        <div>
          <div className="brand-title">Travel IDSS</div>
          <div className="brand-sub">Sri Lanka</div>
        </div>
      </Link>

      <ul className={`navbar__links ${menuOpen ? 'open' : ''}`} style={{ display: 'flex', gap: '0.25rem', listStyle: 'none' }}>
        {navLinks.map(({ path, label }) => (
          <li key={path}>
            <Link
              to={path}
              className={`navbar__link ${location.pathname === path ? 'active' : ''}`}
            >
              {label}
            </Link>
          </li>
        ))}
      </ul>

      <div className="user-area">
        <div className="user-greeting-badge" style={{ display: 'none' /* Hide on very small screens if needed, inline styling handled via CSS mostly */ }}>
          <span>🍃</span> Plan Smart, Travel Better
        </div>
        

        <button
          className="navbar__toggle"
          onClick={() => setMenuOpen((prev) => !prev)}
          aria-label="Toggle navigation"
          style={{ background: 'none', border: 'none', marginLeft: '1rem', cursor: 'pointer' }}
        >
          <span style={{ display: 'block', width: '22px', height: '2px', background: '#334155', margin: '4px 0' }} />
          <span style={{ display: 'block', width: '22px', height: '2px', background: '#334155', margin: '4px 0' }} />
          <span style={{ display: 'block', width: '22px', height: '2px', background: '#334155', margin: '4px 0' }} />
        </button>
      </div>
    </header>
  );
}
