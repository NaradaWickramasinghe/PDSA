// src/hooks/useResourceAllocation.js
// Custom hook for Resource Allocation logic

import { useState, useCallback } from 'react';
import resourceService from '../services/resourceService';

export const useResourceAllocation = () => {
  const [resources, setResources] = useState([]);
  const [allocationResult, setAllocationResult] = useState(null);
  const [utilizationStats, setUtilizationStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const allocateResources = useCallback(async (allocationData) => {
    setLoading(true);
    setError(null);
    try {
      const response = await resourceService.allocateResources(allocationData);
      setAllocationResult(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to allocate resources');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchResources = useCallback(async () => {
    setLoading(true);
    try {
      const response = await resourceService.getAllResources();
      setResources(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch resources');
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchUtilizationStats = useCallback(async () => {
    try {
      const response = await resourceService.getUtilizationStats();
      setUtilizationStats(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch utilization stats');
    }
  }, []);

  const clearError = useCallback(() => setError(null), []);

  return {
    resources,
    allocationResult,
    utilizationStats,
    loading,
    error,
    allocateResources,
    fetchResources,
    fetchUtilizationStats,
    clearError,
  };
};

export default useResourceAllocation;
