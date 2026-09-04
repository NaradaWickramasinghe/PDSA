// src/pages/NetworkAnalysis/NetworkAnalysis.jsx
import { useState, useEffect, useCallback } from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer
} from 'recharts';
import { useNetworkAnalysis, WEIGHT_OPTIONS } from '../../hooks/useNetworkAnalysis';
import NetworkMap from './NetworkMap';
import { PrimMstSection } from '../../components/PrimMstSection';
import './NetworkAnalysis.css';

// Common dark theme style for HTML <option> dropdown items
const darkOptionStyle = {
  backgroundColor: '#1e293b',
  color: '#f8fafc'
};

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
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

// ─── Data Table ─────────────────────────────────────────────────
function RankingTable({ data, metric, barClass }) {
  if (!data?.length) return null;
  const maxVal = Math.max(...data.map((d) => d[metric] || 0));

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
            <tr key={item.nodeId || item.name}>
              <td className={`na-table__rank ${getRankClass(i)}`}>{i + 1}</td>
              <td className="na-table__name">{item.name}</td>
              <td style={{ fontFamily: 'monospace', fontSize: '0.8125rem' }}>
                {item.nodeId}
              </td>
              <td className="na-table__score">{(item[metric] || 0).toFixed(6)}</td>
              <td className="na-table__bar-cell">
                <div className="na-table__bar-container">
                  <div
                    className={`na-table__bar ${barClass}`}
                    style={{ width: `${maxVal ? ((item[metric] || 0) / maxVal) * 100 : 0}%` }}
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
  const [lookupError, setLookupError] = useState(null);
  const [localLocationScore, setLocalLocationScore] = useState(null);
  const [mapMetric, setMapMetric] = useState('betweenness');

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

  const handleLookup = async () => {
    const query = lookupId.trim();
    if (!query) return;

    setLookupError(null);

    // 1. Instant local lookup in already loaded analysisResult
    if (analysisResult?.rankedByBetweenness) {
      const qLower = query.toLowerCase();
      const match = analysisResult.rankedByBetweenness.find(
        (item) =>
          item.nodeId.toLowerCase() === qLower ||
          item.name.toLowerCase() === qLower ||
          item.nodeId.toLowerCase().includes(qLower) ||
          item.name.toLowerCase().includes(qLower)
      );

      if (match) {
        const bRank =
          analysisResult.rankedByBetweenness.findIndex((i) => i.nodeId === match.nodeId) + 1;
        const cRank =
          (analysisResult.rankedByCloseness || []).findIndex((i) => i.nodeId === match.nodeId) + 1;
        setLocalLocationScore({
          ...match,
          betweennessRank: bRank,
          closenessRank: cRank,
        });
        return;
      }
    }

    // 2. Fallback to API if not in local memory
    try {
      const data = await fetchLocationScore(query, selectedWeight);
      if (data) {
        const bRank =
          (analysisResult?.rankedByBetweenness || []).findIndex((i) => i.nodeId === data.nodeId) + 1;
        const cRank =
          (analysisResult?.rankedByCloseness || []).findIndex((i) => i.nodeId === data.nodeId) + 1;
        setLocalLocationScore({
          ...data,
          betweennessRank: bRank > 0 ? bRank : null,
          closenessRank: cRank > 0 ? cRank : null,
        });
      }
    } catch (err) {
      setLocalLocationScore(null);
      setLookupError(`Destination "${query}" was not found in the network.`);
    }
  };

  const handleLookupKeyDown = (e) => {
    if (e.key === 'Enter') handleLookup();
  };

  const weightLabel =
    WEIGHT_OPTIONS?.find((w) => w.value === analysisResult?.weightUsed)?.label ||
    analysisResult?.weightUsed ||
    '—';

  return (
    <div className="na-page">
      <div className="na-container">
        {/* ── Header ── */}
        <header className="na-header">
          <div className="na-header__badge">
            <span className="na-header__badge-dot" />
            <span>Network Analysis Module</span>
          </div>
          <h1 className="na-header__title">
            Transport Network{' '}
            <span className="na-header__title-gradient">Centrality Analysis</span>
          </h1>
          <p className="na-header__subtitle">
            Discover the most strategically important destinations in the travel network using Brandes&apos; algorithm. Identify gateway towns (betweenness) and best-connected hubs (closeness).
          </p>
        </header>

        {/* ── Controls ── */}
        <div className="na-controls">
          <label htmlFor="weight-select" className="na-controls__label">Edge Weight:</label>
          <select
            className="na-controls__select"
            value={selectedWeight}
            onChange={handleWeightChange}
            disabled={loading}
            id="weight-select"
          >
            {WEIGHT_OPTIONS?.map((opt) => (
              <option key={opt.value} value={opt.value} style={darkOptionStyle}>
                {opt.label}
              </option>
            ))}
          </select>
          <button
            type="button"
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
            <button type="button" className="na-error__dismiss" onClick={clearError}>
              Dismiss
            </button>
          </div>
        )}

        {/* ── Loading ── */}
        {loading && (
          <div className="na-loading">
            <div className="na-spinner" />
            <span className="na-loading__text">
              Running Brandes&apos; algorithm on the network…
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
                  data={analysisResult.rankedByBetweenness?.slice(0, 10) || []}
                  dataKey="betweenness"
                  gradientId="betweennessGradient"
                  color1="#06b6d4"
                  color2="#0891b2"
                  metricLabel="Betweenness"
                />
                <RankingTable
                  data={analysisResult.rankedByBetweenness || []}
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
                  data={analysisResult.rankedByCloseness?.slice(0, 10) || []}
                  dataKey="closeness"
                  gradientId="closenessGradient"
                  color1="#8b5cf6"
                  color2="#7c3aed"
                  metricLabel="Closeness"
                />
                <RankingTable
                  data={analysisResult.rankedByCloseness || []}
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
                    type="button"
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
                    type="button"
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
                  data={mapMetric === 'betweenness' ? (analysisResult.rankedByBetweenness || []) : (analysisResult.rankedByCloseness || [])} 
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
                    Look up centrality scores and gateway rankings for any destination by Name or Node ID
                  </p>
                </div>
              </div>
              <div className="na-card">
                <div className="na-lookup">
                  <input
                    className="na-lookup__input"
                    type="text"
                    list="destinations-lookup-list"
                    placeholder="Search by Node ID or Name (e.g. HMB_PORT, Kandy, Colombo...)"
                    value={lookupId}
                    onChange={(e) => {
                      setLookupId(e.target.value);
                      if (lookupError) setLookupError(null);
                      if (localLocationScore) setLocalLocationScore(null);
                      if (locationScore) clearLocationScore();
                    }}
                    onKeyDown={handleLookupKeyDown}
                    id="lookup-input"
                  />
                  <datalist id="destinations-lookup-list">
                    {(analysisResult?.rankedByBetweenness || []).map((node) => (
                      <option key={node.nodeId} value={node.nodeId}>
                        {node.name} ({node.nodeId})
                      </option>
                    ))}
                  </datalist>
                  <button
                    type="button"
                    className="na-lookup__btn"
                    onClick={handleLookup}
                    disabled={locationLoading || !lookupId.trim()}
                    id="lookup-btn"
                  >
                    {locationLoading ? 'Searching...' : 'Search'}
                  </button>
                </div>

                {/* Local Inline Error for Lookup */}
                {lookupError && (
                  <div className="na-error" style={{ marginTop: '1.25rem', marginBottom: 0 }}>
                    <span className="na-error__icon">⚠️</span>
                    <span className="na-error__text">{lookupError}</span>
                    <button
                      type="button"
                      className="na-error__dismiss"
                      onClick={() => setLookupError(null)}
                    >
                      Dismiss
                    </button>
                  </div>
                )}

                {/* Lookup Result Card */}
                {(localLocationScore || locationScore) && (() => {
                  const score = localLocationScore || locationScore;
                  return (
                    <div className="na-location-result">
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '0.5rem' }}>
                        <div>
                          <div className="na-location-result__name" style={{ fontSize: '1.25rem', fontWeight: 700, color: '#f8fafc' }}>
                            📍 {score.name}
                          </div>
                          <div className="na-location-result__id" style={{ color: '#94a3b8', fontSize: '0.8125rem' }}>
                            Node ID: <code style={{ color: '#06b6d4', background: 'rgba(6, 182, 212, 0.1)', padding: '2px 6px', borderRadius: '4px' }}>{score.nodeId}</code>
                            {score.latitude && score.longitude && (
                              <span style={{ marginLeft: '0.75rem', color: '#64748b' }}>
                                Coordinates: {score.latitude.toFixed(4)}°, {score.longitude.toFixed(4)}°
                              </span>
                            )}
                          </div>
                        </div>
                        <button
                          type="button"
                          onClick={() => {
                            setLocalLocationScore(null);
                            clearLocationScore();
                            setLookupId('');
                          }}
                          style={{
                            background: 'transparent',
                            border: '1px solid rgba(255,255,255,0.1)',
                            borderRadius: '4px',
                            color: '#94a3b8',
                            fontSize: '0.75rem',
                            padding: '4px 8px',
                            cursor: 'pointer',
                          }}
                        >
                          ✕ Clear
                        </button>
                      </div>

                      <div className="na-location-result__scores">
                        <div className="na-location-result__score-card">
                          <div className="na-location-result__score-label">
                            Betweenness Centrality
                            {score.betweennessRank && (
                              <span style={{ marginLeft: '0.5rem', color: '#06b6d4', fontWeight: 700 }}>
                                (Rank #{score.betweennessRank})
                              </span>
                            )}
                          </div>
                          <div className="na-location-result__score-value na-location-result__score-value--cyan">
                            {(score.betweenness || 0).toFixed(6)}
                          </div>
                          <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.25rem' }}>
                            Bridge / Gateway Score
                          </div>
                        </div>

                        <div className="na-location-result__score-card">
                          <div className="na-location-result__score-label">
                            Closeness Centrality
                            {score.closenessRank && (
                              <span style={{ marginLeft: '0.5rem', color: '#8b5cf6', fontWeight: 700 }}>
                                (Rank #{score.closenessRank})
                              </span>
                            )}
                          </div>
                          <div className="na-location-result__score-value na-location-result__score-value--violet">
                            {(score.closeness || 0).toFixed(6)}
                          </div>
                          <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.25rem' }}>
                            Accessibility / Central Hub Score
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })()}
              </div>
            </section>

            {/* ── Prim's Minimum Spanning Tree (MST) Section ── */}
            <PrimMstSection 
              allNodes={analysisResult.rankedByBetweenness || []} 
              allEdges={analysisResult.edges || []} 
              selectedWeight={selectedWeight}
              optionStyle={darkOptionStyle}
            />
          </>
        )}

        {/* ── Empty State ── */}
        {!loading && !analysisResult && !error && (
          <div className="na-empty">
            <div className="na-empty__icon">🔗</div>
            <h3 className="na-empty__title">No Analysis Data</h3>
            <p className="na-empty__text">
              Select an edge weight and click &quot;Run Analysis&quot; to analyze the travel network.
            </p>
          </div>
        )}

        {/* ── Footer ── */}
        <footer className="footer" style={{ marginTop: '2rem' }}>
          <p className="footer__text">
            © 2026 Travel IDSS — Network Analysis Module
          </p>
        </footer>
      </div>
    </div>
  );
}