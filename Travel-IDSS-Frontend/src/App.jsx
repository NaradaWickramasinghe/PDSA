// src/App.jsx
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/common/Navbar';
import Dashboard from './pages/Dashboard/Dashboard';
import NetworkAnalysis from './pages/NetworkAnalysis/NetworkAnalysis';
import NetworkMstPage from './pages/NetworkMstPage';
import OptimizationPage from './pages/Optimization/OptimizationPage';
import DecisionSupport from './pages/DecisionSupport/DecisionSupport';

function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/" element={<Dashboard />} />
        {/* Module routes */}
        <Route path="/route-optimization" element={<PlaceholderPage title="Route Optimization" />} />
        <Route path="/resource-allocation" element={<PlaceholderPage title="Resource Allocation" />} />
        <Route path="/network-analysis" element={<NetworkAnalysis />} />
        <Route path="/decision-support" element={<DecisionSupport />} />
        <Route path="/network-analysis/mst-prim" element={<NetworkMstPage />} />
        <Route path="/decision-support" element={<PlaceholderPage title="Decision Support" />} />
        <Route path="/optimization" element={<OptimizationPage />} />
      </Routes>
    </Router>
  );
}

// Temporary placeholder for module pages (will be replaced)
function PlaceholderPage({ title }) {
  return (
    <div className="page">
      <div className="page__container" style={{ paddingTop: '4rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 700, letterSpacing: '-0.02em' }}>{title}</h1>
        <p style={{ color: 'var(--color-text-secondary)', marginTop: '0.5rem' }}>
          This module is under construction. Components will be added soon.
        </p>
      </div>
    </div>
  );
}

export default App;