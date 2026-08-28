// src/pages/NetworkAnalysis/NetworkAnalysis.jsx
// Network Analysis page — betweenness & closeness centrality rankings

import { useState, useEffect, useCallback } from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from 'recharts';
import { useNetworkAnalysis, WEIGHT_OPTIONS } from '../../hooks/useNetworkAnalysis';
import NetworkMap from './NetworkMap';
import './NetworkAnalysis.css';

// ─── Custom Recharts Tooltip ───────────────────────────────────
function CustomTooltip({ active, payload, label, metricLabel }) {
  if (!active || !payload?.length) return null;
  return (
    <div
      style={{
        background: 'rgba(17, 24, 39, 0.95)',
        border: '1px solid rgba(255,255,255,0.1)',
        borderRadius: 8,
        padding: '10px 14px',
        fontSize: '0.8125rem',
      }}
    >
      <p style={{ fontWeight: 700, marginBottom: 4 }}>{label}</p>
      <p style={{ color: payload[0]?.color || '#06b6d4' }}>
        {metricLabel}: {payload[0]?.value?.toFixed(6)}
      </p>
    </div>
  );
}

// ─── Bar Chart Section ──────────────────────────────────────────
function CentralityChart({ data, dataKey, gradientId, color1, color2, metricLabel }) {
  return (
    <div className="na-chart">
      <ResponsiveContainer width="100%" height={320}>
        <BarChart
          data={data}
          margin={{ top: 10, right: 10, left: 0, bottom: 60 }}
        >
          <defs>
            <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={color1} stopOpacity={0.9} />
              <stop offset="100%" stopColor={color2} stopOpacity={0.4} />
            </linearGradient>
          </defs>
          <CartesianGrid
            strokeDasharray="3 3"
            stroke="rgba(255,255,255,0.04)"
            vertical={false}
          />
          <XAxis
            dataKey="name"
            tick={{ fill: '#64748b', fontSize: 11 }}
            angle={-45}
            textAnchor="end"
            height={80}
            axisLine={{ stroke: 'rgba(255,255,255,0.06)' }}
            tickLine={false}
          />
          <YAxis
            tick={{ fill: '#64748b', fontSize: 11 }}
            axisLine={false}
            tickLine={false}
            tickFormatter={(v) => v.toFixed(4)}
          />
          <Tooltip content={<CustomTooltip metricLabel={metricLabel} />} />
          <Bar
            dataKey={dataKey}
            fill={`url(#${gradientId})`}
            radius={[4, 4, 0, 0]}
            maxBarSize={48}
          >
            {data.map((_, index) => (
              <Cell
                key={index}
                fillOpacity={1 - index * 0.06 > 0.3 ? 1 - index * 0.06 : 0.3}
              />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

// ─── Data Table ─────────────────────────────────────────────────
function RankingTable({ data, metric, barClass }) {
  if (!data?.length) return null;
  const maxVal = Math.max(...data.map((d) => d[metric]));

  const getRankClass = (i) => {
    if (i === 0) return 'na-table__rank--gold';
    if (i === 1) return 'na-table__rank--silver';
    if (i === 2) return 'na-table__rank--bronze';
    return '';
  };

  return (
    <div className="na-table-wrapper">
      <table className="na-table">
        <thead>
          <tr>
            <th style={{ width: 48 }}>#</th>
            <th>Destination</th>
            <th>Node ID</th>
            <th>Score</th>
            <th className="na-table__bar-cell">Distribution</th>
          </tr>
        </thead>
        <tbody>
          {data.map((item, i) => (
            <tr key={item.nodeId}>
              <td className={`na-table__rank ${getRankClass(i)}`}>{i + 1}</td>
              <td className="na-table__name">{item.name}</td>
              <td style={{ fontFamily: 'monospace', fontSize: '0.8125rem' }}>
                {item.nodeId}
              </td>
              <td className="na-table__score">{item[metric].toFixed(6)}</td>
              <td className="na-table__bar-cell">
                <div className="na-table__bar-container">
                  <div
                    className={`na-table__bar ${barClass}`}
                    style={{ width: `${maxVal ? (item[metric] / maxVal) * 100 : 0}%` }}
                  />
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ─── Main Page Component ────────────────────────────────────────
export default function NetworkAnalysis() {
  const {
    analysisResult,
    locationScore,
    selectedWeight,
    loading,
    locationLoading,
    error,
    fetchFullAnalysis,
    fetchLocationScore,
    clearError,
    clearLocationScore,
  } = useNetworkAnalysis();

  const [lookupId, setLookupId] = useState('');
  const [mapMetric, setMapMetric] = useState('betweenness');

  // Fetch on mount and when weight changes
  const handleAnalyze = useCallback(
    (weight) => {
      fetchFullAnalysis(weight);
    },
    [fetchFullAnalysis]
  );

  useEffect(() => {
    handleAnalyze(selectedWeight);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleWeightChange = (e) => {
    const weight = e.target.value;
    handleAnalyze(weight);
  };

  const handleLookup = () => {
    if (lookupId.trim()) {
      fetchLocationScore(lookupId.trim(), selectedWeight);
    }
  };

  const handleLookupKeyDown = (e) => {
    if (e.key === 'Enter') handleLookup();
  };

  // Shorten weight label for stats display
  const weightLabel =
    WEIGHT_OPTIONS.find((w) => w.value === analysisResult?.weightUsed)?.label ||
    analysisResult?.weightUsed ||
    '—';

  return (
    <div className="na-page">
      <div className="na-container">
        {/* ── Header ── */}
        <header className="na-header">
          <div className="na-header__badge">
            <span className="na-header__badge-dot" />
            Network Analysis Module
          </div>
          <h1 className="na-header__title">
            Transport Network{' '}
            <span className="na-header__title-gradient">Centrality Analysis</span>
          </h1>
          <p className="na-header__subtitle">
            Discover the most strategically important destinations in the travel
            network using Brandes' algorithm. Identify gateway towns (betweenness)
            and best-connected hubs (closeness).
          </p>
        </header>

        {/* ── Controls ── */}
        <div className="na-controls">
          <span className="na-controls__label">Edge Weight:</span>
          <select
            className="na-controls__select"
            value={selectedWeight}
            onChange={handleWeightChange}
            disabled={loading}
            id="weight-select"
          >
            {WEIGHT_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
          <button
            className="na-controls__btn"
            onClick={() => handleAnalyze(selectedWeight)}
            disabled={loading}
            id="analyze-btn"
          >
            {loading ? '⟳ Analyzing...' : '▶ Run Analysis'}
          </button>
        </div>

        {/* ── Error ── */}
        {error && (
          <div className="na-error">
            <span className="na-error__icon">⚠️</span>
            <span className="na-error__text">{error}</span>
            <button className="na-error__dismiss" onClick={clearError}>
              Dismiss
            </button>
          </div>
        )}

        {/* ── Loading ── */}
        {loading && (
          <div className="na-loading">
            <div className="na-spinner" />
            <span className="na-loading__text">
              Running Brandes' algorithm on the network…
            </span>
          </div>
        )}

        {/* ── Results ── */}
        {!loading && analysisResult && (
          <>
            {/* Stats Bar */}
            <div className="na-stats">
              <div className="na-stats__card">
                <div className="na-stats__value">{analysisResult.nodeCount}</div>
                <div className="na-stats__label">Destinations</div>
              </div>
              <div className="na-stats__card">
                <div className="na-stats__value">{analysisResult.edgeCount}</div>
                <div className="na-stats__label">Routes</div>
              </div>
              <div className="na-stats__card">
                <div className="na-stats__value">{weightLabel}</div>
                <div className="na-stats__label">Weight Metric</div>
              </div>
              <div className="na-stats__card">
                <div className="na-stats__value">{analysisResult.computationTimeMs}ms</div>
                <div className="na-stats__label">Computation Time</div>
              </div>
            </div>

            {/* ── Betweenness Section ── */}
            <section className="na-section na-section--betweenness">
              <div className="na-section__header">
                <div className="na-section__icon na-section__icon--betweenness">🔀</div>
                <div>
                  <h2 className="na-section__title">Betweenness Centrality</h2>
                  <p className="na-section__subtitle">
                    Gateway towns that control the flow between regions
                  </p>
                </div>
              </div>
              <div className="na-card">
                <CentralityChart
                  data={analysisResult.rankedByBetweenness.slice(0, 10)}
                  dataKey="betweenness"
                  gradientId="betweennessGradient"
                  color1="#06b6d4"
                  color2="#0891b2"
                  metricLabel="Betweenness"
                />
                <RankingTable
                  data={analysisResult.rankedByBetweenness}
                  metric="betweenness"
                  barClass="na-table__bar--cyan"
                />
              </div>
            </section>

            {/* ── Closeness Section ── */}
            <section className="na-section na-section--closeness">
              <div className="na-section__header">
                <div className="na-section__icon na-section__icon--closeness">📍</div>
                <div>
                  <h2 className="na-section__title">Closeness Centrality</h2>
                  <p className="na-section__subtitle">
                    Best-connected destinations to reach all others quickly
                  </p>
                </div>
              </div>
              <div className="na-card">
                <CentralityChart
                  data={analysisResult.rankedByCloseness.slice(0, 10)}
                  dataKey="closeness"
                  gradientId="closenessGradient"
                  color1="#8b5cf6"
                  color2="#7c3aed"
                  metricLabel="Closeness"
                />
                <RankingTable
                  data={analysisResult.rankedByCloseness}
                  metric="closeness"
                  barClass="na-table__bar--violet"
                />
              </div>
            </section>

            {/* ── Map Visualization ── */}
            <section className="na-section na-section--map">
              <div className="na-section__header" style={{ justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div className="na-section__icon" style={{ background: 'rgba(255,255,255,0.1)', color: '#fff' }}>🗺️</div>
                  <div>
                    <h2 className="na-section__title">Map Visualization</h2>
                    <p className="na-section__subtitle">
                      Geographic distribution of network centrality
                    </p>
                  </div>
                </div>
                
                {/* Map Toggle Controls */}
                <div style={{ display: 'flex', gap: '0.5rem', background: 'var(--color-bg-card)', padding: '0.25rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)' }}>
                  <button 
                    onClick={() => setMapMetric('betweenness')}
                    style={{
                      padding: '0.375rem 0.75rem', 
                      fontSize: '0.8125rem',
                      fontWeight: 600,
                      borderRadius: 'var(--radius-sm)',
                      background: mapMetric === 'betweenness' ? 'rgba(6, 182, 212, 0.15)' : 'transparent',
                      color: mapMetric === 'betweenness' ? '#06b6d4' : 'var(--color-text-secondary)',
                      border: 'none',
                      cursor: 'pointer',
                      transition: '0.2s'
                    }}
                  >
                    Betweenness
                  </button>
                  <button 
                    onClick={() => setMapMetric('closeness')}
                    style={{
                      padding: '0.375rem 0.75rem', 
                      fontSize: '0.8125rem',
                      fontWeight: 600,
                      borderRadius: 'var(--radius-sm)',
                      background: mapMetric === 'closeness' ? 'rgba(139, 92, 246, 0.15)' : 'transparent',
                      color: mapMetric === 'closeness' ? '#8b5cf6' : 'var(--color-text-secondary)',
                      border: 'none',
                      cursor: 'pointer',
                      transition: '0.2s'
                    }}
                  >
                    Closeness
                  </button>
                </div>
              </div>
              <div className="na-card" style={{ padding: 0, border: 'none' }}>
                <NetworkMap 
                  data={mapMetric === 'betweenness' ? analysisResult.rankedByBetweenness : analysisResult.rankedByCloseness} 
                  metric={mapMetric} 
                />
              </div>
            </section>

            {/* ── Location Lookup ── */}
            <section className="na-section na-section--lookup">
              <div className="na-section__header">
                <div className="na-section__icon na-section__icon--lookup">🔎</div>
                <div>
                  <h2 className="na-section__title">Destination Lookup</h2>
                  <p className="na-section__subtitle">
                    Look up centrality scores for a specific destination by Node ID
                  </p>
                </div>
              </div>
              <div className="na-card">
                <div className="na-lookup">
                  <input
                    className="na-lookup__input"
                    type="text"
                    placeholder="Enter Node ID (e.g. N001)"
                    value={lookupId}
                    onChange={(e) => {
                      setLookupId(e.target.value);
                      if (locationScore) clearLocationScore();
                    }}
                    onKeyDown={handleLookupKeyDown}
                    id="lookup-input"
                  />
                  <button
                    className="na-lookup__btn"
                    onClick={handleLookup}
                    disabled={locationLoading || !lookupId.trim()}
                    id="lookup-btn"
                  >
                    {locationLoading ? 'Searching...' : 'Search'}
                  </button>
                </div>

                {locationScore && (
                  <div className="na-location-result">
                    <div className="na-location-result__name">{locationScore.name}</div>
                    <div className="na-location-result__id">
                      Node ID: {locationScore.nodeId}
                    </div>
                    <div className="na-location-result__scores">
                      <div className="na-location-result__score-card">
                        <div className="na-location-result__score-label">
                          Betweenness Centrality
                        </div>
                        <div className="na-location-result__score-value na-location-result__score-value--cyan">
                          {locationScore.betweenness.toFixed(6)}
                        </div>
                      </div>
                      <div className="na-location-result__score-card">
                        <div className="na-location-result__score-label">
                          Closeness Centrality
                        </div>
                        <div className="na-location-result__score-value na-location-result__score-value--violet">
                          {locationScore.closeness.toFixed(6)}
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </section>
          </>
        )}

        {/* ── Empty State ── */}
        {!loading && !analysisResult && !error && (
          <div className="na-empty">
            <div className="na-empty__icon">🔗</div>
            <h3 className="na-empty__title">No Analysis Data</h3>
            <p className="na-empty__text">
              Select an edge weight and click "Run Analysis" to analyze the travel
              network.
            </p>
          </div>
        )}

        {/* ── Footer ── */}
        <footer className="footer" style={{ marginTop: '2rem' }}>
          <p className="footer__text">
            © {new Date().getFullYear()} Travel IDSS — Network Analysis Module
          </p>
        </footer>
      </div>
    </div>
  );
}
