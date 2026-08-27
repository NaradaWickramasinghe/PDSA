// src/pages/Dashboard/Dashboard.jsx
import { Link } from 'react-router-dom';

const modules = [
  {
    title: 'Route Optimization',
    path: '/route-optimization',
    theme: 'indigo',
    icon: '🗺️',
    description:
      'Find the most efficient travel routes using Dijkstra, A*, Bellman-Ford, and Floyd-Warshall algorithms. Compare execution time and path quality.',
    tags: ['Dijkstra', 'A*', 'Shortest Path'],
  },
  {
    title: 'Resource Allocation',
    path: '/resource-allocation',
    theme: 'violet',
    icon: '📦',
    description:
      'Optimally distribute budgets, vehicles, and staff across destinations using dynamic programming and greedy strategies.',
    tags: ['Knapsack', 'Greedy', 'DP'],
  },
  {
    title: 'Network Analysis',
    path: '/network-analysis',
    theme: 'cyan',
    icon: '🔗',
    description:
      'Analyze transport networks with graph algorithms. Discover hubs via centrality measures and build minimum spanning trees.',
    tags: ['BFS', 'DFS', 'MST', 'Centrality'],
  },
  {
    title: 'Decision Support',
    path: '/decision-support',
    theme: 'emerald',
    icon: '🧠',
    description:
      'Get intelligent travel recommendations powered by multi-criteria decision analysis and weighted scoring models.',
    tags: ['MCDM', 'Scoring', 'Ranking'],
  },
  {
    title: 'Optimization Engine',
    path: '/optimization',
    theme: 'amber',
    icon: '⚡',
    description:
      'Benchmark and compare optimization algorithms including genetic algorithms, simulated annealing, and branch-and-bound.',
    tags: ['GA', 'SA', 'Branch & Bound'],
  },
];

const stats = [
  { number: '6+', label: 'Algorithms' },
  { number: '5', label: 'Modules' },
  { number: 'Real-time', label: 'Analysis' },
  { number: '100%', label: 'Interactive' },
];

export default function Dashboard() {
  return (
    <div className="page">
      <div className="page__container">
        {/* ── Hero ── */}
        <section className="hero">
          <div className="hero__badge">
            <span className="hero__badge-dot" />
            Intelligent Decision Support System
          </div>

          <h1 className="hero__title">
            Smarter Travel Decisions,{' '}
            <span className="hero__title-gradient">Powered by Algorithms</span>
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
              Each module targets a specific travel planning challenge
            </p>
          </div>

          <div className="modules__grid">
            {modules.map(({ title, path, theme, icon, description, tags }) => (
              <Link
                to={path}
                className={`module-card module-card--${theme}`}
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
        <footer className="footer">
          <p className="footer__text">
            © {new Date().getFullYear()} Travel IDSS — Intelligent Decision
            Support System
          </p>
        </footer>
      </div>
    </div>
  );
}
