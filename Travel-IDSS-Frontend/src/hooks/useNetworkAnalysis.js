// src/hooks/useNetworkAnalysis.js
// Custom hook for Network Analysis logic

import { useState, useCallback } from 'react';
import networkService from '../services/networkService';

/**
 * Weight options that map to the backend's edge weight types.
 */
export const WEIGHT_OPTIONS = [
  { value: 'distance_km', label: 'Distance (km)' },
  { value: 'travel_time_minutes', label: 'Travel Time (min)' },
  { value: 'estimated_cost_lkr', label: 'Estimated Cost (LKR)' },
];

export const useNetworkAnalysis = () => {
  const [analysisResult, setAnalysisResult] = useState(null);
  const [mstResult, setMstResult] = useState(null);
  const [locationScore, setLocationScore] = useState(null);
  const [selectedWeight, setSelectedWeight] = useState('distance_km');
  const [loading, setLoading] = useState(false);
  const [mstLoading, setMstLoading] = useState(false);
  const [locationLoading, setLocationLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * Fetch full centrality analysis (both rankings + metadata).
   */
  const fetchFullAnalysis = useCallback(async (weight = 'distance_km') => {
    setLoading(true);
    setError(null);
    try {
      const response = await networkService.getFullAnalysis(weight);
      setAnalysisResult(response.data);
      setSelectedWeight(weight);
      return response.data;
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to analyze network';
      setError(msg);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Fetch Minimum Spanning Tree / Forest using Prim's algorithm.
   */
  const fetchMstAnalysis = useCallback(async (weight = 'distance_km') => {
    setMstLoading(true);
    setError(null);
    try {
      const response = await networkService.getMstPrimAnalysis(weight);
      setMstResult(response.data);
      setSelectedWeight(weight);
      return response.data;
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to compute MST analysis';
      setError(msg);
      throw err;
    } finally {
      setMstLoading(false);
    }
  }, []);

  /**
   * Fetch centrality scores for a single destination.
   */
  const fetchLocationScore = useCallback(async (nodeId, weight = 'distance_km') => {
    setLocationLoading(true);
    setError(null);
    try {
      const response = await networkService.getLocationScore(nodeId, weight);
      setLocationScore(response.data);
      return response.data;
    } catch (err) {
      const msg = err.response?.data?.message || 'Location not found';
      setError(msg);
      setLocationScore(null);
      throw err;
    } finally {
      setLocationLoading(false);
    }
  }, []);

  const clearError = useCallback(() => setError(null), []);
  const clearLocationScore = useCallback(() => setLocationScore(null), []);
  const clearMstResult = useCallback(() => setMstResult(null), []);

  return {
    // State
    analysisResult,
    mstResult,
    locationScore,
    selectedWeight,
    loading,
    mstLoading,
    locationLoading,
    error,
    // Actions
    fetchFullAnalysis,
    fetchMstAnalysis,
    fetchLocationScore,
    setSelectedWeight,
    clearError,
    clearLocationScore,
    clearMstResult,
  };
};

export default useNetworkAnalysis;