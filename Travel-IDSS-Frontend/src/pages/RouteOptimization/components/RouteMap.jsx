// src/pages/RouteOptimization/components/RouteMap.jsx
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';
import { useEffect } from 'react';
import 'leaflet/dist/leaflet.css';

// Fix default Leaflet icon issue
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

// Custom icons for start, end, and waypoints
const startIcon = new L.DivIcon({
  className: '',
  html: `<div style="
    width:32px;height:32px;border-radius:50%;
    background:linear-gradient(135deg,#10b981,#059669);
    border:3px solid white;
    box-shadow:0 2px 8px rgba(0,0,0,0.4);
    display:flex;align-items:center;justify-content:center;
    color:white;font-weight:700;font-size:14px;
  ">A</div>`,
  iconSize: [32, 32],
  iconAnchor: [16, 16],
});

const endIcon = new L.DivIcon({
  className: '',
  html: `<div style="
    width:32px;height:32px;border-radius:50%;
    background:linear-gradient(135deg,#ef4444,#dc2626);
    border:3px solid white;
    box-shadow:0 2px 8px rgba(0,0,0,0.4);
    display:flex;align-items:center;justify-content:center;
    color:white;font-weight:700;font-size:14px;
  ">B</div>`,
  iconSize: [32, 32],
  iconAnchor: [16, 16],
});

function waypointIcon(index) {
  return new L.DivIcon({
    className: '',
    html: `<div style="
      width:28px;height:28px;border-radius:50%;
      background:linear-gradient(135deg,#3b82f6,#2563eb);
      border:2px solid white;
      box-shadow:0 2px 6px rgba(0,0,0,0.3);
      display:flex;align-items:center;justify-content:center;
      color:white;font-weight:700;font-size:12px;
    ">${index}</div>`,
    iconSize: [28, 28],
    iconAnchor: [14, 14],
  });
}

// Component to fit bounds when path changes
function FitBounds({ path }) {
  const map = useMap();
  useEffect(() => {
    if (path && path.length > 0) {
      const coords = path
        .filter(loc => loc.latitude && loc.longitude)
        .map(loc => [loc.latitude, loc.longitude]);
      if (coords.length > 0) {
        const bounds = L.latLngBounds(coords);
        map.fitBounds(bounds, { padding: [50, 50], maxZoom: 14 });
      }
    }
  }, [path, map]);
  return null;
}

export default function RouteMap({ routeResult }) {
  // Center of Sri Lanka as the default
  const defaultCenter = [7.8731, 80.7718];
  const defaultZoom = 8;

  const path = routeResult?.path || [];
  const orderedLocations = routeResult?.orderedLocations || [];

  // Build polyline coordinates from path
  const polylinePositions = path
    .filter(loc => loc.latitude && loc.longitude)
    .map(loc => [loc.latitude, loc.longitude]);

  // Determine start and end from the path
  const startLoc = path.length > 0 ? path[0] : null;
  const endLoc = path.length > 1 ? path[path.length - 1] : null;

  // Multi-stop waypoints (ordered locations excluding start)
  const waypoints = orderedLocations.length > 1
    ? orderedLocations.slice(1) // Skip start (index 0)
    : [];

  return (
    <MapContainer
      center={defaultCenter}
      zoom={defaultZoom}
      style={{ width: '100%', height: '100%' }}
      scrollWheelZoom={true}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
      />

      {path.length > 0 && <FitBounds path={path} />}

      {/* Route polyline */}
      {polylinePositions.length > 1 && (
        <Polyline
          positions={polylinePositions}
          pathOptions={{
            color: '#38bdf8',
            weight: 5,
            opacity: 0.85,
            dashArray: null,
            lineCap: 'round',
            lineJoin: 'round',
          }}
        />
      )}

      {/* Glow effect line underneath */}
      {polylinePositions.length > 1 && (
        <Polyline
          positions={polylinePositions}
          pathOptions={{
            color: '#38bdf8',
            weight: 12,
            opacity: 0.2,
          }}
        />
      )}

      {/* Start marker */}
      {startLoc && startLoc.latitude && startLoc.longitude && (
        <Marker position={[startLoc.latitude, startLoc.longitude]} icon={startIcon}>
          <Popup>
            <strong>🟢 Start:</strong> {startLoc.name}
            {startLoc.type && <><br />Type: {startLoc.type}</>}
          </Popup>
        </Marker>
      )}

      {/* End marker */}
      {endLoc && endLoc.latitude && endLoc.longitude && (
        <Marker position={[endLoc.latitude, endLoc.longitude]} icon={endIcon}>
          <Popup>
            <strong>🔴 End:</strong> {endLoc.name}
            {endLoc.type && <><br />Type: {endLoc.type}</>}
          </Popup>
        </Marker>
      )}

      {/* Multi-stop waypoints */}
      {waypoints.map((loc, i) => (
        loc.latitude && loc.longitude && (
          <Marker
            key={loc.id || i}
            position={[loc.latitude, loc.longitude]}
            icon={waypointIcon(i + 1)}
          >
            <Popup>
              <strong>📍 Stop {i + 1}:</strong> {loc.name}
              {loc.type && <><br />Type: {loc.type}</>}
            </Popup>
          </Marker>
        )
      ))}
    </MapContainer>
  );
}
