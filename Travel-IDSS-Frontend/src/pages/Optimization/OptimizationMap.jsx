import React, { useMemo } from 'react';
import { MapContainer, TileLayer, CircleMarker, Polyline, Tooltip } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

// Center map roughly on Sri Lanka
const CENTER = [7.8731, 80.7718];
const ZOOM = 7;

export default function OptimizationMap({ nodes, edges, activeRoutePath }) {
  
  // Create a lookup for nodes by ID to get lat/long for edges
  const nodeMap = useMemo(() => {
    const map = {};
    nodes.forEach(n => {
      map[n.nodeId] = n;
    });
    return map;
  }, [nodes]);

  // Determine if an edge is part of the active route
  const isEdgeInActiveRoute = (source, target) => {
    if (!activeRoutePath || activeRoutePath.length < 2) return false;
    for (let i = 0; i < activeRoutePath.length - 1; i++) {
      if ((activeRoutePath[i] === source && activeRoutePath[i+1] === target) ||
          (activeRoutePath[i] === target && activeRoutePath[i+1] === source)) {
        return true;
      }
    }
    return false;
  };

  const activeSrc = activeRoutePath && activeRoutePath.length > 0 ? activeRoutePath[0] : null;
  const activeDst = activeRoutePath && activeRoutePath.length > 1 ? activeRoutePath[activeRoutePath.length - 1] : null;

  return (
    <div className="opt-map-container">
      <MapContainer
        center={CENTER}
        zoom={ZOOM}
        scrollWheelZoom={true}
        className="opt-leaflet-map"
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; OpenStreetMap contributors'
          className="opt-map-dark-tiles"
        />

        {/* Draw Edges */}
        {edges.map((e, idx) => {
          const n1 = nodeMap[e.source];
          const n2 = nodeMap[e.destination];
          if (!n1 || !n2) return null;

          const isActive = isEdgeInActiveRoute(e.source, e.destination);

          return (
            <Polyline
              key={`edge-${idx}`}
              positions={[
                [n1.latitude, n1.longitude],
                [n2.latitude, n2.longitude]
              ]}
              pathOptions={{
                color: isActive ? '#06b6d4' : '#475569',
                weight: isActive ? 4 : 1,
                opacity: isActive ? 1 : 0.4
              }}
            />
          );
        })}

        {/* Draw Nodes */}
        {nodes.map(n => {
          const isOrigin = n.nodeId === activeSrc;
          const isDest = n.nodeId === activeDst;
          const isPath = activeRoutePath?.includes(n.nodeId);
          
          let fillColor = '#8b5cf6'; // Default purple
          let color = '#a78bfa';
          let radius = 5;
          let fillOpacity = 0.6;

          if (isOrigin) {
            fillColor = '#10b981'; // Green
            color = '#34d399';
            radius = 8;
            fillOpacity = 1;
          } else if (isDest) {
            fillColor = '#ef4444'; // Red
            color = '#f87171';
            radius = 8;
            fillOpacity = 1;
          } else if (isPath) {
            fillColor = '#06b6d4'; // Cyan
            color = '#22d3ee';
            radius = 6;
            fillOpacity = 0.9;
          }

          return (
            <CircleMarker
              key={n.nodeId}
              center={[n.latitude, n.longitude]}
              radius={radius}
              pathOptions={{
                color: color,
                fillColor: fillColor,
                fillOpacity: fillOpacity,
                weight: 2
              }}
            >
              <Tooltip className="na-map-tooltip">
                <div style={{ fontWeight: 700, marginBottom: '2px', color: 'var(--color-primary)' }}>
                  {n.name} ({n.nodeId})
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)' }}>
                  📍 {n.district}, {n.province} Province
                </div>
              </Tooltip>
            </CircleMarker>
          );
        })}
      </MapContainer>
    </div>
  );
}
