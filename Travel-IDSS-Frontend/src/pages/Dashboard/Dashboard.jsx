// src/pages/Dashboard/Dashboard.jsx
import { Link } from 'react-router-dom';
import { MapContainer, TileLayer, CircleMarker, Polyline } from 'react-leaflet';

const modules = [
  {
    title: 'Route Optimization',
    path: '/route-optimization',
    icon: '🗺️',
    description:
      'Find the most efficient travel routes using Dijkstra, A*, Bellman-Ford, and Floyd-Warshall algorithms. Compare execution time and path quality.',
    tags: ['Dijkstra', 'A*', 'Shortest Path'],
  },
  {
    title: 'Resource Allocation',
    path: '/resource-allocation',
    icon: '📦',
    description:
      'Optimally distribute budgets, vehicles, and staff across destinations using dynamic programming and greedy strategies.',
    tags: ['Knapsack', 'Greedy', 'DP'],
  },
  {
    title: 'Network Analysis',
    path: '/network-analysis',
    icon: '🔗',
    description:
      'Analyze transport networks with graph algorithms. Discover hubs via centrality measures and build minimum spanning trees.',
    tags: ['BFS', 'DFS', 'MST', 'Centrality'],
  },
  {
    title: 'Decision Support',
    path: '/decision-support',
    icon: '🧠',
    description:
      'Get intelligent travel recommendations powered by multi-criteria decision analysis and weighted scoring models.',
    tags: ['MCDM', 'Scoring', 'Ranking'],
  },
  {
    title: 'Optimization Engine',
    path: '/optimization',
    icon: '⚡',
    description:
      'Benchmark and compare optimization algorithms including genetic algorithms, simulated annealing, and branch-and-bound.',
    tags: ['GA', 'SA', 'Branch & Bound'],
  },
];

const stats = [
  { number: '25', label: 'Districts / Nodes' },
  { number: '342', label: 'Active Routes' },
  { number: '5', label: 'Core Modules' },
  { number: '8+', label: 'Algorithms' },
];

// Mock Data for MST Visualization in Hero
const mstNodes = [
  { id: 'CMB', lat: 6.9271, lng: 79.8612, name: 'Colombo' },
  { id: 'KND', lat: 7.2906, lng: 80.6337, name: 'Kandy' },
  { id: 'GAL', lat: 6.0535, lng: 80.2210, name: 'Galle' },
  { id: 'JAF', lat: 9.6615, lng: 80.0255, name: 'Jaffna' },
  { id: 'TRN', lat: 8.5874, lng: 81.2152, name: 'Trincomalee' },
];

const mstEdges = [
  { from: 'CMB', to: 'KND' },
  { from: 'CMB', to: 'GAL' },
  { from: 'KND', to: 'TRN' },
  { from: 'KND', to: 'JAF' },
];

export default function Dashboard() {
  return (
    <div className="page">
      <div className="page__container">
        {/* ── Hero ── */}
        <section className="hero">
          <div className="hero__content">
            <div className="hero__badge">
              Travel IDSS — System Overview
            </div>

            <h1 className="hero__title">
              Smarter Decisions,<br />
              <span className="hero__title-gradient">Powered by Algorithms.</span>
            </h1>

            <p className="hero__subtitle">
              Harness the power of advanced data structures and algorithms to
              optimize routes, allocate resources, analyze networks, and make
              data-driven travel decisions — all in one platform.
            </p>

            <div className="hero__actions">
              <Link to="/route-optimization" className="btn btn--primary">
                Get Started
                <span>→</span>
              </Link>
              <a href="#modules" className="btn btn--ghost">
                Explore Modules
              </a>
            </div>
          </div>
          
          <div className="hero__visual" style={{ 
            height: '100%', 
            minHeight: '400px', 
            background: 'var(--color-bg-secondary)', 
            border: '1px solid var(--color-border)', 
            position: 'relative',
            overflow: 'hidden',
            boxShadow: 'var(--shadow-md)'
          }}>
            <MapContainer
              center={[7.8731, 80.7718]}
              zoom={6.5}
              scrollWheelZoom={false}
              zoomControl={false}
              dragging={false}
              style={{ height: '100%', width: '100%', zIndex: 0 }}
            >
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; OpenStreetMap contributors'
                className="dark-map-tiles"
              />
              
              {/* Draw Edges for MST */}
              {mstEdges.map((e, idx) => {
                const fromNode = mstNodes.find((n) => n.id === e.from);
                const toNode = mstNodes.find((n) => n.id === e.to);
                return (
                  <Polyline
                    key={`mst-edge-${idx}`}
                    positions={[
                      [fromNode.lat, fromNode.lng],
                      [toNode.lat, toNode.lng],
                    ]}
                    pathOptions={{ color: 'var(--color-accent-primary)', weight: 3, opacity: 0.8, dashArray: '5, 5' }}
                  />
                );
              })}

              {/* Draw Nodes */}
              {mstNodes.map((n) => (
                <CircleMarker
                  key={n.id}
                  center={[n.lat, n.lng]}
                  radius={6}
                  pathOptions={{
                    color: 'var(--color-bg-secondary)',
                    fillColor: 'var(--color-accent-secondary)',
                    fillOpacity: 1,
                    weight: 2,
                  }}
                />
              ))}
            </MapContainer>
            
            {/* Overlay Title */}
            <div style={{
              position: 'absolute',
              bottom: '20px',
              left: '20px',
              zIndex: 1000,
              background: 'rgba(255, 255, 255, 0.9)',
              padding: '10px 15px',
              border: '1px solid var(--color-border)',
              borderRadius: '4px',
              pointerEvents: 'none'
            }}>
              <div style={{ fontFamily: 'var(--font-display)', fontStyle: 'italic', fontSize: '1.2rem', color: 'var(--color-text-primary)' }}>
                Prim's Algorithm (MST)
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                Optimal Network Backbone
              </div>
            </div>
          </div>
        </section>

        {/* ── Stats ── */}
        <section className="stats">
          {stats.map(({ number, label }) => (
            <div className="stats__item" key={label}>
              <div className="stats__number">{number}</div>
              <div className="stats__label">{label}</div>
            </div>
          ))}
        </section>

        {/* ── Module Cards ── */}
        <section className="modules" id="modules">
          <div className="modules__header">
            <h2 className="modules__title">System Modules</h2>
            <p className="modules__subtitle">
              Each module targets a specific travel planning challenge using tailored algorithmic approaches.
            </p>
          </div>

          <div className="modules__grid">
            {modules.map(({ title, path, icon, description, tags }) => (
              <Link
                to={path}
                className="module-card"
                key={path}
              >
                <div className="module-card__icon">{icon}</div>
                <h3 className="module-card__title">{title}</h3>
                <p className="module-card__description">{description}</p>

                <div className="module-card__tags">
                  {tags.map((tag) => (
                    <span className="module-card__tag" key={tag}>
                      {tag}
                    </span>
                  ))}
                </div>

                <span className="module-card__cta">
                  Open Module{' '}
                  <span className="module-card__cta-arrow">→</span>
                </span>
              </Link>
            ))}
          </div>
        </section>

        {/* ── Footer ── */}
        <footer className="footer" style={{ padding: '2rem 0', borderTop: '1px solid var(--color-border)', margin: '4rem 0 2rem 0' }}>
          <p className="footer__text" style={{ color: 'var(--color-text-secondary)', fontSize: '0.85rem' }}>
            © {new Date().getFullYear()} Travel IDSS — Intelligent Decision Support System
          </p>
        </footer>
      </div>
    </div>
  );
}
