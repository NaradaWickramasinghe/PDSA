// src/services/optimizationService.js
// API service for the Multi-Objective Hiking & Travel Optimization module

import api from './api';

const OPTIMIZATION_BASE = '/v1/optimization';

export const optimizationService = {
  /**
   * Fetch the network topology (nodes and edges)
   */
  getNetwork: () => {
    return api.get(`${OPTIMIZATION_BASE}/network`);
  },

  /**
   * Plan an optimal route given preferences and constraints
   * @param {Object} requestData - The OptimizationRequest containing origin, destination, weights, constraints, etc.
   */
  planRoute: (requestData) => {
    return api.post(`${OPTIMIZATION_BASE}/plan`, requestData);
  },

  /**
   * Run the empirical benchmark suite for a specific source and destination
   * @param {string} sourceNodeId 
   * @param {string} destinationNodeId 
   */
  runBenchmark: (sourceNodeId, destinationNodeId) => {
    return api.post(`${OPTIMIZATION_BASE}/benchmark`, null, {
      params: { sourceNodeId, destinationNodeId }
    });
  },

  /**
   * Run the full scalability benchmark suite
   */
  getScalabilityBenchmarks: () => {
    return api.get(`${OPTIMIZATION_BASE}/scalability-suite`);
  },

  /**
   * Refresh the network graph from the database
   */
  refreshNetwork: () => {
    return api.post(`${OPTIMIZATION_BASE}/network/refresh`);
  }
};

export default optimizationService;
