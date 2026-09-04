// src/hooks/useOptimization.js
// Custom hook for Optimization logic

import { useState, useCallback, useEffect } from 'react';
import optimizationService from '../services/optimizationService';

export const useOptimization = () => {
  const [networkData, setNetworkData] = useState({ nodes: [], edges: [], nodeCount: 0, edgeCount: 0 });
  const [optimizationResult, setOptimizationResult] = useState(null);
  const [benchmarkResult, setBenchmarkResult] = useState(null);
  const [scalabilityResults, setScalabilityResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Fetch network data on mount
  const fetchNetwork = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await optimizationService.getNetwork();
      setNetworkData(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch network topology');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchNetwork();
  }, [fetchNetwork]);

  const planRoute = useCallback(async (requestData) => {
    setLoading(true);
    setError(null);
    setOptimizationResult(null);
    try {
      const response = await optimizationService.planRoute(requestData);
      setOptimizationResult(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to plan optimal route');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const runBenchmark = useCallback(async (sourceNodeId, destinationNodeId) => {
    setLoading(true);
    setError(null);
    setBenchmarkResult(null);
    try {
      const response = await optimizationService.runBenchmark(sourceNodeId, destinationNodeId);
      setBenchmarkResult(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to run benchmark');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const clearError = useCallback(() => setError(null), []);

  const runScalabilitySuite = useCallback(async () => {
    setLoading(true);
    setError(null);
    setScalabilityResults(null);
    try {
      const response = await optimizationService.getScalabilityBenchmarks();
      setScalabilityResults(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to run scalability benchmarks');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const refreshNetwork = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await optimizationService.refreshNetwork();
      setNetworkData(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to refresh network topology');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    networkData,
    optimizationResult,
    benchmarkResult,
    scalabilityResults,
    loading,
    error,
    fetchNetwork,
    planRoute,
    runBenchmark,
    runScalabilitySuite,
    refreshNetwork,
    clearError,
  };
};

export default useOptimization;
