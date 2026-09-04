import 'leaflet/dist/leaflet.css';
// src/App.jsx
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/common/Navbar';
import Dashboard from './pages/Dashboard/Dashboard';
import NetworkAnalysis from './pages/NetworkAnalysis/NetworkAnalysis';
import NetworkMstPage from './pages/NetworkAnalysis/NetworkMstPage'; 
import OptimizationPage from './pages/Optimization/OptimizationPage';
import DecisionSupport from './pages/DecisionSupport/DecisionSupport';
import RouteOptimization from './pages/RouteOptimization/RouteOptimization';
import ResourceAllocation from './pages/ResourceAllocation/ResourceAllocation';

function App() {
  return (
    <Router>
      <div className="reference-app-layout">
        <Navbar />
        <div className="main-wrapper">
          <div className="content-container">
            <Routes>
              <Route path="/" element={<Dashboard />} />
              {/* Module routes */}
              <Route path="/route-optimization" element={<RouteOptimization />} />
              <Route path="/resource-allocation" element={<ResourceAllocation />} />
              <Route path="/network-analysis" element={<NetworkAnalysis />} />
              <Route path="/decision-support" element={<DecisionSupport />} />
              <Route path="/network-analysis/mst-prim" element={<NetworkMstPage />} />
              <Route path="/optimization" element={<OptimizationPage />} />
            </Routes>
          </div>
        </div>
      </div>
    </Router>
  );
}

// Temporary placeholder for module pages
function PlaceholderPage({ title }) {
  return (
    <div className="page">
      <div className="page__container">
        <h1 style={{ fontSize: '2rem', fontWeight: 700, letterSpacing: '-0.02em' }}>{title}</h1>
        <p style={{ color: 'var(--color-text-secondary)', marginTop: '0.5rem' }}>
          This module is under construction. Components will be added soon.
        </p>
      </div>
    </div>
  );
}

export default App;