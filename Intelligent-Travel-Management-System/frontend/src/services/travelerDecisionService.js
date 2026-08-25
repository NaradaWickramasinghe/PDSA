/**
 * Module 4 Service Client for React / Electron Frontend
 */
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const travelerDecisionService = {
    /**
     * Submit traveler constraints & preferences to receive personalized ranked recommendations
     * @param {Object} payload 
     * @returns {Promise<Object>} RecommendationResponse
     */
    async getRecommendations(payload) {
        const response = await fetch(`${API_BASE_URL}/api/decisions/recommend`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        const json = await response.json();

        if (!response.ok || !json.success) {
            throw new Error(json.message || `Request failed with status ${response.status}`);
        }

        return json.data;
    },

    /**
     * Health check verification
     */
    async checkHealth() {
        const response = await fetch(`${API_BASE_URL}/api/decisions/health`);
        const json = await response.json();
        return json.data;
    }
};
