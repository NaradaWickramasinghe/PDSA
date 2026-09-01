import { useState, useEffect } from 'react';
import './NetworkAnalysis.css';

export default function NetworkMstPage({ allNodes: initialNodes = [] }) {
  const [allNodes, setAllNodes] = useState(initialNodes);
  const [startNode, setStartNode] = useState('');
  const [mstResult, setMstResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Auto-fetch nodes from backend if not provided as props
  useEffect(() => {
    if (initialNodes.length > 0) {
      setAllNodes(initialNodes);
      return;
    }

    const fetchNodes = async () => {
      try {
        const res = await fetch('http://localhost:8080/api/network/nodes');
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
        const json = await res.json();
        
        // Handle common backend API wrapper schemas ({ data: [...] } or direct array)
        const nodesList = json.data || json.nodes || (Array.isArray(json) ? json : []);
        setAllNodes(nodesList);
      } catch (err) {
        console.error('Failed to auto-load nodes:', err);
      }
    };

    fetchNodes();
  }, [initialNodes]);

  const handleCompute = async (e) => {
    e.preventDefault();
    if (!startNode) return;
    setLoading(true);
    setError(null);

    try {
      const res = await fetch(`http://localhost:8080/api/network/mst?startNodeId=${startNode}`);
      const json = await res.json();
      
      if (!res.ok || (json.success !== undefined && !json.success)) {
        throw new Error(json.message || `Error: ${res.status}`);
      }
      
      // Fallback for response payloads with or without a `.data` wrapper
      setMstResult(json.data || json);
    } catch (err) {
      setError(err.message || 'Failed to compute MST');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="na-page">
      <div className="na-container">
        <header className="na-header">
          <h1 className="na-header__title">Minimum Spanning Tree</h1>
          <p className="na-header__subtitle">
            Calculate the optimal path connecting network locations with Prim's algorithm.
          </p>
        </header>

        <form onSubmit={handleCompute} className="na-controls">
          <label htmlFor="mst-start-select" className="na-controls__label">
            Start Node:
          </label>
          <select
            id="mst-start-select"
            className="na-controls__select"
            value={startNode}
            onChange={(e) => setStartNode(e.target.value)}
          >
            <option value="">Select Starting Destination...</option>
            {allNodes.map((n) => {
              const id = n.nodeId || n.id;
              const name = n.name || n.label || id;
              return (
                <option key={id} value={id}>
                  {name} ({id})
                </option>
              );
            })}
          </select>
          <button 
            type="submit" 
            className="na-controls__btn" 
            disabled={loading || !startNode}
          >
            {loading ? 'Computing...' : 'Compute MST'}
          </button>
        </form>

        {error && <div className="na-error">{error}</div>}

        {mstResult && (
          <div className="na-card" style={{ marginTop: '1.5rem' }}>
            <h3 style={{ marginBottom: '1rem', color: 'var(--color-text-primary)' }}>
              MST Total Weight: {mstResult.totalCost ?? mstResult.totalWeight ?? 'N/A'}
            </h3>
            <div className="na-table-wrapper">
              <table className="na-table">
                <thead>
                  <tr>
                    <th>Source</th>
                    <th>Destination</th>
                    <th>Weight</th>
                  </tr>
                </thead>
                <tbody>
                  {(mstResult.edges || mstResult.mstEdges || []).map((edge, idx) => (
                    <tr key={`${edge.fromNodeId || edge.source}-${edge.toNodeId || edge.target}-${idx}`}>
                      <td className="na-table__name">
                        {edge.fromNodeName || edge.sourceName || edge.fromNodeId || edge.source}
                      </td>
                      <td className="na-table__name">
                        {edge.toNodeName || edge.targetName || edge.toNodeId || edge.target}
                      </td>
                      <td className="na-table__score">
                        {edge.weight ?? edge.cost}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}