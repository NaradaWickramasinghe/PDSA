// src/services/routeService.js
// API service for Route Optimization endpoints

import api from './api';

const ROUTE_BASE = '/routes';

export const routeService = {
  // Find optimal route (A to B)
  calculateRoute: (request) => {
    return api.post(`${ROUTE_BASE}/calculate`, request);
  },

  // Find optimal multi-stop route (TSP)
  calculateMultiStopRoute: (request) => {
    return api.post(`${ROUTE_BASE}/multi-stop`, request);
  },

  // Search locations by name
  searchLocations: (query) => {
    return api.get(`${ROUTE_BASE}/locations/search`, { params: { query } });
  },

  // Get current traffic status
  getTrafficStatus: () => {
    return api.get(`${ROUTE_BASE}/traffic/status`);
  },

  // Get traffic info for a specific location
  getLocationTraffic: (id) => {
    return api.get(`${ROUTE_BASE}/locations/traffic/${id}`);
  },

  // Trigger traffic simulation across the graph
  simulateTraffic: () => {
    return api.get(`${ROUTE_BASE}/traffic/simulate`);
  },
  
  // Debug endpoint to get sample nodes
  getDebugNodes: () => {
    return api.get(`${ROUTE_BASE}/debug/nodes`);
  }
};

export default routeService;
