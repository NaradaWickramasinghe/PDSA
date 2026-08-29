// src/services/resourceService.js
// API service for Resource Allocation endpoints

import api from './api';

const RESOURCE_BASE = '/resources';

export const resourceService = {
  // Allocate resources optimally
  allocateResources: (allocationData) => {
    return api.post(`${RESOURCE_BASE}/allocate`, allocationData);
  },

  // Get all resources
  getAllResources: () => {
    return api.get(RESOURCE_BASE);
  },

  // Get resource by ID
  getResourceById: (id) => {
    return api.get(`${RESOURCE_BASE}/${id}`);
  },

  // Update resource
  updateResource: (id, resourceData) => {
    return api.put(`${RESOURCE_BASE}/${id}`, resourceData);
  },

  // Get allocation history
  getAllocationHistory: () => {
    return api.get(`${RESOURCE_BASE}/history`);
  },

  // Get resource utilization stats
  getUtilizationStats: () => {
    return api.get(`${RESOURCE_BASE}/utilization`);
  },
};

export default resourceService;
