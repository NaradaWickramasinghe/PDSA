// src/pages/Optimization/OptimizationPage.jsx
// Main Optimization Engine page — assembles ConfigPanel, OptimizationMap, and results

import { useState } from 'react';
import { useOptimization } from '../../hooks/useOptimization';
import ConfigPanel from './ConfigPanel';
import OptimizationMap from './OptimizationMap';
import './Optimization.css';

export default function OptimizationPage() {
  const {
    networkData,
    optimizationResult,
    benchmarkResult,
    scalabilityResults,
    loading,
    error,
    planRoute,
    runBenchmark,
    runScalabilitySuite,
    refreshNetwork,
    clearError,
  } = useOptimization();

  const nodes = networkData.nodes || [];
  const edges = networkData.edges || [];

  // Form state — sliders are 0-100, backend expects 0.0-1.0 doubles
  const [formData, setFormData] = useState({
    sourceNodeId: '',
    destinationNodeId: '',
    algorithm: 'BRANCH_AND_BOUND',
    timeWeight: 35,
    costWeight: 35,
    safetyWeight: 20,
    qualityWeight: 10,
    maxTimeMinutes: 600,
    maxBudgetLkr: 50000,
  });

  // Set default source/dest when nodes load for the first time
  const [defaultsSet, setDefaultsSet] = useState(false);
  if (nodes.length > 0 && !defaultsSet) {
    setFormData(prev => ({
      ...prev,
      sourceNodeId: prev.sourceNodeId || nodes[0].nodeId,
      destinationNodeId: prev.destinationNodeId || (nodes.length > 1 ? nodes[nodes.length - 1].nodeId : nodes[0].nodeId),
    }));
    setDefaultsSet(true);
  }

  // Which alternative route is selected on the map
  const [selectedRouteIdx, setSelectedRouteIdx] = useState(0);

  const handleOptimize = async () => {
    try {
      await planRoute({
        sourceNodeId: formData.sourceNodeId,
        destinationNodeId: formData.destinationNodeId,
        algorithm: formData.algorithm,
        timeWeight: formData.timeWeight / 100,
        costWeight: formData.costWeight / 100,
        safetyWeight: formData.safetyWeight / 100,
        qualityWeight: formData.qualityWeight / 100,
        maxTimeMinutes: formData.maxTimeMinutes || null,
        maxBudgetLkr: formData.maxBudgetLkr || null,
      });
      setSelectedRouteIdx(0);
    } catch (_) {
      // error is handled by the hook
    }
  };

  const handleBenchmark = async () => {
    try {
      await runBenchmark(formData.sourceNodeId, formData.destinationNodeId);
    } catch (_) {
      // error is handled by the hook
    }
  };

  const handleRunScalabilitySuite = async () => {
    try {
      await runScalabilitySuite();
    } catch (_) {
    }
  };

  const handleRefreshNetwork = async () => {
    try {
      await refreshNetwork();
    } catch (_) {
    }
  };

  // Determine active route path for the map
  const bestRoute = optimizationResult?.bestRoute;
  const alternatives = optimizationResult?.paretoAlternatives || [];
  const activeRoute = selectedRouteIdx === 0
    ? bestRoute
    : alternatives[selectedRouteIdx - 1];
  const activeRoutePath = activeRoute?.pathNodeIds || [];

  return (
    <div className="page">
      <div className="opt-container">
        {/* ── LEFT: Config Panel ── */}
        <ConfigPanel
          nodes={nodes}
          formData={formData}
          setFormData={setFormData}
          onOptimize={handleOptimize}
          loading={loading}
        />

        {/* ── CENTER: Map ── */}
        <div className="opt-section" style={{ padding: 0, overflow: 'hidden', position: 'relative' }}>
          <button 
            className="opt-btn-primary" 
            style={{ position: 'absolute', top: '10px', right: '10px', zIndex: 1000, padding: '0.4rem 0.8rem', fontSize: '0.8rem', width: 'auto', boxShadow: '0 2px 10px rgba(0,0,0,0.5)' }}
            onClick={handleRefreshNetwork}
            disabled={loading}
          >
            🔄 Refresh Network
          </button>
          {error && (
            <div className="opt-error-banner">
              <span>⚠️ {error}</span>
              <button onClick={clearError} className="opt-error-dismiss">✕</button>
            </div>
          )}
          <OptimizationMap
            nodes={nodes}
            edges={edges}
            activeRoutePath={activeRoutePath}
          />
        </div>

        {/* ── RIGHT: Results Panel ── */}
        <div className="opt-section" style={{ overflowY: 'auto' }}>
          <div className="opt-section-title">
            Optimization Results
            {optimizationResult?.success && (
              <span className="opt-badge-ready">✓ Solution Found</span>
            )}
          </div>

          {/* Empty state */}
          {!optimizationResult && !loading && (
            <div style={{
              textAlign: 'center',
              padding: '3rem 1rem',
              color: 'var(--color-text-secondary)',
              fontSize: '0.88rem'
            }}>
              <div style={{ fontSize: '2.5rem', marginBottom: '0.75rem', opacity: 0.5 }}>🗺️</div>
              <p>Configure your trip parameters and click <strong>"Optimize Hiking Route"</strong> to find optimal paths.</p>
            </div>
          )}

          {/* Loading spinner */}
          {loading && (
            <div style={{
              textAlign: 'center',
              padding: '3rem 1rem',
              color: 'var(--color-text-secondary)'
            }}>
              <div className="opt-spinner" />
              <p style={{ marginTop: '0.75rem', fontSize: '0.85rem' }}>Running optimization engine…</p>
            </div>
          )}

          {/* Results */}
          {optimizationResult && !loading && (
            <>
              {/* Performance Metrics */}
              <div className="opt-metrics-grid">
                <div className="opt-stat-card">
                  <div className="opt-stat-label">Algorithm</div>
                  <div className="opt-stat-value" style={{ fontSize: '0.82rem' }}>
                    {optimizationResult.selectedAlgorithm?.replace(/_/g, ' ') || '—'}
                  </div>
                </div>
                <div className="opt-stat-card">
                  <div className="opt-stat-label">Execution Time</div>
                  <div className="opt-stat-value">{optimizationResult.executionTimeMs?.toFixed(1)}ms</div>
                </div>
                <div className="opt-stat-card">
                  <div className="opt-stat-label">Memory Used</div>
                  <div className="opt-stat-value">{optimizationResult.memoryUsedKb?.toFixed(1)}KB</div>
                </div>
                <div className="opt-stat-card">
                  <div className="opt-stat-label">Nodes Explored</div>
                  <div className="opt-stat-value">{optimizationResult.nodesExploredCount ?? '—'}</div>
                </div>
              </div>

              {/* Best Route Details */}
              {bestRoute && (
                <>
                  <div className="opt-section-title" style={{ marginTop: '0.5rem', fontSize: '0.85rem' }}>
                    🏆 Best Route — {bestRoute.label || 'Optimal'}
                  </div>

                  <div className="opt-metrics-grid">
                    <div className="opt-stat-card">
                      <div className="opt-stat-label">Distance</div>
                      <div className="opt-stat-value">{bestRoute.totalDistanceKm?.toFixed(1)} km</div>
                    </div>
                    <div className="opt-stat-card">
                      <div className="opt-stat-label">Duration</div>
                      <div className="opt-stat-value">{bestRoute.totalDurationMinutes?.toFixed(0)} min</div>
                    </div>
                    <div className="opt-stat-card">
                      <div className="opt-stat-label">Cost</div>
                      <div className="opt-stat-value">LKR {bestRoute.totalCostLkr?.toLocaleString()}</div>
                    </div>
                    <div className="opt-stat-card">
                      <div className="opt-stat-label">Composite Score</div>
                      <div className="opt-stat-value" style={{ color: 'var(--color-success)' }}>
                        {bestRoute.compositeScore?.toFixed(4)}
                      </div>
                    </div>
                  </div>

                  {/* Route Path Visualization */}
                  <div className="opt-route-path">
                    {bestRoute.pathNodeNames?.map((name, i) => (
                      <span key={i} style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                        <span className="opt-node-tag">{name}</span>
                        {i < bestRoute.pathNodeNames.length - 1 && (
                          <span className="opt-path-arrow">→</span>
                        )}
                      </span>
                    ))}
                  </div>
                </>
              )}

              {/* Alternative Routes (Pareto) */}
              {alternatives.length > 0 && (
                <>
                  <div className="opt-section-title" style={{ marginTop: '1rem', fontSize: '0.85rem' }}>
                    Alternative Routes ({alternatives.length})
                  </div>

                  {/* Best route card */}
                  <div
                    className={`opt-alt-card ${selectedRouteIdx === 0 ? 'active' : ''}`}
                    onClick={() => setSelectedRouteIdx(0)}
                  >
                    <div className="opt-alt-title">
                      🏆 {bestRoute?.label || 'Best Route'}
                    </div>
                    <div className="opt-alt-stats">
                      <span>{bestRoute?.totalDistanceKm?.toFixed(1)} km</span>
                      <span>{bestRoute?.totalDurationMinutes?.toFixed(0)} min</span>
                      <span>LKR {bestRoute?.totalCostLkr?.toLocaleString()}</span>
                    </div>
                  </div>

                  {alternatives.map((alt, idx) => (
                    <div
                      key={idx}
                      className={`opt-alt-card ${selectedRouteIdx === idx + 1 ? 'active' : ''}`}
                      onClick={() => setSelectedRouteIdx(idx + 1)}
                    >
                      <div className="opt-alt-title">
                        {alt.label || `Alternative ${idx + 1}`}
                      </div>
                      <div className="opt-alt-stats">
                        <span>{alt.totalDistanceKm?.toFixed(1)} km</span>
                        <span>{alt.totalDurationMinutes?.toFixed(0)} min</span>
                        <span>LKR {alt.totalCostLkr?.toLocaleString()}</span>
                      </div>
                    </div>
                  ))}
                </>
              )}

              {/* Benchmark Buttons */}
              <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
                <button
                  className="opt-btn-primary"
                  onClick={handleBenchmark}
                  disabled={loading}
                  style={{ flex: 1, background: 'var(--color-bg-secondary)', color: 'var(--color-text-primary)', boxShadow: 'none', border: '1px solid var(--color-border)' }}
                >
                  ⚡ Run Benchmark
                </button>
                <button
                  className="opt-btn-primary"
                  onClick={handleRunScalabilitySuite}
                  disabled={loading}
                  style={{ flex: 1, background: 'var(--color-bg-secondary)', color: 'var(--color-text-primary)', boxShadow: 'none', border: '1px solid var(--color-border)' }}
                >
                  📈 Run Scalability Suite
                </button>
              </div>

              {/* Benchmark Results */}
              {benchmarkResult && (
                <div style={{ marginTop: '1rem' }}>
                  <div className="opt-section-title" style={{ fontSize: '0.85rem' }}>
                    📊 Benchmark: {benchmarkResult.scenarioName}
                  </div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--color-text-secondary)', marginBottom: '0.5rem' }}>
                    Network: {benchmarkResult.networkNodesCount} nodes, {benchmarkResult.networkEdgesCount} edges
                  </div>

                  {benchmarkResult.algorithmMetrics && Object.entries(benchmarkResult.algorithmMetrics).map(([key, metric]) => (
                    <div key={key} className="opt-stat-card" style={{ marginBottom: '0.5rem' }}>
                      <div className="opt-stat-label" style={{ color: 'var(--color-primary)', fontWeight: 600 }}>
                        {metric.algorithmName || key}
                      </div>
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.25rem', marginTop: '0.35rem', fontSize: '0.78rem', color: 'var(--color-text-secondary)' }}>
                        <span>⏱ {metric.executionTimeMs?.toFixed(2)}ms</span>
                        <span>💾 {metric.memoryUsedKb?.toFixed(1)}KB</span>
                        <span>🎯 Score: {metric.bestCompositeScore?.toFixed(4)}</span>
                        <span>🔍 Explored: {metric.nodesExplored}</span>
                        <span>{metric.foundValidPath ? '✅ Path found' : '❌ No path'}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* Scalability Results */}
              {scalabilityResults && scalabilityResults.length > 0 && (
                <div style={{ marginTop: '1.5rem', borderTop: '1px solid var(--color-border)', paddingTop: '1rem' }}>
                  <div className="opt-section-title" style={{ fontSize: '0.85rem' }}>
                    📈 Scalability Suite Results
                  </div>
                  {scalabilityResults.map((result, idx) => (
                    <div key={idx} style={{ marginTop: '1rem' }}>
                      <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--color-text-primary)' }}>
                        {result.scenarioName} ({result.networkNodesCount} nodes, {result.networkEdgesCount} edges)
                      </div>
                      {result.algorithmMetrics && Object.entries(result.algorithmMetrics).map(([key, metric]) => (
                        <div key={key} className="opt-stat-card" style={{ marginBottom: '0.5rem', marginTop: '0.5rem' }}>
                          <div className="opt-stat-label" style={{ color: 'var(--color-primary)', fontWeight: 600 }}>
                            {metric.algorithmName || key}
                          </div>
                          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.25rem', marginTop: '0.35rem', fontSize: '0.78rem', color: 'var(--color-text-secondary)' }}>
                            <span>⏱ {metric.executionTimeMs?.toFixed(2)}ms</span>
                            <span>💾 {metric.memoryUsedKb?.toFixed(1)}KB</span>
                            <span>🎯 Score: {metric.bestCompositeScore?.toFixed(4)}</span>
                            <span>🔍 Explored: {metric.nodesExplored}</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
