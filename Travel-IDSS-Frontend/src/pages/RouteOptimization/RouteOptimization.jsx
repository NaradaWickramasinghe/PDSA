// src/pages/RouteOptimization/RouteOptimization.jsx
import { useState, useCallback, useEffect } from 'react';
import LocationSearchInput from './components/LocationSearchInput';
import RouteMap from './components/RouteMap';
import RouteMetrics from './components/RouteMetrics';
import routeService from '../../services/routeService';
import './RouteOptimization.css';

const TRANSPORT_MODES = [
  { value: 'NORMAL_VEHICLE', label: '🚗 Car', short: 'Car' },
  { value: 'BUS', label: '🚌 Bus', short: 'Bus' },
  { value: 'BIKE', label: '🏍️ Bike', short: 'Bike' },
  { value: 'BICYCLE', label: '🚲 Bicycle', short: 'Bicycle' },
];

export default function RouteOptimization() {
  // Mode: single or multi
  const [routeMode, setRouteMode] = useState('single'); // 'single' | 'multi'

  // Single route locations
  const [startLocation, setStartLocation] = useState(null);
  const [endLocation, setEndLocation] = useState(null);

  // Multi-stop destinations
  const [destinations, setDestinations] = useState([]);
  const [tempDestination, setTempDestination] = useState(null);

  // Options
  const [transportMode, setTransportMode] = useState('NORMAL_VEHICLE');
  const [prioritizeTime, setPrioritizeTime] = useState(false);
  const [preferSafeRoute, setPreferSafeRoute] = useState(false);
  const [maxRiskLevel, setMaxRiskLevel] = useState(5);

  // Results & state
  const [routeResult, setRouteResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Traffic info
  const [trafficStatus, setTrafficStatus] = useState(null);
  
  // Live Traffic toggle and data
  const [showLiveTraffic, setShowLiveTraffic] = useState(false);
  const [liveTrafficData, setLiveTrafficData] = useState([]);

  const handleCalculateRoute = useCallback(async () => {
    setError(null);
    setRouteResult(null);

    if (routeMode === 'single') {
      if (!startLocation || !endLocation) {
        setError('Please select both start and end locations.');
        return;
      }

      setLoading(true);
      try {
        const res = await routeService.calculateRoute({
          startLocationId: startLocation.id,
          endLocationId: endLocation.id,
          transportMode,
          prioritizeTime,
          preferSafeRoute,
          maxRiskLevel: preferSafeRoute ? maxRiskLevel : null,
        });
        setRouteResult(res.data);
      } catch (err) {
        setError(
          err.response?.data?.message ||
            err.response?.data ||
            'Failed to calculate route. Please try again.'
        );
      } finally {
        setLoading(false);
      }
    } else {
      // Multi-stop
      if (!startLocation) {
        setError('Please select a start location.');
        return;
      }
      if (destinations.length < 1) {
        setError('Please add at least 1 destination.');
        return;
      }

      setLoading(true);
      try {
        const res = await routeService.calculateMultiStopRoute({
          startLocationId: startLocation.id,
          destinationIds: destinations.map((d) => d.id),
          transportMode,
          prioritizeTime,
          preferSafeRoute,
          maxRiskLevel: preferSafeRoute ? maxRiskLevel : null,
        });
        setRouteResult(res.data);
      } catch (err) {
        setError(
          err.response?.data?.message ||
            err.response?.data ||
            'Failed to calculate multi-stop route.'
        );
      } finally {
        setLoading(false);
      }
    }
  }, [
    routeMode,
    startLocation,
    endLocation,
    destinations,
    transportMode,
    prioritizeTime,
    preferSafeRoute,
    maxRiskLevel,
  ]);

  const handleSimulateTraffic = async () => {
    try {
      const res = await routeService.simulateTraffic();
      setTrafficStatus(res.data);
      if (showLiveTraffic) {
        fetchLiveTraffic(); // Refresh live traffic data immediately after simulation
      }
    } catch {
      // Silently ignore traffic simulation errors
    }
  };

  const fetchLiveTraffic = async () => {
    try {
      const res = await routeService.getAllTraffic();
      setLiveTrafficData(res.data || []);
    } catch (err) {
      console.error("Failed to fetch live traffic:", err);
    }
  };

  useEffect(() => {
    let intervalId;
    if (showLiveTraffic) {
      fetchLiveTraffic();
      intervalId = setInterval(fetchLiveTraffic, 30000); // Poll every 30s
    } else {
      setLiveTrafficData([]);
    }
    return () => {
      if (intervalId) clearInterval(intervalId);
    };
  }, [showLiveTraffic]);

  const addDestination = () => {
    if (tempDestination && !destinations.find((d) => d.id === tempDestination.id)) {
      setDestinations([...destinations, tempDestination]);
      setTempDestination(null);
    }
  };

  const removeDestination = (id) => {
    setDestinations(destinations.filter((d) => d.id !== id));
  };

  return (
    <div className="route-opt-page">
      {/* ─── Left Sidebar ─── */}
      <aside className="route-opt-sidebar">
        <div>
          <h1>Route Optimization</h1>
          <p className="subtitle">A* Algorithm · Traffic-Aware Pathfinding</p>
        </div>

        {/* Mode Toggle */}
        <div className="segmented-control">
          <button
            className={routeMode === 'single' ? 'active' : ''}
            onClick={() => setRouteMode('single')}
          >
            A → B
          </button>
          <button
            className={routeMode === 'multi' ? 'active' : ''}
            onClick={() => setRouteMode('multi')}
          >
            Multi-Stop
          </button>
        </div>

        {/* Location Inputs */}
        <div className="glass-panel">
          <h3>{routeMode === 'single' ? '📍 Locations' : '📍 Multi-Stop Route'}</h3>

          <LocationSearchInput
            label="Start Location"
            placeholder="Search start point..."
            value={startLocation}
            onSelect={setStartLocation}
            onClear={() => setStartLocation(null)}
          />

          {routeMode === 'single' ? (
            <LocationSearchInput
              label="End Location"
              placeholder="Search destination..."
              value={endLocation}
              onSelect={setEndLocation}
              onClear={() => setEndLocation(null)}
            />
          ) : (
            <>
              {/* Add destination input */}
              <LocationSearchInput
                label="Add Destination"
                placeholder="Search a stop..."
                value={tempDestination}
                onSelect={setTempDestination}
                onClear={() => setTempDestination(null)}
              />
              <button
                className="btn-secondary"
                onClick={addDestination}
                disabled={!tempDestination}
                style={{ marginBottom: '0.75rem' }}
              >
                + Add Stop
              </button>

              {/* List of destinations */}
              {destinations.length > 0 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  {destinations.map((d, i) => (
                    <div
                      key={d.id}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        background: 'rgba(15, 23, 42, 0.5)',
                        borderRadius: '8px',
                        padding: '0.5rem 0.75rem',
                        fontSize: '0.9rem',
                        border: '1px solid rgba(255,255,255,0.05)',
                      }}
                    >
                      <span style={{ color: '#e2e8f0' }}>
                        <span
                          style={{
                            color: '#38bdf8',
                            fontWeight: 600,
                            marginRight: '0.5rem',
                          }}
                        >
                          {i + 1}.
                        </span>
                        {d.name}
                      </span>
                      <button
                        onClick={() => removeDestination(d.id)}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: '#ef4444',
                          cursor: 'pointer',
                          fontSize: '1rem',
                        }}
                        title="Remove stop"
                      >
                        ✕
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </div>

        {/* Transport Mode */}
        <div className="glass-panel">
          <h3>🚗 Transport Mode</h3>
          <div className="segmented-control">
            {TRANSPORT_MODES.map((m) => (
              <button
                key={m.value}
                className={transportMode === m.value ? 'active' : ''}
                onClick={() => setTransportMode(m.value)}
                title={m.label}
              >
                {m.short}
              </button>
            ))}
          </div>

          {/* Options toggles */}
          <div className="toggle-wrapper">
            <span className="toggle-label"> Prioritize Time</span>
            <label className="toggle-switch">
              <input
                type="checkbox"
                checked={prioritizeTime}
                onChange={(e) => setPrioritizeTime(e.target.checked)}
              />
              <span className="toggle-slider" />
            </label>
          </div>

          <div className="toggle-wrapper">
            <span className="toggle-label">Prefer Safe Route</span>
            <label className="toggle-switch">
              <input
                type="checkbox"
                checked={preferSafeRoute}
                onChange={(e) => setPreferSafeRoute(e.target.checked)}
              />
              <span className="toggle-slider" />
            </label>
          </div>

          {preferSafeRoute && (
            <div className="form-group">
              <label>Max Risk Level (1-5)</label>
              <input
                type="range"
                min="1"
                max="5"
                value={maxRiskLevel}
                onChange={(e) => setMaxRiskLevel(parseInt(e.target.value))}
                style={{ width: '100%', accentColor: '#3b82f6' }}
              />
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  fontSize: '0.75rem',
                  color: '#94a3b8',
                  marginTop: '4px',
                }}
              >
                <span>1 (Safest)</span>
                <span style={{ fontWeight: 600, color: '#e2e8f0' }}>{maxRiskLevel}</span>
                <span>5 (Any)</span>
              </div>
            </div>
          )}
        </div>

        <div className="glass-panel">
          <h3>🚦 Live Network Status</h3>
          <div className="toggle-wrapper" style={{ marginBottom: 0 }}>
            <span className="toggle-label">Show Live Traffic</span>
            <label className="toggle-switch">
              <input
                type="checkbox"
                checked={showLiveTraffic}
                onChange={(e) => setShowLiveTraffic(e.target.checked)}
              />
              <span className="toggle-slider" />
            </label>
          </div>
        </div>

        {/* Actions */}
        <button
          className="btn-primary"
          onClick={handleCalculateRoute}
          disabled={loading}
        >
          {loading ? (
            <>
              <span className="loading-spinner" /> Calculating...
            </>
          ) : (
            <>Calculate Route</>
          )}
        </button>

        <button className="btn-secondary" onClick={handleSimulateTraffic}>
          Simulate Traffic
        </button>

        {/* Traffic status indicator */}
        {trafficStatus && (
          <div
            className="glass-panel"
            style={{ padding: '0.75rem 1rem', fontSize: '0.85rem' }}
          >
            <div style={{ color: '#94a3b8', marginBottom: '4px' }}>Traffic Simulation</div>
            <div style={{ color: '#e2e8f0' }}>
              ✅ {trafficStatus.status || 'Applied'}
              {trafficStatus.isPeakHour !== undefined && (
                <span style={{ marginLeft: '0.5rem' }}>
                  {trafficStatus.isPeakHour ? '🔴 Peak Hour' : '🟢 Off-Peak'}
                </span>
              )}
            </div>
          </div>
        )}

        {/* Error */}
        {error && <div className="error-message">⚠️ {typeof error === 'string' ? error : JSON.stringify(error)}</div>}

        {/* Results */}
        <RouteMetrics result={routeResult} />
      </aside>

      {/* ─── Right: Map ─── */}
      <div className="route-opt-map-container">
        <RouteMap routeResult={routeResult} liveTrafficData={liveTrafficData} />
      </div>
    </div>
  );
}
