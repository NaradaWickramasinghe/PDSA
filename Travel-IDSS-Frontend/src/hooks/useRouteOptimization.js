// src/hooks/useRouteOptimization.js
// Custom hook for Route Optimization logic

import { useState, useCallback } from 'react';
import routeService from '../services/routeService';

export const useRouteOptimization = () => {
  const [routes, setRoutes] = useState([]);
  const [optimalRoute, setOptimalRoute] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [comparisonResults, setComparisonResults] = useState(null);

  const findOptimalRoute = useCallback(async (routeData) => {
    setLoading(true);
    setError(null);
    try {
      const response = await routeService.findOptimalRoute(routeData);
      setOptimalRoute(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to find optimal route');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const compareAlgorithms = useCallback(async (routeData) => {
    setLoading(true);
    setError(null);
    try {
      const response = await routeService.compareAlgorithms(routeData);
      setComparisonResults(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to compare algorithms');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchAllRoutes = useCallback(async () => {
    setLoading(true);
    try {
      const response = await routeService.getAllRoutes();
      setRoutes(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch routes');
    } finally {
      setLoading(false);
    }
  }, []);

  const clearError = useCallback(() => setError(null), []);

  return {
    routes,
    optimalRoute,
    loading,
    error,
    comparisonResults,
    findOptimalRoute,
    compareAlgorithms,
    fetchAllRoutes,
    clearError,
  };
};

export default useRouteOptimization;
