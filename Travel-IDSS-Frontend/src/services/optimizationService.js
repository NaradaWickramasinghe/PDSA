// src/services/optimizationService.js
// API service for Optimization endpoints

import api from './api';

const OPTIMIZATION_BASE = '/optimization';

export const optimizationService = {
  // Run optimization
  runOptimization: (optimizationData) => {
    return api.post(`${OPTIMIZATION_BASE}/run`, optimizationData);
  },

  // Get available algorithms
  getAlgorithms: () => {
    return api.get(`${OPTIMIZATION_BASE}/algorithms`);
  },

  // Get optimization result
  getResult: (id) => {
    return api.get(`${OPTIMIZATION_BASE}/results/${id}`);
  },

  // Get optimization history
  getHistory: () => {
    return api.get(`${OPTIMIZATION_BASE}/history`);
  },

  // Compare optimization algorithms
  compareAlgorithms: (data) => {
    return api.post(`${OPTIMIZATION_BASE}/compare`, data);
  },

  // Get performance benchmarks
  getBenchmarks: () => {
    return api.get(`${OPTIMIZATION_BASE}/benchmarks`);
  },
};

export default optimizationService;
