// src/services/routeService.js
// API service for Route Optimization endpoints

import api from './api';

const ROUTE_BASE = '/routes';

export const routeService = {
  // Find optimal route
  findOptimalRoute: (routeData) => {
    return api.post(`${ROUTE_BASE}/optimize`, routeData);
  },

  // Get all saved routes
  getAllRoutes: () => {
    return api.get(ROUTE_BASE);
  },

  // Get route by ID
  getRouteById: (id) => {
    return api.get(`${ROUTE_BASE}/${id}`);
  },

  // Compare routes using different algorithms
  compareAlgorithms: (routeData) => {
    return api.post(`${ROUTE_BASE}/compare`, routeData);
  },

  // Get route suggestions
  getSuggestions: (params) => {
    return api.get(`${ROUTE_BASE}/suggestions`, { params });
  },
};

export default routeService;
