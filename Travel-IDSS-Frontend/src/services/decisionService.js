// src/services/decisionService.js
// API service for Decision Support endpoints

import api from './api';

const DECISION_BASE = '/decisions';

export const decisionService = {
  // Get decision recommendations
  getRecommendations: (decisionData) => {
    return api.post(`${DECISION_BASE}/recommend`, decisionData);
  },

  // Evaluate decision criteria
  evaluateCriteria: (criteriaData) => {
    return api.post(`${DECISION_BASE}/evaluate`, criteriaData);
  },

  // Get decision history
  getDecisionHistory: () => {
    return api.get(`${DECISION_BASE}/history`);
  },

  // Get decision by ID
  getDecisionById: (id) => {
    return api.get(`${DECISION_BASE}/${id}`);
  },

  // Save decision result
  saveDecision: (decisionResult) => {
    return api.post(DECISION_BASE, decisionResult);
  },
};

export default decisionService;
