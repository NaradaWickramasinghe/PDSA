// src/components/PrimMstSection.jsx
import { useState, useMemo, useEffect } from 'react';
import { networkService } from '../services/networkService';
import { MapContainer, TileLayer, CircleMarker, Polyline, Tooltip } from 'react-leaflet';

export function PrimMstSection({
  allNodes = [],
  selectedWeight = 'distance_km',
  optionStyle,
}) {
  const [startNode, setStartNode] = useState('');
  const [mstResult, setMstResult] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [activeTreeTab, setActiveTreeTab] = useState('ALL');

  // Fallback dark styling for standard select options
  const darkOption = optionStyle || {
    backgroundColor: '#1e293b',
    color: '#f8fafc',
  };

  // Build a lookup map of Node ID -> Name to display friendly names in the table
  const nodeNameMap = useMemo(() => {
    const map = {};
    allNodes.forEach((node) => {
      const id = node.nodeId || node.id || node.code;
      if (id) {
        map[id] = node.name || id;
      }
    });
    return map;
  }, [allNodes]);

  // Unit suffix based on current weight metric
  const weightUnit = useMemo(() => {
    switch (selectedWeight) {
      case 'travel_time_minutes':
        return 'mins';
      case 'estimated_cost_lkr':
        return 'LKR';
      case 'distance_km':
      default:
        return 'km';
    }
  }, [selectedWeight]);

  const handleComputeMst = async (overrideStartNode) => {
    const nodeToUse = overrideStartNode !== undefined ? overrideStartNode : startNode;
    setError(null);
    setLoading(true);

    try {
      const res = await networkService.getMstPrimAnalysis(
        selectedWeight,
        nodeToUse.trim()
      );
      setMstResult(res.data);
      setActiveTreeTab('ALL');
    } catch (err) {
      console.error('MST Prim Computation Error:', err);
      const errMsg =
        err.response?.data?.message ||
        err.message ||
        'Failed to compute Minimum Spanning Tree. Ensure the backend server is running on port 8080.';
      setError(errMsg);
    } finally {
      setLoading(false);
    }
  };

  // Re-compute if weight changed and user already has calculated MST
  useEffect(() => {
    if (mstResult) {
      handleComputeMst(startNode);
    }
  }, [selectedWeight]); // eslint-disable-line react-hooks/exhaustive-deps

  // Extract trees and edge lists
  const trees = useMemo(() => {
    if (!mstResult) return [];
    if (Array.isArray(mstResult.trees)) {
      return mstResult.trees;
    }
    return [];
  }, [mstResult]);

  // All edges across all trees combined
  const allEdges = useMemo(() => {
    if (!mstResult) return [];
    if (trees.length > 0) {
      return trees.flatMap((t, treeIdx) =>
        (t.edges || []).map((e) => ({ ...e, treeIndex: treeIdx + 1, treeRoot: t.startedFromLocationId }))
      );
    }
    return (mstResult.mstEdges || mstResult.edges || []).map((e) => ({ ...e, treeIndex: 1 }));
  }, [mstResult, trees]);

  // Filtered edge list according to active component tab
  const displayedEdges = useMemo(() => {
    if (activeTreeTab === 'ALL') {
      return allEdges;
    }
    const idx = parseInt(activeTreeTab, 10);
    if (!isNaN(idx) && trees[idx]) {
      return (trees[idx].edges || []).map((e) => ({
        ...e,
        treeIndex: idx + 1,
        treeRoot: trees[idx].startedFromLocationId,
      }));
    }
    return allEdges;
  }, [activeTreeTab, allEdges, trees]);

  // Total cost / Forest weight
  const totalCost = useMemo(() => {
    if (!mstResult) return 0;
    if (activeTreeTab !== 'ALL') {
      const idx = parseInt(activeTreeTab, 10);
      if (!isNaN(idx) && trees[idx]) {
        return trees[idx].totalWeight ?? 0;
      }
    }
    return (
      mstResult.totalForestWeight ??
      mstResult.totalCost ??
      trees.reduce((acc, t) => acc + (t.totalWeight || 0), 0)
    );
  }, [mstResult, activeTreeTab, trees]);

  // Maximum edge weight for relative distribution bar
  const maxEdgeWeight = useMemo(() => {
    if (!displayedEdges.length) return 1;
    return Math.max(...displayedEdges.map((e) => e.weight ?? e.cost ?? 0), 1);
  }, [displayedEdges]);

  return (
    <section className="na-section na-section--mst">
      <div className="na-section__header">
        <div
          className="na-section__icon"
          style={{ background: 'rgba(16, 185, 129, 0.1)', color: '#10b981' }}
        >
          🌲
        </div>
        <div>
          <h2 className="na-section__title">Minimum Spanning Tree (Prim&apos;s Algorithm)</h2>
          <p className="na-section__subtitle">
            Find the optimal backbone connection graph connecting destinations with minimum total cost using a binary heap Priority Queue
          </p>
        </div>
      </div>

      <div className="na-card">
        {/* Controls */}
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
          <label
            htmlFor="mst-start-node"
            style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--color-text-secondary)' }}
          >
            Starting Destination:
          </label>
          <select
            id="mst-start-node"
            value={startNode}
            onChange={(e) => setStartNode(e.target.value)}
            style={{
              padding: '0.625rem 1rem',
              borderRadius: 'var(--radius-sm)',
              background: '#0f172a',
              border: '1px solid var(--color-border)',
              color: '#f8fafc',
              fontSize: '0.875rem',
              minWidth: '260px',
            }}
          >
            <option value="" style={darkOption}>
              🌐 Entire Network (Auto Root)
            </option>
            {allNodes.map((node) => {
              const id = node.nodeId || node.id || node.code;
              return (
                <option key={id} value={id} style={darkOption}>
                  {node.name || id} ({id})
                </option>
              );
            })}
          </select>

          <button
            type="button"
            onClick={() => handleComputeMst()}
            disabled={loading}
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.625rem 1.5rem',
              borderRadius: 'var(--radius-sm)',
              background: 'linear-gradient(135deg, #065f46, #047857)',
              color: '#fff',
              border: 'none',
              fontWeight: 600,
              fontSize: '0.875rem',
              cursor: loading ? 'not-allowed' : 'pointer',
              opacity: loading ? 0.7 : 1,
              boxShadow: '0 0 16px rgba(16, 185, 129, 0.25)',
              transition: 'all 0.2s ease',
            }}
          >
            {loading ? '⟳ Computing MST...' : '▶ Compute MST'}
          </button>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="na-error" style={{ marginBottom: '1.5rem' }}>
            <span className="na-error__icon">⚠️</span>
            <span className="na-error__text">{error}</span>
            <button
              type="button"
              className="na-error__dismiss"
              onClick={() => setError(null)}
            >
              Dismiss
            </button>
          </div>
        )}

        {/* Results */}
        {mstResult && (
          <div>
            {/* Stats Cards */}
            <div className="na-stats" style={{ marginBottom: '1.5rem' }}>
              <div className="na-stats__card">
                <div className="na-stats__value" style={{ color: '#10b981' }}>
                  {totalCost.toFixed(2)} <span style={{ fontSize: '0.875rem', color: '#94a3b8' }}>{weightUnit}</span>
                </div>
                <div className="na-stats__label">
                  {activeTreeTab === 'ALL' ? 'Total Forest Cost' : 'Component Cost'}
                </div>
              </div>
              <div className="na-stats__card">
                <div className="na-stats__value">{displayedEdges.length}</div>
                <div className="na-stats__label">Spanning Edges</div>
              </div>
              <div className="na-stats__card">
                <div className="na-stats__value">
                  {trees.length > 0 ? trees.length : 1}
                </div>
                <div className="na-stats__label">
                  {trees.length > 1 ? 'Connected Components' : 'Spanning Tree'}
                </div>
              </div>
              <div className="na-stats__card">
                <div className="na-stats__value">{mstResult.computationTimeMs ?? 0}ms</div>
                <div className="na-stats__label">Computation Time</div>
              </div>
            </div>

            {/* Map Visualization */}
            <div style={{ height: '400px', marginBottom: '1.5rem', borderRadius: '8px', overflow: 'hidden', border: '1px solid var(--color-border)' }}>
              <MapContainer center={[7.8731, 80.7718]} zoom={7} scrollWheelZoom={true} style={{ height: '100%', width: '100%' }}>
                <TileLayer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  attribution='&copy; OpenStreetMap contributors'
                  className="dark-map-tiles"
                />
                
                {/* Draw Edges */}
                {displayedEdges.map((edge, idx) => {
                  const fromId = edge.fromLocationId || edge.fromNodeId || edge.sourceNodeId || edge.fromId;
                  const toId = edge.toLocationId || edge.toNodeId || edge.targetNodeId || edge.toId;
                  const n1 = allNodes.find(n => (n.nodeId || n.id || n.code) === fromId);
                  const n2 = allNodes.find(n => (n.nodeId || n.id || n.code) === toId);
                  
                  if (!n1 || !n2 || n1.latitude == null || n2.latitude == null) return null;

                  return (
                    <Polyline
                      key={`mst-edge-${idx}`}
                      positions={[
                        [n1.latitude, n1.longitude],
                        [n2.latitude, n2.longitude]
                      ]}
                      pathOptions={{ color: 'var(--color-accent-primary)', weight: 3, opacity: 0.8 }}
                    />
                  );
                })}

                {/* Draw Nodes */}
                {allNodes.filter(n => n.latitude != null && n.longitude != null).map(n => {
                   const nodeId = n.nodeId || n.id || n.code;
                   
                   // Find if the node is part of the displayed MST
                   const inMST = displayedEdges.some(e => {
                       const f = e.fromLocationId || e.fromNodeId || e.sourceNodeId || e.fromId;
                       const t = e.toLocationId || e.toNodeId || e.targetNodeId || e.toId;
                       return f === nodeId || t === nodeId;
                   });
                   
                   if (!inMST) return null;

                   return (
                     <CircleMarker
                       key={`node-${nodeId}`}
                       center={[n.latitude, n.longitude]}
                       radius={6}
                       pathOptions={{ 
                         color: 'var(--color-bg-secondary)', 
                         fillColor: 'var(--color-accent-secondary)', 
                         fillOpacity: 1, 
                         weight: 2 
                       }}
                     >
                       <Tooltip>
                         <strong>{n.name || nodeId}</strong>
                       </Tooltip>
                     </CircleMarker>
                   );
                })}
              </MapContainer>
            </div>

            {/* Tree Component Tabs (if graph has multiple components) */}
            {trees.length > 1 && (
              <div
                style={{
                  display: 'flex',
                  gap: '0.5rem',
                  marginBottom: '1rem',
                  flexWrap: 'wrap',
                  alignItems: 'center',
                }}
              >
                <span style={{ fontSize: '0.8125rem', color: '#94a3b8', fontWeight: 600, marginRight: '0.5rem' }}>
                  Components:
                </span>
                <button
                  type="button"
                  onClick={() => setActiveTreeTab('ALL')}
                  style={{
                    padding: '0.375rem 0.75rem',
                    fontSize: '0.8125rem',
                    fontWeight: 600,
                    borderRadius: 'var(--radius-sm)',
                    background: activeTreeTab === 'ALL' ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                    color: activeTreeTab === 'ALL' ? '#10b981' : '#94a3b8',
                    border: activeTreeTab === 'ALL' ? '1px solid #10b981' : '1px solid rgba(255, 255, 255, 0.1)',
                    cursor: 'pointer',
                  }}
                >
                  All Trees ({allEdges.length} edges)
                </button>
                {trees.map((t, idx) => {
                  const rootName = nodeNameMap[t.startedFromLocationId] || t.startedFromLocationId;
                  const isSelected = activeTreeTab === String(idx);
                  return (
                    <button
                      key={`tree-tab-${idx}`}
                      type="button"
                      onClick={() => setActiveTreeTab(String(idx))}
                      style={{
                        padding: '0.375rem 0.75rem',
                        fontSize: '0.8125rem',
                        fontWeight: 600,
                        borderRadius: 'var(--radius-sm)',
                        background: isSelected ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                        color: isSelected ? '#10b981' : '#94a3b8',
                        border: isSelected ? '1px solid #10b981' : '1px solid rgba(255, 255, 255, 0.1)',
                        cursor: 'pointer',
                      }}
                    >
                      Tree #{idx + 1} (Root: {rootName}, {t.nodeCount} nodes)
                    </button>
                  );
                })}
              </div>
            )}

            {/* Edge Table */}
            {displayedEdges.length === 0 ? (
              <div
                style={{
                  textAlign: 'center',
                  padding: '2.5rem 1rem',
                  color: '#94a3b8',
                  background: 'rgba(255, 255, 255, 0.02)',
                  borderRadius: '8px',
                  border: '1px solid var(--color-border)',
                }}
              >
                ⚠️ No connected routes found in the graph for the selected criteria.
              </div>
            ) : (
              <div className="na-table-wrapper">
                <table className="na-table">
                  <thead>
                    <tr>
                      <th style={{ width: 48 }}>#</th>
                      <th>From Destination</th>
                      <th>To Destination</th>
                      <th>{selectedWeight === 'travel_time_minutes' ? 'Travel Time' : selectedWeight === 'estimated_cost_lkr' ? 'Cost' : 'Distance'}</th>
                      <th className="na-table__bar-cell">Weight Distribution</th>
                    </tr>
                  </thead>
                  <tbody>
                    {displayedEdges.map((edge, index) => {
                      const fromId = edge.fromLocationId || edge.fromNodeId || edge.sourceNodeId || edge.fromId;
                      const toId = edge.toLocationId || edge.toNodeId || edge.targetNodeId || edge.toId;
                      const weight = edge.weight ?? edge.cost ?? 0;

                      const fromName = nodeNameMap[fromId] || edge.fromNodeName || fromId;
                      const toName = nodeNameMap[toId] || edge.toNodeName || toId;

                      const barPct = maxEdgeWeight > 0 ? (weight / maxEdgeWeight) * 100 : 0;

                      return (
                        <tr key={`${fromId}-${toId}-${index}`}>
                          <td className="na-table__rank">{index + 1}</td>
                          <td className="na-table__name">
                            {fromName}{' '}
                            <span style={{ color: '#64748b', fontSize: '0.75rem', fontFamily: 'monospace' }}>
                              ({fromId})
                            </span>
                          </td>
                          <td className="na-table__name">
                            {toName}{' '}
                            <span style={{ color: '#64748b', fontSize: '0.75rem', fontFamily: 'monospace' }}>
                              ({toId})
                            </span>
                          </td>
                          <td className="na-table__score" style={{ color: '#10b981', fontWeight: 600 }}>
                            {weight.toFixed(2)}{' '}
                            <span style={{ fontSize: '0.75rem', color: '#94a3b8', fontWeight: 400 }}>
                              {weightUnit}
                            </span>
                          </td>
                          <td className="na-table__bar-cell">
                            <div className="na-table__bar-container">
                              <div
                                className="na-table__bar"
                                style={{
                                  width: `${barPct}%`,
                                  background: 'linear-gradient(90deg, #059669, #10b981)',
                                }}
                              />
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
    </section>
  );
}