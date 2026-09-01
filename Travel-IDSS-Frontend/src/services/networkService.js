// src/services/networkService.js
// API service for Network Analysis endpoints
// Maps to: NetworkAnalysisController (@RequestMapping("/api/network"))

import api from "./api";

const NETWORK_BASE = "/network";

export const networkService = {
  /**
   * Full network analysis — returns both betweenness and closeness rankings.
   * GET /api/network/analysis?weight=distance_km
   *
   * @param {string} weight - Edge weight type: distance_km | travel_time_minutes | estimated_cost_lkr
   * @returns {Promise} NetworkAnalysisResponseDTO
   */
  getFullAnalysis: (weight = "distance_km") => {
    return api.get(`${NETWORK_BASE}/analysis`, {
      params: { weight },
    });
  },

  /**
   * Top N destinations ranked by betweenness centrality.
   * GET /api/network/betweenness?weight=distance_km&limit=10
   *
   * @param {string} weight - Edge weight type
   * @param {number} limit  - Max results (default 10)
   * @returns {Promise} CentralityScoreDTO[]
   */
  getBetweennessRanking: (weight = "distance_km", limit = 10) => {
    return api.get(`${NETWORK_BASE}/betweenness`, {
      params: { weight, limit },
    });
  },

  /**
   * Top N destinations ranked by closeness centrality.
   * GET /api/network/closeness?weight=distance_km&limit=10
   *
   * @param {string} weight - Edge weight type
   * @param {number} limit  - Max results (default 10)
   * @returns {Promise} CentralityScoreDTO[]
   */
  getClosenessRanking: (weight = "distance_km", limit = 10) => {
    return api.get(`${NETWORK_BASE}/closeness`, {
      params: { weight, limit },
    });
  },

  /**
   * Centrality scores for a single destination.
   * GET /api/network/location/{id}?weight=distance_km
   *
   * @param {string} id     - Node ID (e.g. "N001")
   * @param {string} weight - Edge weight type
   * @returns {Promise} CentralityScoreDTO
   */
  getLocationScore: (id, weight = "distance_km") => {
    return api.get(`${NETWORK_BASE}/location/${id}`, {
      params: { weight },
    });
  },

  /**
   * Minimum Spanning Tree / Forest computation using Prim's algorithm.
   * GET /api/network/mst-prim?weight=distance_km&startNodeId=N001
   *
   * @param {string} weight - Edge weight type: distance_km | travel_time_minutes | estimated_cost_lkr
   * @param {string} [startNodeId] - Optional root start node ID (e.g. "N001")
   * @returns {Promise} PrimMstResponseDTO
   */
  getMstPrimAnalysis: (weight = "distance_km", startNodeId = "") => {
    const params = { weight };
    if (startNodeId) {
      params.startNodeId = startNodeId;
    }
    return api.get(`${NETWORK_BASE}/mst-prim`, {
      params,
    });
  },
};

export default networkService;
