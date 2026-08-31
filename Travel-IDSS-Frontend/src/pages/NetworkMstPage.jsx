// src/pages/NetworkMstPage.jsx
import React, { useEffect } from 'react';
import { useNetworkAnalysis, WEIGHT_OPTIONS } from '../hooks/useNetworkAnalysis';

export const NetworkMstPage = () => {
  const {
    mstResult,
    selectedWeight,
    mstLoading,
    error,
    fetchMstAnalysis,
    setSelectedWeight,
  } = useNetworkAnalysis();

  // Load Prim's MST data when component mounts or weight changes
  useEffect(() => {
    fetchMstAnalysis(selectedWeight);
  }, [fetchMstAnalysis, selectedWeight]);

  const handleWeightChange = (e) => {
    const newWeight = e.target.value;
    setSelectedWeight(newWeight);
    fetchMstAnalysis(newWeight);
  };

  return (
    <div className="page">
      <div className="page__container" style={{ paddingTop: '2rem', paddingBottom: '3rem' }}>
        
        {/* Header Section */}
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '2rem', fontWeight: 700, letterSpacing: '-0.02em' }}>
            Minimum Spanning Tree Analysis
          </h1>
          <p style={{ color: 'var(--color-text-secondary, #666)', marginTop: '0.5rem' }}>
            Powered by Prim's Algorithm — computes the minimum weight tree or forest across all interconnected travel destinations.
          </p>
        </div>

        {/* Controls / Filter Section */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '1rem',
          backgroundColor: '#fff',
          padding: '1rem 1.5rem',
          borderRadius: '8px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
          marginBottom: '2rem'
        }}>
          <label style={{ fontWeight: 600, fontSize: '0.95rem' }}>Optimization Metric:</label>
          <select
            value={selectedWeight}
            onChange={handleWeightChange}
            disabled={mstLoading}
            style={{
              padding: '0.5rem 1rem',
              borderRadius: '6px',
              border: '1px solid #ccc',
              backgroundColor: '#fff',
              fontSize: '0.95rem',
              cursor: 'pointer'
            }}
          >
            {WEIGHT_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        {/* Loading Indicator */}
        {mstLoading && (
          <div style={{ padding: '2rem', textAlign: 'center', color: '#666' }}>
            Computing Minimum Spanning Forest...
          </div>
        )}

        {/* Error Alert */}
        {error && !mstLoading && (
          <div style={{
            padding: '1rem',
            backgroundColor: '#fee2e2',
            color: '#dc2626',
            borderRadius: '6px',
            marginBottom: '1.5rem'
          }}>
            {error}
          </div>
        )}

        {/* Main Data View */}
        {!mstLoading && mstResult && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            
            {/* Summary Metrics Cards */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: '1rem'
            }}>
              <div style={cardStyle}>
                <span style={cardLabelStyle}>Total Destinations</span>
                <p style={cardValueStyle}>{mstResult.totalNodeCount}</p>
              </div>

              <div style={cardStyle}>
                <span style={cardLabelStyle}>Total Forest Weight</span>
                <p style={cardValueStyle}>{mstResult.totalForestWeight?.toLocaleString()}</p>
              </div>

              <div style={cardStyle}>
                <span style={cardLabelStyle}>Sub-Trees / Components</span>
                <p style={cardValueStyle}>{mstResult.totalTreeCount}</p>
              </div>

              <div style={cardStyle}>
                <span style={cardLabelStyle}>Execution Speed</span>
                <p style={cardValueStyle}>{mstResult.computationTimeMs} ms</p>
              </div>
            </div>

            {/* Connectivity Status Banner */}
            <div style={{
              padding: '1rem 1.25rem',
              borderRadius: '8px',
              backgroundColor: mstResult.graphWasFullyConnected ? '#ecfdf5' : '#fffbe6',
              color: mstResult.graphWasFullyConnected ? '#065f46' : '#92400e',
              border: `1px solid ${mstResult.graphWasFullyConnected ? '#a7f3d0' : '#fef08a'}`,
              fontWeight: 500
            }}>
              <strong>Network Connectivity Status: </strong>
              {mstResult.graphWasFullyConnected
                ? 'Fully connected graph — 1 unified Minimum Spanning Tree generated.'
                : `Disconnected components detected — ${mstResult.totalTreeCount} distinct Minimum Spanning Trees (Spanning Forest) created.`}
            </div>

            {/* Individual Sub-Tree Breakdown */}
            {mstResult.trees?.map((tree, index) => (
              <div key={index} style={{
                backgroundColor: '#fff',
                border: '1px solid #e5e7eb',
                borderRadius: '8px',
                padding: '1.5rem',
                boxShadow: '0 1px 3px rgba(0,0,0,0.05)'
              }}>
                <div style={{
                  display: 'flex',
                  justify: 'space-between',
                  alignItems: 'center',
                  borderBottom: '1px solid #f3f4f6',
                  paddingBottom: '1rem',
                  marginBottom: '1rem'
                }}>
                  <h3 style={{ fontSize: '1.15rem', fontWeight: 600, margin: 0 }}>
                    Tree Component #{index + 1}
                    <span style={{ fontSize: '0.9rem', color: '#6b7280', marginLeft: '0.5rem', fontWeight: 400 }}>
                      (Root: {tree.startedFromLocationId})
                    </span>
                  </h3>
                  <span style={{ fontSize: '0.9rem', fontWeight: 600, color: '#4f46e5' }}>
                    Tree Weight: {tree.totalWeight?.toLocaleString()} | Nodes: {tree.nodeCount}
                  </span>
                </div>

                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead>
                      <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                        <th style={thStyle}>From Location ID</th>
                        <th style={thStyle}>To Location ID</th>
                        <th style={thStyle}>Edge Weight ({selectedWeight})</th>
                      </tr>
                    </thead>
                    <tbody>
                      {tree.edges?.map((edge, edgeIdx) => (
                        <tr key={edgeIdx} style={{ borderBottom: '1px solid #f3f4f6' }}>
                          <td style={tdStyle}>{edge.fromLocationId}</td>
                          <td style={tdStyle}>{edge.toLocationId}</td>
                          <td style={tdStyle}>{edge.weight?.toLocaleString()}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            ))}

          </div>
        )}
      </div>
    </div>
  );
};

// Component inline style definitions
const cardStyle = {
  backgroundColor: '#fff',
  padding: '1.25rem',
  borderRadius: '8px',
  border: '1px solid #e5e7eb',
  boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
};

const cardLabelStyle = {
  fontSize: '0.75rem',
  color: '#6b7280',
  textTransform: 'uppercase',
  fontWeight: 600,
  letterSpacing: '0.05em',
};

const cardValueStyle = {
  fontSize: '1.5rem',
  fontWeight: 700,
  margin: '0.25rem 0 0 0',
  color: '#111827',
};

const thStyle = {
  padding: '0.75rem 1rem',
  fontSize: '0.85rem',
  fontWeight: 600,
  color: '#374151',
};

const tdStyle = {
  padding: '0.75rem 1rem',
  fontSize: '0.9rem',
  color: '#1f2937',
};

export default NetworkMstPage;