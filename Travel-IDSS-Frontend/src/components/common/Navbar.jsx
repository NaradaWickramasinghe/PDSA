// src/components/common/Navbar.jsx
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
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Close mobile menu on route change
  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  return (
    <nav className={`navbar ${scrolled ? 'scrolled' : ''}`}>
      <Link to="/" className="navbar__brand">
        <div className="navbar__logo">T</div>
        <div className="navbar__title">
          <span>Travel IDSS</span>
        </div>
      </Link>

      <ul className={`navbar__links ${menuOpen ? 'open' : ''}`}>
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

      <button
        className="navbar__toggle"
        onClick={() => setMenuOpen((prev) => !prev)}
        aria-label="Toggle navigation"
      >
        <span />
        <span />
        <span />
      </button>
    </nav>
  );
}
