// src/pages/RouteOptimization/components/RouteMetrics.jsx

export default function RouteMetrics({ result }) {
  if (!result) return null;

  const {
    totalDistanceKm = 0,
    estimatedTimeMinutes = 0,
    riskScore = 0,
    trafficScore = 0,
    congestedSegments = 0,
    algorithmUsed = '',
    executionTimeMs = 0,
    path = [],
    orderedLocations = [],
  } = result;

  // Format time as hours and minutes
  const hours = Math.floor(estimatedTimeMinutes / 60);
  const mins = Math.round(estimatedTimeMinutes % 60);
  const timeStr = hours > 0 ? `${hours}h ${mins}m` : `${mins} min`;

  // Risk label
  const riskLabel = riskScore <= 2 ? 'Low' : riskScore <= 3.5 ? 'Moderate' : 'High';
  const riskColor = riskScore <= 2 ? '#10b981' : riskScore <= 3.5 ? '#f59e0b' : '#ef4444';

  // Traffic label
  const trafficLabel =
    congestedSegments === 0
      ? '🟢 Clear'
      : congestedSegments <= 2
      ? '🟡 Moderate'
      : congestedSegments <= 4
      ? '🟠 Heavy'
      : '🔴 Severe';

  return (
    <div className="glass-panel" style={{ animation: 'slideUp 0.4s ease' }}>
      <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        📊 Route Results
      </h3>

      <div className="metrics-grid">
        {/* Total Distance */}
        <div className="metric-item">
          <span className="metric-label">Distance</span>
          <span className="metric-value highlight">
            {totalDistanceKm.toFixed(1)} km
          </span>
        </div>

        {/* Estimated Time */}
        <div className="metric-item">
          <span className="metric-label">Est. Time</span>
          <span className="metric-value highlight">{timeStr}</span>
        </div>

        {/* Risk Score */}
        <div className="metric-item">
          <span className="metric-label">Risk Level</span>
          <span className="metric-value" style={{ color: riskColor }}>
            {riskScore.toFixed(1)} ({riskLabel})
          </span>
        </div>

        {/* Traffic */}
        <div className="metric-item">
          <span className="metric-label">Traffic</span>
          <span className="metric-value">{trafficLabel}</span>
        </div>
      </div>

      {/* Route stops */}
      {path.length > 0 && (
        <div style={{ marginTop: '1rem' }}>
          <div style={{ fontSize: '0.85rem', color: '#94a3b8', marginBottom: '0.5rem' }}>
            Route path ({path.length} points)
          </div>
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '4px',
              maxHeight: '120px',
              overflowY: 'auto',
              paddingRight: '4px',
            }}
          >
            {path.map((loc, i) => (
              <div
                key={`${loc.id}-${i}`}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  fontSize: '0.85rem',
                }}
              >
                <span
                  style={{
                    width: '20px',
                    height: '20px',
                    borderRadius: '50%',
                    background:
                      i === 0
                        ? '#10b981'
                        : i === path.length - 1
                        ? '#ef4444'
                        : '#3b82f6',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '0.65rem',
                    fontWeight: 700,
                    color: 'white',
                    flexShrink: 0,
                  }}
                >
                  {i === 0 ? 'A' : i === path.length - 1 ? 'B' : i}
                </span>
                <span style={{ color: '#e2e8f0' }}>{loc.name}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Ordered locations for multi-stop */}
      {orderedLocations && orderedLocations.length > 1 && (
        <div style={{ marginTop: '1rem' }}>
          <div
            style={{
              fontSize: '0.85rem',
              color: '#94a3b8',
              marginBottom: '0.5rem',
            }}
          >
            ✨ Optimized visit order
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
            {orderedLocations.map((loc, i) => (
              <span
                key={loc.id}
                style={{
                  background: 'rgba(56, 189, 248, 0.15)',
                  border: '1px solid rgba(56, 189, 248, 0.3)',
                  borderRadius: '6px',
                  padding: '4px 10px',
                  fontSize: '0.8rem',
                  color: '#e2e8f0',
                }}
              >
                {i + 1}. {loc.name}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Meta info */}
      <div
        style={{
          marginTop: '1rem',
          display: 'flex',
          justifyContent: 'space-between',
          fontSize: '0.75rem',
          color: '#64748b',
        }}
      >
        <span>Algorithm: {algorithmUsed}</span>
        {executionTimeMs > 0 && <span>{executionTimeMs.toFixed(1)} ms</span>}
      </div>

      <style>{`
        @keyframes slideUp {
          from {
            opacity: 0;
            transform: translateY(16px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
      `}</style>
    </div>
  );
}
