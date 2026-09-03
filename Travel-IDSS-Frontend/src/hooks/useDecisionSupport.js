// src/hooks/useDecisionSupport.js
// Custom React Hook for Module 4 (Intelligent Decision Support System)

import { useState, useCallback } from 'react';
import decisionService from '../services/decisionService';

export const useDecisionSupport = () => {
  const [formData, setFormData] = useState({
    budget: 1200,
    durationDays: 5,
    groupSize: 2,
    ageGroup: 'YOUNG_ADULT',
    travelStyle: 'COUPLE',
    beachPreference: 8,
    adventurePreference: 6,
    naturePreference: 7,
    culturePreference: 4,
    nightlifePreference: 6,
    relaxationPreference: 8,
    topN: 5,
  });

  const [recommendationResult, setRecommendationResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const updateFormField = useCallback((field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  }, []);

  const fetchRecommendations = useCallback(async (customPayload = null) => {
    setLoading(true);
    setError(null);
    try {
      const payload = customPayload || {
        budget: Number(formData.budget),
        durationDays: Number(formData.durationDays),
        groupSize: Number(formData.groupSize),
        ageGroup: formData.ageGroup,
        travelStyle: formData.travelStyle,
        beachPreference: Number(formData.beachPreference),
        adventurePreference: Number(formData.adventurePreference),
        naturePreference: Number(formData.naturePreference),
        culturePreference: Number(formData.culturePreference),
        nightlifePreference: Number(formData.nightlifePreference),
        relaxationPreference: Number(formData.relaxationPreference),
        topN: Number(formData.topN),
      };

      const response = await decisionService.getRecommendations(payload);
      const resData = response.data?.data || response.data;
      setRecommendationResult(resData);
      return resData;
    } catch (err) {
      const errMsg =
        err.response?.data?.message ||
        err.message ||
        'Failed to generate recommendations from backend decision engine.';
      setError(errMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [formData]);

  const clearError = useCallback(() => setError(null), []);

  return {
    formData,
    setFormData,
    updateFormField,
    recommendationResult,
    loading,
    error,
    fetchRecommendations,
    clearError,
  };
};

export default useDecisionSupport;
