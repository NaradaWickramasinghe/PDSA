import { useState, useMemo } from 'react';
import { MapContainer, TileLayer, CircleMarker, Tooltip } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import './NetworkMap.css';

// Center map roughly on Sri Lanka
const CENTER = [7.8731, 80.7718];
const ZOOM = 7;

export default function NetworkMap({ data, metric }) {
  if (!data || data.length === 0) return <div className="na-map-empty">No data to display</div>;

  // Find max value to scale the markers correctly
  const maxVal = useMemo(() => Math.max(...data.map(d => d[metric])), [data, metric]);

  const getMarkerColor = (metricType) => {
    return metricType === 'betweenness' ? '#06b6d4' : '#8b5cf6';
  };

  const getMarkerFill = (metricType) => {
    return metricType === 'betweenness' ? '#22d3ee' : '#a78bfa';
  };

  return (
    <div className="na-map-wrapper">
      <MapContainer
        center={CENTER}
        zoom={ZOOM}
        scrollWheelZoom={false}
        className="na-leaflet-container"
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          className="na-dark-tiles"
        />
        {data.map((item) => {
          if (!item.latitude || !item.longitude) return null;

          // Scale radius from 5 to 25 based on the score
          const ratio = maxVal > 0 ? item[metric] / maxVal : 0;
          const radius = 5 + ratio * 20;

          return (
            <CircleMarker
              key={item.nodeId}
              center={[item.latitude, item.longitude]}
              radius={radius}
              pathOptions={{
                color: getMarkerColor(metric),
                fillColor: getMarkerFill(metric),
                fillOpacity: 0.6,
                weight: 2,
              }}
            >
              <Tooltip className="na-map-tooltip">
                <div className="na-map-tooltip-title">{item.name}</div>
                <div className="na-map-tooltip-score">
                  <span style={{ textTransform: 'capitalize' }}>{metric}</span>:{' '}
                  {item[metric].toFixed(6)}
                </div>
              </Tooltip>
            </CircleMarker>
          );
        })}
      </MapContainer>
    </div>
  );
}
